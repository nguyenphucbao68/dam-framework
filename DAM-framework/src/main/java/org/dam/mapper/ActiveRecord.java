package org.dam.mapper;
import org.dam.annotation.*;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class ActiveRecord {
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
                field.setAccessible(true);
                try {
                    if(isIdField(field) && isGeneratedValueField(field) && field.get(this) == null)
                        values.append("'"+UUID.randomUUID()+"'");
                    else
                        values.append("?");
                }  catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                field.setAccessible(false);
            }
        }
        return values.toString();
    }

    public static Class<?> getClassForTableName(String tableName) {
        return ORMManagement.getClassScanner().getTableToClassMap().get(tableName);
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

    public void setPrimaryKeyParameters(PreparedStatement statement, int mode) throws SQLException {
        int index = 1;
        Object id = null;
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic() && ((mode == 0 || mode == 2) && !isRelationField(field)) || (mode == 1 && isPrimaryKey(field))) {
                field.setAccessible(true);
                try {
                    Object value = field.get(this);
                    if(isPrimaryKey(field) && mode != 2){
                        id = value;
                    }
                    if(mode == 0 || mode == 1 || (mode == 2 && (!isGeneratedValueField(field) || (isGeneratedValueField(field) && value != null)))){
                        statement.setObject(index, value);
                        index++;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                field.setAccessible(false);
            }
        }

        if(mode == 0 && id != null){
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
