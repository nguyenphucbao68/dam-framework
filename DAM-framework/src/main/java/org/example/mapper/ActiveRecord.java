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
    private DatabaseConnectionManagment dam = null;
    private CRUDManager CRUDm= null;

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
            if (!field.isSynthetic() && !isIdField(field)) {
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
            if (!field.isSynthetic() && !isIdField(field)) {
                if (!values.isEmpty()) {
                    values.append(", ");
                }
                values.append("?");
            }
        }
        return values.toString();
    }

    public static String getTableNameFromClass(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        return tableAnnotation != null ? tableAnnotation.name() : "";
    }
    //select where group by having limit 1
    public <T extends ActiveRecord> T getFirst(String refTable, String condition, Object[] conditionValues, String[] groupColumns, String havingCondition, Object[] havingConditionValues, int maxDepth) throws SQLException {
        if (dam == null)
            throw new SQLException("Connection is null");
        return CRUDm.selectFirst(refTable, condition, conditionValues, groupColumns, havingCondition, havingConditionValues, maxDepth);
    }


    public <T extends ActiveRecord> List<T> getRelatedObjects(String refTable, String condition, Object[] conditionValues, String[] groupColumns, String havingCondition, Object[] havingConditionValues, int maxDepth) throws SQLException {
        if (dam == null)
            throw new SQLException("Connection is null");
        return CRUDm.selectAll(refTable, condition, conditionValues, groupColumns, havingCondition, havingConditionValues, maxDepth);
    }

    public int update() throws SQLException {
        if (dam == null)
            throw new SQLException("Connection is null");
        return CRUDm.update(this);
    }
    public int save() throws SQLException {
        if (dam == null)
            throw new SQLException("Connection is null");
        return CRUDm.insert(this);
    }
    public int delete() throws SQLException {
        if (dam == null)
            throw new SQLException("Connection is null");
        return CRUDm.delete(this);
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
            if (!field.isSynthetic() && !isIdField(field)) {
                setClause.append(getColumnName(field)).append(" = ?, ");
            }
        }
        return setClause.substring(0, setClause.length() - 2);
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
    public void setPrimaryKeyParameters(PreparedStatement statement) throws SQLException {
        int index = 1;
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic() && !isPrimaryKey(field)) {
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

    //set connection manager
    public void setConnectionManager(DatabaseConnectionManagment dam){
        this.dam = dam;
        this.CRUDm = new CRUDManager(dam);
    }

}
