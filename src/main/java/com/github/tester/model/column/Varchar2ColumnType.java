package com.github.tester.model.column;

import com.github.tester.model.ColumnEntity;
import com.github.tester.model.RecordEntity;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class Varchar2ColumnType implements IColumnType {
    @Override
    public String getInsertSqlPart(String value) {
        return "?";
    }
    @Override
    public String getDeleteSqlPart(ColumnEntity c, List<RecordEntity> records) {
        return c.name;
    }
    
    @Override
    public String getSelectSqlPart(String name) {
        return name;
    }

    @Override
    public void fillInsertSql(PreparedStatement preparedStatement, int index, String value) throws Exception {
        preparedStatement.setString(index, value);
    }

    @Override
    public void fillDeleteSql(PreparedStatement preparedStatement, int index, String value) throws Exception {
        preparedStatement.setString(index, value);
    }

    @Override
    public void fillSelectSql(PreparedStatement preparedStatement, int index, String value) throws Exception {
        preparedStatement.setString(index, value);
    }

    @Override
    public String getResult(ResultSet rs, String columnName) throws Exception {
        return rs.getString(columnName);
    }
    
    @Override
    public boolean isSame(String expect, String actual) {
        if (StringUtils.isNotEmpty(expect) && StringUtils.isNotEmpty(actual)) {
            return StringUtils.equals(expect.trim(), actual.trim());
        } else {
            return StringUtils.isEmpty(expect) && StringUtils.isEmpty(actual);
        }
    }
}
