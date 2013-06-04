package com.github.nathnatica.model.column;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class DateColumnType implements IColumnType {
    @Override
    public String getInsertSqlPart(String value) {
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
        throw new Exception("wrong column type");
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
        String e = expect.replaceAll("/", "").replaceAll(":","").replaceAll(" ","").replaceAll("\\.","");
        String a = actual.replaceAll("/", "").replaceAll(":","").replaceAll(" ","").replaceAll("\\.","");
        
        while (e.length() != a.length()) {
            if (e.length() < a.length()) {
                e = e + "0";    
            } else if (e.length() > a.length()) {
                a = a + "0";
            }
        }
        
        return StringUtils.equals(e, a);
    }

}
