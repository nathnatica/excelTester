package com.github.nathnatica.model;

import java.util.List;

public class TableEntity {

    public String name;

    public List<ColumnEntity> columns;

    public List<RecordEntity> records;

    public int count;

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

        System.out.println(sb.toString());
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
        System.out.println(sb.toString());
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
