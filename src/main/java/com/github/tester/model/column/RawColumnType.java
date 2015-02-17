package com.github.tester.model.column;

import com.github.tester.model.ColumnEntity;
import com.github.tester.model.RecordEntity;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class RawColumnType implements IColumnType {
    @Override
    public String getInsertSqlPart(String value) {
        if (value.matches("[0-9]+")) {
            return "glencrypt(?)";
        } else if (value.matches("[A-F0-9]+")) {
            return "?";
        } else {
            return "glencrypt(?)";
        }
    }

    @Override
    public String getDeleteSqlPart(ColumnEntity c, List<RecordEntity> records) {
        String sampleValue = c.getSampleColumnValue(records);
        if (!sampleValue.matches("[A-F0-9]+")) {
            return "gldecrypt(" + c.name + ")";
        }
        return c.name;
    }
    
    @Override
    public String getSelectSqlPart(String name) {
        return "gldecrypt(" + name + ")";
    }

    @Override
    public void fillInsertSql(PreparedStatement preparedStatement, int index, String value) throws Exception {
        preparedStatement.setString(index, value);
    }

    @Override
    public void fillDeleteSql(PreparedStatement preparedStatement, int index, String value) throws Exception {
        preparedStatement.setString(index, value);
//        throw new Exception("wrong column type");
    }

    @Override
    public void fillSelectSql(PreparedStatement preparedStatement, int index, String value) throws Exception {
        throw new Exception("wrong column type");
    }

    @Override
    public String getResult(ResultSet rs, String columnName) throws Exception {
        return rs.getString(columnName);
    }
    
    @Override
    public boolean isSame(String expect, String actual) {
        return StringUtils.equals(expect, actual);
    }
}
