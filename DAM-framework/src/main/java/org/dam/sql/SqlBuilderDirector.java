package org.dam.sql;

import org.dam.mapper.ActiveRecord;

public class SqlBuilderDirector {
    private SqlBuilder sb = null;
    SqlBuilderDirector(SqlBuilder sb){
        this.sb = sb;
    }
    public String insert(ActiveRecord obj){
        String columns = obj.getColumnsString();
        String values = obj.getValuesString();

        return sb.insert()
                .into(obj.getTableName())
                .column(columns)
                .value(values)
                .result();
    }
    public String delete(ActiveRecord obj){
        return sb.delete()
                .from(obj.getTableName())
                .where(obj.getWherePrimaryKey())
                .result();
    }
    public String update(ActiveRecord obj){
        return sb.update(obj.getTableName())
                .setValues(obj.getSetClause())
                .where(obj.getWherePrimaryKey())
                .result();
    }
    public String selectAll(String tablename, String condition, String[] groupByColumns, String havingCondition){
        return sb.select()
                .selectedColumn(groupByColumns)
                .from(tablename)
                .where(condition)
                .groupBy(groupByColumns)
                .having(groupByColumns, havingCondition)
                .result();
    }
    public String selectGroupByAndLimit(String tablename, String condition, String[] groupByColumns, String havingCondition, int limitNum){
        return sb.select()
                .selectedColumn(groupByColumns)
                .from(tablename)
                .where(condition)
                .groupBy(groupByColumns)
                .having(groupByColumns, havingCondition)
                .limit(limitNum)
                .result();
    }
}
