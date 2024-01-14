package org.example.mapper;
import org.example.annotation.*;
import org.example.sql.CRUDManager;
import org.example.sql.DatabaseConnectionManagment;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class ActiveRecord {
    private static final ClassScanner classScanner = new ClassScanner();

    static {
        // Replace "your.package.name" with the actual package where your model classes are located
        classScanner.scanClassesWithAnnotation("org.example", Table.class);
    }

    public String getTableName() {
        Table tableAnnotation = getClass().getAnnotation(Table.class);
        return tableAnnotation != null ? tableAnnotation.name() : "";
    }

    public static String getColumnName(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        return columnAnnotation != null ? columnAnnotation.name() : field.getName();
    }
    private String getPrimaryKeyName(Field field){
        PrimaryKey columnAnnotation = field.getAnnotation(PrimaryKey.class);
        return columnAnnotation != null ? columnAnnotation.name() : field.getName();
    }

    public String getColumnsString() {
        StringBuilder columns = new StringBuilder();
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic() && !isRelationField(field)) {
                if (!columns.isEmpty()) {
                    columns.append(", ");
                }
                columns.append(getColumnName(field));
            }

        }
        return columns.toString();
    }

    public String getValuesString() {
        StringBuilder values = new StringBuilder();
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic() && !isRelationField(field)) {
                if (!values.isEmpty()) {
                    values.append(", ");
                }
                if(isIdField(field) && isGeneratedValueField(field))
                    values.append("'"+UUID.randomUUID()+"'");
                else
                    values.append("?");
            }
        }
        return values.toString();
    }

    public static String getTableNameFromClass(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        return tableAnnotation != null ? tableAnnotation.name() : "";
    }
    public static Class<?> getClassForTableName(String tableName) {
        return classScanner.getTableToClassMap().get(tableName);
    }

    private boolean isIdField(Field field) {
        return field.isAnnotationPresent(Id.class);
    }
    private boolean isPrimaryKey(Field field){
        return field.isAnnotationPresent(PrimaryKey.class);
    }

    private boolean isGeneratedValueField(Field field) {
        return field.isAnnotationPresent(GeneratedValue.class);
    }

    public String[] getGeneratedColumns() {
        List<String> generatedColumns = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (isGeneratedValueField(field)) {
                generatedColumns.add(getColumnName(field));
            }
        }
        return generatedColumns.toArray(new String[0]);
    }

    public String[] getGeneratedColumnsValues() {
        List<String> generatedColumnsValues = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (isGeneratedValueField(field)) {
                generatedColumnsValues.add("'"+UUID.randomUUID()+"'");
            }
        }
        return generatedColumnsValues.toArray(new String[0]);
    }

    public boolean hasGeneratedColumns() {
        return getGeneratedColumns().length > 0;
    }

    public boolean isAutoGeneratedId() {
        for (Field field : getClass().getDeclaredFields()) {
            if (isIdField(field) && isGeneratedValueField(field)) {
                return true;
            }
        }
        return false;
    }

    public void setAutoGeneratedId(ResultSet generatedKeys) throws SQLException {
        for (Field field : getClass().getDeclaredFields()) {
            if (isIdField(field) && isGeneratedValueField(field)) {
                field.setAccessible(true);
                try {
                    field.set(this, generatedKeys.getObject(1));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                field.setAccessible(false);
            }
        }
    }
    public String getSetClause() {
        StringBuilder setClause = new StringBuilder();
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic() && !isRelationField(field)) {
                setClause.append(getColumnName(field)).append(" = ?, ");
            }
        }
        return setClause.substring(0, setClause.length() - 2);
    }

    public boolean isRelationField(Field field) {
        return field.isAnnotationPresent(OneToMany.class)
                || field.isAnnotationPresent(OneToOne.class)
                || field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(ManyToMany.class);
    }

    public String getWherePrimaryKey(){
        String wherePrimaryClause = "";
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic() && isPrimaryKey(field)) {
                field.setAccessible(true);
                if(wherePrimaryClause.length() > 0)
                    wherePrimaryClause = wherePrimaryClause + String.format(" AND %s = ?", getColumnName(field));
                else
                    wherePrimaryClause = String.format("%s = ?", getPrimaryKeyName(field));
                field.setAccessible(false);
            }
        }
        return wherePrimaryClause;
    }
    public void setParameters(PreparedStatement statement) throws SQLException {
        int index = 1;
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic() && !isIdField(field)) {
                field.setAccessible(true);
                try {
                    statement.setObject(index, field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                field.setAccessible(false);
                index++;
            }
        }
    }
    public void setPrimaryKeyParameters(PreparedStatement statement, int mode) throws SQLException {
        int index = 1;
        Object id = null;
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic() && ((mode == 0 || mode == 2) && !isRelationField(field)) || (mode == 1 && isPrimaryKey(field))) {
                field.setAccessible(true);
                try {
                    if(isPrimaryKey(field) && mode != 2){
                        System.out.println("Primary key: " + field.get(this));
                        id = field.get(this);
                    }
                    if(mode == 0 || (mode == 2 && !isGeneratedValueField(field))){
                        System.out.println("Set parameter: " + field.get(this));

                        statement.setObject(index, field.get(this));
                        index++;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                field.setAccessible(false);
            }
        }

        if(mode == 0 && id != null){
            System.out.println("Set parameter2: " + id);
            statement.setObject(index, id);
        }
    }


    public static <T extends ActiveRecord> T newInstance(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        try {
            return (T) clazz.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new InstantiationException("Error creating a new instance of " + clazz.getName());
        }
    }

    // print all fields
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic()) {
                field.setAccessible(true);
                try {
                    sb.append(field.getName()).append(": ").append(field.get(this)).append("\n");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                field.setAccessible(false);
            }
        }
        return sb.toString();
    }
}
