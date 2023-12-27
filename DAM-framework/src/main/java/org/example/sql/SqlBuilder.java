package org.example.sql;

import java.lang.reflect.Field;

public class SqlBuilder {
    String sqlString = null;
    public SqlBuilder insert(){
        sqlString = "INSERT"; //INTO %s (%s) VALUES (%s)";
        return this;
    }
    public SqlBuilder update(String tableName){
        sqlString = "UPDATE " + tableName;// %s SET %s WHERE %s";
        return this;
    }
    public  SqlBuilder delete(){
        sqlString = "DELETE";//FROM %s WHERE %s";
        return this;
    }
    public SqlBuilder select(){
        sqlString = "SELECT *";//FROM %s WHERE %s";
        return this;
    }
    public SqlBuilder limit(int num){
        sqlString = String.format("%s LIMIT %d", sqlString, num);
        return this;
    }
    public SqlBuilder into(String tableName){
        sqlString = String.format("%s INTO %s", sqlString, tableName);
        return this;
    }
    public SqlBuilder from(String tableName){
        sqlString = String.format("%s FROM %s", sqlString, tableName);
        return this;
    }
    public SqlBuilder column(String columnsString){
        sqlString = String.format("%s (%s)", sqlString, columnsString);
        return this;
    }
    public SqlBuilder selectedColumn(String[] columns){
        if(columns != null) {
            StringBuilder columnsString = new StringBuilder();
            for (String column : columns) {
                if (!columnsString.isEmpty()) {
                    columnsString.append(", ");
                }
                columnsString.append(column);
            }
            if (columns.length > 0) {
                sqlString = sqlString.substring(0, sqlString.length() - 2);
                sqlString = String.format("%s %s", sqlString, columnsString);
            }
        }
        return this;
    }
    public SqlBuilder value(String valuesString){
        sqlString = String.format("%s VALUES (%s)", sqlString, valuesString);
        return this;
    }
    public SqlBuilder setValues(String setValuesString){
        sqlString = String.format("%s SET (%s)", sqlString, setValuesString);
        return this;
    }
    public SqlBuilder where(String whereString){
        if(whereString != null && !whereString.isEmpty())
            sqlString = String.format("%s WHERE %s", sqlString, whereString);
        return this;
    }
    public SqlBuilder groupBy(String[] columns){
        if(columns != null){
            StringBuilder columnsString = new StringBuilder();
            for (String column : columns) {
                if (!columnsString.isEmpty()) {
                    columnsString.append(", ");
                }
                columnsString.append(column);
            }
            if(columns.length > 0) {
                sqlString = String.format("%s GROUP BY %s", sqlString, columnsString);
            }
        }
        return this;
    }
    public SqlBuilder having(String[] groupByColumns, String havingString){
        if(groupByColumns != null && groupByColumns.length > 0 && havingString != null && !havingString.isEmpty())
            sqlString = String.format("%s HAVING %s", sqlString, havingString);
        return this;
    }
    public String result(){
        return sqlString;
    }
}
