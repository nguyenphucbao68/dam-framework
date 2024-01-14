package org.example.sql;

import org.example.annotation.DepthLimit;
import org.example.annotation.ManyToOne;
import org.example.annotation.OneToMany;
import org.example.annotation.OneToOne;
import org.example.mapper.ActiveRecord;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.example.mapper.ActiveRecord.*;

public class CRUDManager {
    private DatabaseConnectionManagment dam;
    private final SqlBuilder sqlBuilder = SqlBuilder.getInstance();
    private final SqlBuilderDirector sbd = new SqlBuilderDirector(sqlBuilder);
    public CRUDManager(DatabaseConnectionManagment dam){
        this.dam = dam;
    }
    public SqlBuilder sqlBuidler(){
        sqlBuilder.reset();
        return sqlBuilder;
    }
    public boolean executeInsert(ActiveRecord obj) {
        String sql = sbd.insert(obj);
        System.out.println(sql);

        boolean execution = false;

        try (Connection connection = dam.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            obj.setPrimaryKeyParameters(statement,2);
            execution = statement.execute();

            if (obj.isAutoGeneratedId()) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    obj.setAutoGeneratedId(generatedKeys);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return execution;
    }
    public boolean executeDelete(ActiveRecord obj) {
        String sql = sbd.delete(obj);
        boolean execution = false;
        System.out.println(sql);
        try (Connection connection = dam.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            obj.setPrimaryKeyParameters(statement, 1);
            execution = statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return execution;
    }
    public boolean executeUpdate(ActiveRecord obj){
        String sql = sbd.update(obj);
        boolean execution = false;

        try (Connection connection = dam.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            obj.setPrimaryKeyParameters(statement, 0);
            execution = statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return execution;
    }
    public <T extends ActiveRecord> List<T> executeSelect(String sql, Object[] conditionValues, int maxDepth)
    {
        if (maxDepth <= 0) {
            return null;
        }
        Class clazz = getClassForTableName(sqlBuilder.getTableName());

        List<T> relatedObjects = new ArrayList<>();

        try (Connection connection = dam.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if(conditionValues != null)
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

    private void setOneToOneField(ActiveRecord object, Field field, ResultSet resultSet, OneToOne oneToOneAnnotation, int maxDepth) throws SQLException, IllegalAccessException {
        field.setAccessible(true);


        String refTable = oneToOneAnnotation.refTable();
        String joinColumn = oneToOneAnnotation.joinColumn();
        String refColumn = oneToOneAnnotation.refColumn();
        if(!columnExistInResultSet(resultSet, joinColumn))
            return;
        // Assuming you have a method to fetch a single record based on a condition
        String condition = refColumn + " = ?";
        Object joinColumnValue = resultSet.getObject(joinColumn);
        Object[] conditionValues = { joinColumnValue };

        // get class based on field

        Object referencedObject = executeSelect(sbd.selectGroupByAndLimit(refTable, condition, null, null, 1), conditionValues, maxDepth - 1);

        field.set(object, referencedObject);

        field.setAccessible(false);
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

                if (oneToOneAnnotation == null
                        && oneToManyAnnotation == null
                        && manyToOneAnnotation == null
                        && columnExistInResultSet(resultSet, getColumnName(field))) {
                    field.set(object, resultSet.getObject(getColumnName(field)));
                }

                field.setAccessible(false);
            }
        }
    }
    private boolean
    columnExistInResultSet(ResultSet rs, String column)
    {
        try
        {
            rs.findColumn(column);
            return true;
        } catch (SQLException sqlex)
        {

        }
        return false;
    }

    private void setOneToManyField(ActiveRecord object, Field field, ResultSet resultSet, OneToMany oneToManyAnnotation, int maxDepth) throws SQLException, IllegalAccessException {
        field.setAccessible(true);

        String refTable = oneToManyAnnotation.refTable();
        String joinColumn = oneToManyAnnotation.joinColumn();
        String refColumn = oneToManyAnnotation.refColumn();
        if(!columnExistInResultSet(resultSet, joinColumn))
            return;

        // Assuming you have a method to fetch a list of records based on a condition
        String condition = refColumn + " = ?";
        Object joinColumnValue = resultSet.getObject(joinColumn);
        Object[] conditionValues = { joinColumnValue };


        // Retrieve the list of related objects
        List<ActiveRecord> relatedObjects = executeSelect(sbd.selectAll(refTable, condition, null,null), conditionValues, maxDepth - 1);

        // Set the collection of related objects to the field
        field.set(object, relatedObjects);

        field.setAccessible(false);
    }
    private void setManyToOneField(ActiveRecord object, Field field, ResultSet resultSet, ManyToOne manyToOneAnnotation, int maxDepth) throws SQLException, IllegalAccessException {
        field.setAccessible(true);

        String refTable = manyToOneAnnotation.refTable();
        String joinColumn = manyToOneAnnotation.joinColumn();
        String refColumn = manyToOneAnnotation.refColumn();
        if(!columnExistInResultSet(resultSet, joinColumn))
            return;

        // Assuming you have a method to fetch a single record based on a condition
        String condition = refColumn + " = ?";
        Object joinColumnValue = resultSet.getObject(joinColumn);
        Object[] conditionValues = { joinColumnValue };

        // get class based on field
        Object referencedObject = executeSelect(sbd.selectGroupByAndLimit(refTable, condition,null,null, 1), conditionValues,maxDepth - 1);

        field.set(object, referencedObject);

        field.setAccessible(false);
    }
}
