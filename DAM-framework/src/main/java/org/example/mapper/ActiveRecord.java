package org.example.mapper;
import org.example.sql.DatabaseConnectionManager;
import org.example.annotation.DepthLimit;
import org.example.annotation.Table;
import org.example.annotation.Column;
import org.example.annotation.PrimaryKey;
import org.example.annotation.OneToMany;
import org.example.annotation.OneToOne;
import org.example.annotation.ManyToOne;
import org.example.annotation.Id;
import org.example.annotation.GeneratedValue;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ActiveRecord {
    private static final Map<String, Class<?>> tableToClassMap = new HashMap<>();

    static {
        // Replace "your.package.name" with the actual package where your model classes are located
        scanClassesWithAnnotation("org.example", Table.class);
    }

    private static void scanClassesWithAnnotation(String packageName, Class<? extends Annotation> annotation) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String filePath = URLDecoder.decode(resource.getFile(), "UTF-8");
                File directory = new File(filePath);

                if (directory.isDirectory()) {
                    scanClassesInDirectory(packageName, directory, annotation);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }

    private static void scanClassesInDirectory(String packageName, File directory, Class<? extends Annotation> annotation) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanClassesInDirectory(packageName + "." + file.getName(), file, annotation);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(annotation)) {
                            Table tableAnnotation = clazz.getAnnotation(Table.class);
                            if (tableAnnotation != null) {
                                tableToClassMap.put(tableAnnotation.name(), clazz);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace(); // Handle the exception according to your needs
                    }
                }
            }
        }
    }

    protected Connection getConnection() throws SQLException {
        return DatabaseConnectionManager.getConnection();
    }

    private String getTableName() {
        Table tableAnnotation = getClass().getAnnotation(Table.class);
        return tableAnnotation != null ? tableAnnotation.name() : "";
    }

    private String getColumnName(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        return columnAnnotation != null ? columnAnnotation.name() : field.getName();
    }

    private String getColumnsString() {
        StringBuilder columns = new StringBuilder();
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic() && !isIdField(field)) {
                if (columns.length() > 0) {
                    columns.append(", ");
                }
                columns.append(getColumnName(field));
            }

        }
        return columns.toString();
    }

    private String getValuesString() {
        StringBuilder values = new StringBuilder();
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic() && !isIdField(field)) {
                if (values.length() > 0) {
                    values.append(", ");
                }
                values.append("?");
            }
        }
        return values.toString();
    }

    private String getTableNameFromClass(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        return tableAnnotation != null ? tableAnnotation.name() : "";
    }

    public <T extends ActiveRecord> T getFirst(String refTable, String condition, Object[] conditionValues, int maxDepth) {
        if (maxDepth <= 0) {
            return null;
        }
        Class clazz = getClassForTableName(refTable);

        String tableName = getTableNameFromClass(clazz);
        String sql = String.format("SELECT * FROM %s WHERE %s LIMIT 1", tableName, condition);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < conditionValues.length; i++) {
                statement.setObject(i + 1, conditionValues[i]);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    T object = newInstance(clazz);
                    setFieldsFromResultSet(object, resultSet, maxDepth);

                    return object;
                }
            }

        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setFieldsFromResultSet(ActiveRecord object, ResultSet resultSet, int maxDepth) throws SQLException, IllegalAccessException {
        Class<?> clazz = object.getClass();

        DepthLimit depthLimitAnnotation = clazz.getAnnotation(DepthLimit.class);
        maxDepth = (depthLimitAnnotation != null) ? depthLimitAnnotation.value() : maxDepth;

        for (Field field : object.getClass().getDeclaredFields()) {
            if (!field.isSynthetic()) {
                field.setAccessible(true);

                // Handle other fields as usual
                OneToOne oneToOneAnnotation = field.getAnnotation(OneToOne.class);
                if (oneToOneAnnotation != null) {
                    setOneToOneField(object, field, resultSet, oneToOneAnnotation, maxDepth);
                }

                OneToMany oneToManyAnnotation = field.getAnnotation(OneToMany.class);
                if (oneToManyAnnotation != null) {
                    setOneToManyField(object, field, resultSet, oneToManyAnnotation, maxDepth);
                }

                ManyToOne manyToOneAnnotation = field.getAnnotation(ManyToOne.class);
                if (manyToOneAnnotation != null) {
                    setManyToOneField(object, field, resultSet, manyToOneAnnotation, maxDepth);
                }

                if (oneToOneAnnotation == null && oneToManyAnnotation == null && manyToOneAnnotation == null) {
                    field.set(object, resultSet.getObject(getColumnName(field)));
                }

                field.setAccessible(false);
            }
        }
    }

    private void setOneToOneField(ActiveRecord object, Field field, ResultSet resultSet, OneToOne oneToOneAnnotation, int maxDepth) throws SQLException, IllegalAccessException {
        field.setAccessible(true);

        String refTable = oneToOneAnnotation.refTable();
        String joinColumn = oneToOneAnnotation.joinColumn();
        String refColumn = oneToOneAnnotation.refColumn();

        // Assuming you have a method to fetch a single record based on a condition
        String condition = refColumn + " = ?";
        Object joinColumnValue = resultSet.getObject(joinColumn);
        Object[] conditionValues = { joinColumnValue };


        // get class based on field

        Object referencedObject = this.getFirst(refTable, condition, conditionValues, maxDepth - 1);


        field.set(object, referencedObject);

        field.setAccessible(false);
    }

    private void setOneToManyField(ActiveRecord object, Field field, ResultSet resultSet, OneToMany oneToManyAnnotation, int maxDepth) throws SQLException, IllegalAccessException {
        field.setAccessible(true);

        String refTable = oneToManyAnnotation.refTable();
        String joinColumn = oneToManyAnnotation.joinColumn();
        String refColumn = oneToManyAnnotation.refColumn();

        // Assuming you have a method to fetch a list of records based on a condition
        String condition = refColumn + " = ?";
        Object joinColumnValue = resultSet.getObject(joinColumn);
        Object[] conditionValues = { joinColumnValue };


        // Retrieve the list of related objects
        List<ActiveRecord> relatedObjects = getRelatedObjects(refTable, condition, conditionValues, maxDepth - 1);

        // Set the collection of related objects to the field
        field.set(object, relatedObjects);

        field.setAccessible(false);
    }

    private void setManyToOneField(ActiveRecord object, Field field, ResultSet resultSet, ManyToOne manyToOneAnnotation, int maxDepth) throws SQLException, IllegalAccessException {
        field.setAccessible(true);

        String refTable = manyToOneAnnotation.refTable();
        String joinColumn = manyToOneAnnotation.joinColumn();
        String refColumn = manyToOneAnnotation.refColumn();

        // Assuming you have a method to fetch a single record based on a condition
        String condition = refColumn + " = ?";
        Object joinColumnValue = resultSet.getObject(joinColumn);
        Object[] conditionValues = { joinColumnValue };

        // get class based on field
        Object referencedObject = this.getFirst(refTable, condition, conditionValues, maxDepth - 1);

        field.set(object, referencedObject);

        field.setAccessible(false);
    }

    private <T extends ActiveRecord> List<T> getRelatedObjects(String refTable, String condition, Object[] conditionValues, int maxDepth) {
        if (maxDepth <= 0) {
            return null;
        }
        Class clazz = getClassForTableName(refTable);

        String tableName = getTableNameFromClass(clazz);
        String sql = String.format("SELECT * FROM %s WHERE %s", tableName, condition);

        List<T> relatedObjects = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < conditionValues.length; i++) {
                statement.setObject(i + 1, conditionValues[i]);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    T object = newInstance(clazz);
                    setFieldsFromResultSet(object, resultSet, maxDepth);
                    relatedObjects.add(object);
                }
            }

        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return relatedObjects;
    }

    private Class<?> getClassForTableName(String tableName) {
        return tableToClassMap.get(tableName);
    }

    private boolean isIdField(Field field) {
        return field.isAnnotationPresent(Id.class);
    }

    private boolean isGeneratedValueField(Field field) {
        return field.isAnnotationPresent(GeneratedValue.class);
    }

    private String[] getGeneratedColumns() {
        List<String> generatedColumns = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (isGeneratedValueField(field)) {
                generatedColumns.add(getColumnName(field));
            }
        }
        return generatedColumns.toArray(new String[0]);
    }

    private String[] getGeneratedColumnsValues() {
        List<String> generatedColumnsValues = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (isGeneratedValueField(field)) {
                generatedColumnsValues.add("'"+UUID.randomUUID()+"'");
            }
        }
        return generatedColumnsValues.toArray(new String[0]);
    }

    private boolean hasGeneratedColumns() {
        return getGeneratedColumns().length > 0;
    }

    private boolean isAutoGeneratedId() {
        for (Field field : getClass().getDeclaredFields()) {
            if (isIdField(field) && isGeneratedValueField(field)) {
                return true;
            }
        }
        return false;
    }

    private String[] getPrimaryKeys() {
        String[] primaryKey = new String[2];
        for (Field field : getClass().getDeclaredFields()) {
            PrimaryKey primaryKeyAnnotation = field.getAnnotation(PrimaryKey.class);
            if (primaryKeyAnnotation != null) {
                primaryKey[0] = primaryKeyAnnotation.name();
                primaryKey[1] = primaryKeyAnnotation.type();
            }
        }
        return primaryKey;
    }

    private String getInsertSql() {
        String tableName = getTableName();
        String columns = getColumnsString();
        String values = getValuesString();

        if (hasGeneratedColumns()) {
            columns += ", " + String.join(", ", getGeneratedColumns());
            values += ", " + String.join(", ", getGeneratedColumnsValues());
        }
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, values);
    }

    public void save() {
        String sql = getInsertSql();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement);
            System.out.println(statement);
            statement.executeUpdate();

            if (isAutoGeneratedId()) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    setAutoGeneratedId(generatedKeys);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setAutoGeneratedId(ResultSet generatedKeys) throws SQLException {
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

    public void update() {
        String tableName = getTableName();
        String setClause = getSetClause();
        String whereClause = getWhereClause();

        String sql = String.format("UPDATE %s SET %s WHERE %s", tableName, setClause, whereClause);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        String tableName = getTableName();
        String whereClause = getWhereClause();

        String sql = String.format("DELETE FROM %s WHERE %s", tableName, whereClause);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getSetClause() {
        StringBuilder setClause = new StringBuilder();
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isSynthetic() && !isIdField(field)) {
                setClause.append(getColumnName(field)).append(" = ?, ");
            }
        }
        return setClause.substring(0, setClause.length() - 2);
    }

    private String getWhereClause() {
        return "id = ?";
    }

    private void setParameters(PreparedStatement statement) throws SQLException {
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

    private <T extends ActiveRecord> T newInstance(Class<?> clazz) throws IllegalAccessException, InstantiationException {
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
