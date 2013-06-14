package com.github.tester.model.column;

import com.github.tester.model.ColumnEntity;
import com.github.tester.model.RecordEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public interface IColumnType {
    
    String getInsertSqlPart(String value);
    String getDeleteSqlPart(ColumnEntity c, List<RecordEntity> records);
    String getSelectSqlPart(String name);
    void fillInsertSql(PreparedStatement preparedStatement, int index, String value) throws Exception;
    void fillDeleteSql(PreparedStatement preparedStatement, int index, String value) throws Exception;
    void fillSelectSql(PreparedStatement preparedStatement, int index, String value) throws Exception;
    String getResult(ResultSet rs, String columnName) throws Exception;
    boolean isSame(String expect, String actual);
}
