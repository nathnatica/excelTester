package com.github.nathnatica;

import com.github.nathnatica.model.column.IColumnType;
import com.github.nathnatica.model.column.DateColumnType;
import com.github.nathnatica.model.column.NumberColumnType;
import com.github.nathnatica.model.column.RawColumnType;
import com.github.nathnatica.model.column.Varchar2ColumnType;
import org.apache.commons.lang3.StringUtils;

public class ColumnTypeFactory implements IColumnTypeFactory {
    
    public IColumnType getColumnType(String t) {
        String type = t.trim();
        if (StringUtils.equalsIgnoreCase("VARCHAR2", type)) {
            return new Varchar2ColumnType();
        } else if (StringUtils.equalsIgnoreCase("NUMBER", type)) {
            return new NumberColumnType();
        } else if (StringUtils.equalsIgnoreCase("DATE", type)) {
            return new DateColumnType();
        } else if (StringUtils.equalsIgnoreCase("RAW", type)) {
            return new RawColumnType();
        } else {
            throw new IllegalArgumentException(type + " is not supporting column type");
        }
    }
}
