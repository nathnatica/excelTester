package com.github.nathnatica.sql;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DeleteSql implements ISql {
    
    final static Logger logger = LoggerFactory.getLogger(DeleteSql.class);
    final static String name = "Delete";
    int resultCount = 0;
    TableEntity table = null;
    
    @Override
    public String getSqlFor(TableEntity table) {
        this.table = table;
        
        StringBuilder sb = new StringBuilder();
        sb.append("delete from ");
        sb.append(table.name);
        sb.append(" where ");

        boolean isFirstCondition = true;
        for (int i=0; i<table.columns.size(); i++) {
            ColumnEntity column = table.columns.get(i);
            if (column.condition != null && (column.condition.contains("W") || column.condition.contains("w"))) {
                if (!isFirstCondition) {
                    sb.append(" and ");
                }
                sb.append(column.getDeleteSQLPart(table.records));
                sb.append(" = ?");
                isFirstCondition = false;
            }
        }
        return sb.toString();
    }

    @Override
    public boolean fillSql(PreparedStatement preparedStatement, RecordEntity r) throws Exception {
        List<ColumnEntity> cList = r.columns;
        List<String> vList = r.values;
        int sqlParamIndex = 0;
        for (int i=0; i<cList.size(); i++) {
            String condition = cList.get(i).condition;
            ColumnEntity c = cList.get(i);
            if (StringUtils.equalsIgnoreCase("W", condition)) {
                if (StringUtils.equalsIgnoreCase("VARCHAR2", c.type)) {
                    logger.debug(i + " = " + vList.get(i));
                    preparedStatement.setString(++sqlParamIndex, vList.get(i));
                } else if (StringUtils.equalsIgnoreCase("NUMBER", c.type)) {
                    logger.debug(i + " = " + vList.get(i));
                    preparedStatement.setBigDecimal(++sqlParamIndex, new BigDecimal(vList.get(i)));
                } else {
                    throw new Exception("wrong column type");
                }
            }
        }
        return true;
    }

    @Override
    public void excuteSqlsForTable(PreparedStatement preparedStatement) throws SQLException {
        resultCount += preparedStatement.executeUpdate();
    }

    @Override
    public void postProcess(RecordEntity r) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void validateExecutionCount() throws Exception {
        if (resultCount != table.count) {
            logger.error("for table {}, expect {} records {}ed, but actual {} records", new Object[] {table.name, table.count, name, resultCount}) ;
            throw new Exception("processed record number is not matched with excel input");
        } else {
            logger.info("for table {}, {} records had been [{}]ed", new Object[] {table.name, resultCount, name});
        }
    }
}
