package com.github.nathnatica.sql;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            if (column.isWhereColumn()) {
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
            ColumnEntity c = cList.get(i);
            if (c.isWhereColumn()) {
                c.type.fillDeleteSql(preparedStatement, ++sqlParamIndex, vList.get(i));
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
    }

    @Override
    public void validateExecutionCount() throws Exception {
        if (table.isOfInputSheet() && resultCount != table.count) {
            logger.error("for table [{}][{}], expect {} records [{}]ed, but actual {} records", new Object[] {table.sheetName, table.name, table.count, name, resultCount}) ;
            throw new Exception("processed record number is not matched with excel input");
        } else if (table.isOfCheckSheet() && resultCount != table.deleteRowCount) {
                logger.error("for table [{}][{}], expect {} records [{}]ed, but actual {} records", new Object[] {table.sheetName, table.name, table.count, name, resultCount}) ;
                throw new Exception("processed record number is not matched with excel input");
        } else {
            logger.info("for table [{}][{}], {} records had been [{}]ed", new Object[] {table.sheetName, table.name, resultCount, name});
        }
    }
}
