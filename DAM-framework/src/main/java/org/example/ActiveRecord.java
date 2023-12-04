package org.example;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface UUIDGenerate {
    boolean generate() default false;
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Table {
    String name();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Column {
    String name();
    String type();
}

// annotations interface for PrimaryKey in SQL
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface PrimaryKey {
    String name();
    String type();
}

// annotations interface for ForeignKey in SQL
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface ForeignKey {
    String name();
    String type();
}

// annotations interface for OneToOne in SQL
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface OneToOne {
    // Name of the referenced table
    String refTable();

    // Name of the foreign key column in the current table
    String joinColumn();

    // Name of the referenced table's primary key column
    String refColumn();
}

// annotations interface for OneToMany in SQL
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface OneToMany {
    String dbRef();
    String type();
}

// annotations interface for ManyToOne in SQL
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface ManyToOne {
    String dbRef();
    String type();
}

// annotations interface for ManyToMany in SQL
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface ManyToMany {
    String dbRef();
    String type();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Id {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface GeneratedValue {
}

public class ActiveRecord {
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/ticket?loggerLevel=OFF";
    private static final String USER = "postgres";
    private static final String PASSWORD = "localdb";

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
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
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

    <T extends ActiveRecord> T getFirst(String refTable, String condition, Object[] conditionValues) {
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
                    setFieldsFromResultSet(object, resultSet);
                    return object;
                }
            }

        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setFieldsFromResultSet(ActiveRecord object, ResultSet resultSet) throws SQLException, IllegalAccessException {
        for (Field field : object.getClass().getDeclaredFields()) {
            if (!field.isSynthetic()) {
                field.setAccessible(true);

                // Handle other fields as usual
                OneToOne oneToOneAnnotation = field.getAnnotation(OneToOne.class);
                if (oneToOneAnnotation != null) {
                    setOneToOneField(object, field, resultSet, oneToOneAnnotation);
                } else {
                    // Handle other fields as usual
                    field.set(object, resultSet.getObject(getColumnName(field)));
                }

                field.setAccessible(false);
            }
        }
    }

    private void setOneToOneField(ActiveRecord object, Field field, ResultSet resultSet, OneToOne oneToOneAnnotation) throws SQLException, IllegalAccessException {
        field.setAccessible(true);

        String refTable = oneToOneAnnotation.refTable();
        String joinColumn = oneToOneAnnotation.joinColumn();
        String refColumn = oneToOneAnnotation.refColumn();

        // Assuming you have a method to fetch a single record based on a condition
        String condition = refColumn + " = ?";
        Object joinColumnValue = resultSet.getObject(joinColumn);
        Object[] conditionValues = { joinColumnValue };


        // get class based on field

        Object referencedObject = this.getFirst(refTable, condition, conditionValues);


        field.set(object, referencedObject);

        field.setAccessible(false);
    }

    private Class<?> getClassForTableName(String tableName) {
        return tableToClassMap.get(tableName);
    }

    private boolean isIdField(Field field) {
        return field.isAnnotationPresent(Id.class);
    }

    // private formatValue to String or Integer based on type in annotation @Column
    private String formatValue(Field field, Object value) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        String type = columnAnnotation.type();
        if (type.equals("varchar")) {
            return "'" + value + "'";
        } else if (type.equals("int")) {
            return value.toString();
        } else if (type.equals("datetime")) {
            return "'" + value + "'";
        } else if (type.equals("uuid")) {
            return "'" + value + "'";
        }
        return "";
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
