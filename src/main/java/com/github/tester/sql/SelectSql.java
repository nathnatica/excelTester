package com.github.tester.sql;

import com.github.tester.model.ColumnEntity;
import com.github.tester.model.RecordEntity;
import com.github.tester.model.TableEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SelectSql implements ISql {
    
    final static Logger logger = LoggerFactory.getLogger(SelectSql.class);
    final static String name = "Select";
    int resultCount = 0;
    ResultSet rs = null;
    TableEntity table = null;

    @Override
    public String getSqlFor(TableEntity table) {
        this.table = table;
        
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        boolean isFirstColumn = true;
        for (int i=0; i<table.columns.size(); i++) {
            ColumnEntity column = table.columns.get(i);
            if (!isFirstColumn) {
                sb.append(",");
            }
            sb.append(column.type.getSelectSqlPart(column.name));
            isFirstColumn = false;
        }
        sb.append(" from ");
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
        List<String> vList = r.expecteds;
        int sqlParamIndex = 0;
        for (int i=0; i<cList.size(); i++) {
            ColumnEntity c = cList.get(i);
            if (c.isWhereColumn()) {
                c.type.fillSelectSql(preparedStatement, ++sqlParamIndex, vList.get(i));
            }
        }
        return true;
    }

    @Override
    public void excuteSqlsForTable(PreparedStatement preparedStatement) throws SQLException {
        rs = preparedStatement.executeQuery();
    }


    @Override
    public void postProcess(RecordEntity r) {
        try {
            if (rs.next()) {
                List<ColumnEntity> cList = r.columns;
                r.actuals = new ArrayList<String>();
                for (ColumnEntity c : cList) {
                    r.actuals.add(c.type.getResult(rs, c.name));
                }
                r.isExisingRecord = true;
                resultCount++;
            }
            if (rs.next()) {
                logger.error("more than 1 record is selected");
                throw new Exception();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void validateExecutionCount() throws Exception {
        if (resultCount != table.count) {
            logger.error("for table [{}][{}], expect {} records [{}]ed, but actual {} records", new Object[] {table.sheetName, table.name, table.count, name, resultCount}) ;
            throw new Exception("processed record number is not matched with excel input");
        } else {
            logger.info("for table [{}][{}], {} records had been [{}]ed", new Object[] {table.sheetName, table.name, resultCount, name});
        }
    }

}
