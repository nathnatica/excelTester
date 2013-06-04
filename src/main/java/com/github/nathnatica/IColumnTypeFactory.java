package com.github.nathnatica;

import com.github.nathnatica.model.column.IColumnType;

public interface IColumnTypeFactory {
    public IColumnType getColumnType(String type);
}
