package com.github.tester.model;

import java.util.List;

public class TableEntity {
    public String sheetName;
    public String name;
    public List<ColumnEntity> columns;
    public List<RecordEntity> records;
    public int count;
    public int deleteRowCount;
    
    
    public boolean isOfInputSheet() {
        return this.sheetName.contains("input");    
    }
    public boolean isOfCheckSheet() {
        return this.sheetName.contains("check");
    }
}
