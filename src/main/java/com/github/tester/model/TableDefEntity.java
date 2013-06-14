package com.github.tester.model;

import com.github.tester.model.column.IColumnType;

public class TableDefEntity {
    private IColumnType type;
    private boolean pk;

    public IColumnType getType() {
        return type;
    }

    public void setType(IColumnType type) {
        this.type = type;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }
}
