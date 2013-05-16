package com.github.nathnatica.model;

import com.github.nathnatica.validator.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TableEntity {

    final static Logger logger = LoggerFactory.getLogger(TableEntity.class);
    
    public String name;

    public List<ColumnEntity> columns;

    public List<RecordEntity> records;

    public int count;

    public String getSQLfor(Argument.Action action) {
        if (Argument.Action.INSERT == action) {
            String sql = getInsertSQL();
            logger.debug(sql);
            return sql;    
        } else if (Argument.Action.DELETE == action) {
            String sql = getDeleteSQL();
            logger.debug(sql);
            return  sql; 
        }
        return null;
    } 
    
    
    public String getDeleteSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("delete from ");
        sb.append(name);
        sb.append(" where ");

        boolean isFirstCondition = true;
        for (int i=0; i<columns.size(); i++) {
            ColumnEntity column = columns.get(i);
            if (column.condition != null && (column.condition.contains("W") || column.condition.contains("w"))) {
                if (!isFirstCondition) {
                    sb.append(" and ");
                }
                sb.append(column.getDeleteSQLPart(records));
                sb.append(" = ?");
                isFirstCondition = false;
            }
        }
        return sb.toString();
    }


    public String getInsertSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(name);
        sb.append(" (");

        for (int i=0; i<columns.size(); i++) {
            ColumnEntity column = columns.get(i);
            sb.append(column.name);
            if (i<columns.size()-1) {
               sb.append(", ");
            }
        }
        sb.append(") values (");

        for (int i=0; i<columns.size(); i++) {
            ColumnEntity column = columns.get(i);
            sb.append(column.getInsertSQLPart(records));
            if (i<columns.size()-1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public void print() {
        System.out.println("[table]\t" + name);
        System.out.print("[name]\t");
        for (ColumnEntity c : columns) {
            System.out.print(c.name + "\t");
        }
        System.out.println("");
        System.out.print("[type]\t");
        for (ColumnEntity c : columns) {
            System.out.print(c.type+ "\t");
        }
        System.out.println("");
        System.out.print("[con]\t");
        for (ColumnEntity c : columns) {
            System.out.print(c.condition+ "\t");
        }
        System.out.println("");
        for (RecordEntity record : records) {
            System.out.print("[" + record.type + "]\t");
            for (String s : record.values) {
                System.out.print(s + "\t");
            }
            System.out.println("");
        }
        System.out.println("");
    }
}
