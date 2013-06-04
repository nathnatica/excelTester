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

public class InsertSql implements ISql {
    
    final static Logger logger = LoggerFactory.getLogger(InsertSql.class);
    final static String name = "Insert";
    int resultCount = 0;
    TableEntity table = null;

    @Override
    public String getSqlFor(TableEntity table) {
        this.table = table;

        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(table.name);
        sb.append(" (");

        for (int i=0; i<table.columns.size(); i++) {
            ColumnEntity column = table.columns.get(i);
            sb.append(column.name);
            if (i<table.columns.size()-1) {
                sb.append(", ");
            }
        }
        sb.append(") values (");

        for (int i=0; i<table.columns.size(); i++) {
            ColumnEntity column = table.columns.get(i);
            sb.append(column.getInsertSQLPart(table.records));
            if (i<table.columns.size()-1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean fillSql(PreparedStatement preparedStatement, RecordEntity r) throws Exception {
        List<ColumnEntity> cList = r.columns;
        List<String> vList = r.values;
        logger.error("column list size {}, value list size {} ", cList.size(), vList.size());

        try {
            for (int i=0; i<cList.size(); i++) {
                ColumnEntity c = cList.get(i);
                logger.debug(i + "(" + c.name + ") = \"" + vList.get(i) + "\"");
                c.type.fillInsertSql(preparedStatement, i+1, vList.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
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
        if (resultCount != table.count) {
            logger.error("for table {}, expect {} records {}ed, but actual {} records", new Object[] {table.name, table.count, name, resultCount}) ;
            throw new Exception("processed record number is not matched with excel input");
        } else {
            logger.info("for table {}, {} records had been [{}]ed", new Object[] {table.name, resultCount, name});
        }
    }
}
