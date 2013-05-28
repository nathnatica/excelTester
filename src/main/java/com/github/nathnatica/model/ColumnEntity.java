package com.github.nathnatica.model;

import org.apache.commons.lang3.StringUtils;
import java.util.List;

public class ColumnEntity {
    private int index;
    public String name;
    public String type;
    public String condition;
    public String check;
    
    public ColumnEntity(int index) {
        this.index = index;
    }

    public String getInsertSQLPart(String type, String value) {
        if (StringUtils.equalsIgnoreCase("VARCHAR2", type)) {
            return "?";
        } else if (StringUtils.equalsIgnoreCase("NUMBER", type)) {
            return "?";
        } else if (StringUtils.equalsIgnoreCase("DATE", type)) {
            if (value.matches("[0-9]{4}/[0-9]{2}/[0-9]{2} [0-9]{1,2}:[0-9]{2}:[0-9]{2}")) {
                return "TO_DATE(?, 'YYYY/MM/DD HH24:MI:SS')";
            } else if (value.matches("[0-9]{8}")) {
                return "TO_DATE(?, 'YYYYMMDD')";
            } else if (value.matches("[0-9]{4}/[0-9]{2}/[0-9]{2}")) {
                return "TO_DATE(?, 'YYYY/MM/DD')";
            } else if (value.matches("[0-9]{14}")) {
                return "TO_DATE(?, 'YYYYMMDDHH24MISS')";
            } else if (StringUtils.isBlank(value)) {
                return "?";
            } else {
                throw new IllegalArgumentException(value + " is not supporting DATE format");
                
            }
        } else if (StringUtils.equalsIgnoreCase("RAW", type)) {
            if (value.matches("[A-Z0-9]+")) {
                return "?";
            } else {
                return "glencrypt(?)";
            }
        } else {
            throw new IllegalArgumentException(type + " is not supporting column type");
        }
    }

    public String getInsertSQLPart(List<RecordEntity> records) {
        String sampleValue = StringUtils.EMPTY;
        OUTER:
        for (RecordEntity record : records) {
            for (int i=0; i<record.values.size(); i++) {
                if (i == this.index && StringUtils.isNotEmpty(record.values.get(i))) {
                    sampleValue = record.values.get(i);
                    break OUTER;
                }
            }
        }
        return this.getInsertSQLPart(this.type, sampleValue);
    }

    public String getDeleteSQLPart(List<RecordEntity> records) {
        if (StringUtils.equalsIgnoreCase(this.type, "RAW")) {
            String sampleValue = StringUtils.EMPTY;
            OUTER:
            for (RecordEntity record : records) {
                for (int i=0; i<record.values.size(); i++) {
                    if (i == this.index && StringUtils.isNotEmpty(record.values.get(i))) {
                        sampleValue = record.values.get(i);
                        break OUTER;
                    }
                }
            }

            if (!sampleValue.matches("[A-Z0-9]+")) {
                return "gldecrypt(" + this.name + ")";
            }
        }
        return this.name;
    }
}
