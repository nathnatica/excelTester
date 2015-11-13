package com.github.tester.model;

import com.github.tester.RowUtil;
import com.github.tester.model.column.IColumnType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ColumnEntity {
    private int index;
    public String name;
    public IColumnType type;
    public String condition;
    public String check;
    
    public ColumnEntity(int index) {
        this.index = index;
    }

    public String getInsertSQLPart(String value) {
        return this.type.getInsertSqlPart(value);
    }

    public String getInsertSQLPart(List<RecordEntity> records) {
        String sampleValue = getSampleColumnValue(records);
        return this.getInsertSQLPart(sampleValue);
    }

    public String getDeleteSQLPart(List<RecordEntity> records) {
        return this.type.getDeleteSqlPart(this, records);
    }
    
    public String getSampleColumnValue(List<RecordEntity> records) {
        for (RecordEntity record : records) {
            if (StringUtils.isNotEmpty(record.values.get(this.index))) {
                return record.values.get(this.index);
            }
        }
        return StringUtils.EMPTY;
    }
    
    public boolean isWhereColumn() {
        return StringUtils.equalsIgnoreCase(RowUtil.COLUMN_CONDITON_WHERE, this.condition);
    }

    public boolean isCheckTypeEqual() {
        return StringUtils.equalsIgnoreCase(RowUtil.COLUMN_CHECK_TYPE_EQUAL, this.check);
    }
}
