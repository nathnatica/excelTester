package com.github.nathnatica.model;

import org.apache.commons.lang3.StringUtils;

public class TableDefEntity {
    private String type;
    private boolean pk;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }
    
    public boolean isVarchar2Type() {
        return StringUtils.equalsIgnoreCase(this.type, "VARCHAR2");
    }
    public boolean isNumberType() {
        return StringUtils.equalsIgnoreCase(this.type, "NUMBER");
    }
    public boolean isDateType() {
        return StringUtils.equalsIgnoreCase(this.type, "DATE");
    }
    public boolean isRawType() {
        return StringUtils.equalsIgnoreCase(this.type, "RAW");
    }
    
    public boolean isAccectableType() {
        if (isVarchar2Type() || isNumberType() || isDateType() || isRawType()) {
            return true;    
        } else {
            throw new IllegalArgumentException(this.type + " type is unacceptable");
        }
    }
    
}
