package com.github.nathnatica.model;

import java.util.List;

public class RecordEntity {
    public List<String> values;
    public List<ColumnEntity> columns;
    public String type;
    public List<String> expecteds;
    public List<String> actuals;
    public List<Boolean> checkResults;
}
