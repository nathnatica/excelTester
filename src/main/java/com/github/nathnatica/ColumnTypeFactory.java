package com.github.nathnatica;

import com.github.nathnatica.model.column.*;
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
            return null;
        }
    }
}
