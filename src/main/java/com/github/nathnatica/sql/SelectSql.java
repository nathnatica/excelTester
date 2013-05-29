package com.github.nathnatica.sql;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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
            if (column.type.equalsIgnoreCase("RAW")) {
                sb.append("gldecrypt(").append(column.name).append(")");
            } else {
                sb.append(column.name);
            }
            isFirstColumn = false;
        }
        sb.append(" from ");
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
        List<String> vList = r.expecteds;
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
        rs = preparedStatement.executeQuery();
    }


    @Override
    public void postProcess(RecordEntity r) {
        try {
            if (rs.next()) {
                List<ColumnEntity> cList = r.columns;
                r.actuals = new ArrayList<String>();
                for (int i=0; i<cList.size(); i++) {
                    ColumnEntity c = cList.get(i);
                    if (StringUtils.equalsIgnoreCase("VARCHAR2", c.type)) {
                        r.actuals.add(rs.getString(c.name));
                    } else if (StringUtils.equalsIgnoreCase("NUMBER", c.type)) {
                        r.actuals.add("" + rs.getBigDecimal(c.name));
                    } else if (StringUtils.equalsIgnoreCase("RAW", c.type)) {
                        r.actuals.add(rs.getString(c.name));
                    } else if (StringUtils.equalsIgnoreCase("DATE", c.type)) {
                        r.actuals.add(rs.getString(c.name));
                    } else {
                        throw new Exception("wrong column type");
                    }
                }
                r.isExisingRecord = true;
                resultCount++;
            }
            if (rs.next()) {
                logger.error("more than 1 record is selelcted");
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
            logger.error("for table {}, expect {} records {}ed, but actual {} records", new Object[] {table.name, table.count, name, resultCount}) ;
            throw new Exception("processed record number is not matched with excel input");
        } else {
            logger.info("for table {}, {} records had been [{}]ed", new Object[] {table.name, resultCount, name});
        }
    }

}
