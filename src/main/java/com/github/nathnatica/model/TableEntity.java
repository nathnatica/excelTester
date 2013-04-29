package com.github.nathnatica.model;

import java.util.List;

public class TableEntity {

    public String name;

    public int expectedProcessCount;

    public List<ColumnEntity> columns;

    public List<DataEntity> data;

    public int count;

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

        sb.append(")");
        System.out.println(sb.toString());
        return sb.toString();
    }
}
