package com.github.tester;

import com.github.tester.model.column.IColumnType;

public interface IColumnTypeFactory {
    public IColumnType getColumnType(String type);
}
