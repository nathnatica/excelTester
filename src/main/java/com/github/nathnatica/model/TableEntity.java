package com.github.nathnatica.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TableEntity {
    public String name;
    public List<ColumnEntity> columns;
    public List<RecordEntity> records;
    public int count;
}
