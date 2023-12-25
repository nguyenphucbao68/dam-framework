package org.example.sql;

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
    public SqlBuilder value(String valuesString){
        sqlString = String.format("%s VALUES (%s)", sqlString, valuesString);
        return this;
    }
    public SqlBuilder setValues(String setValuesString){
        sqlString = String.format("%s SET (%s)", sqlString, setValuesString);
        return this;
    }
    public SqlBuilder where(String whereString){
        sqlString = String.format("%s WHERE %s", sqlString, whereString);
        return this;
    }
    public SqlBuilder groupBy(String groupByColumns){
        sqlString = String.format("%s GROUP BY %s", sqlString, groupByColumns);
        return this;
    }
    public SqlBuilder having(String havingString){
        sqlString = String.format("%s HAVING %s", sqlString, havingString);
        return this;
    }
    public String result(){
        return sqlString;
    }
}
