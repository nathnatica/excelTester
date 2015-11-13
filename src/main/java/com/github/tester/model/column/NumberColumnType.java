package com.github.tester.model.column;
import com.github.tester.model.ColumnEntity;
import com.github.tester.model.RecordEntity;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class NumberColumnType implements IColumnType {
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
        if (StringUtils.isNotEmpty(value)) {
            preparedStatement.setBigDecimal(index, new BigDecimal(value));
        } else {
            preparedStatement.setBigDecimal(index, null);
        }
    }

    @Override
    public void fillDeleteSql(PreparedStatement preparedStatement, int index, String value) throws Exception {
        preparedStatement.setBigDecimal(index, new BigDecimal(value));
    }

    @Override
    public void fillSelectSql(PreparedStatement preparedStatement, int index, String value) throws Exception {
        preparedStatement.setBigDecimal(index, new BigDecimal(value));
    }

    @Override
    public String getResult(ResultSet rs, String columnName) throws Exception {
        return "" + rs.getBigDecimal(columnName);
    }

    @Override
    public boolean isSame(String expect, String actual) {
        return StringUtils.equals(expect, actual);
    }
}
