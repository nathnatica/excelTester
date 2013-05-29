package com.github.nathnatica;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableEntity;
import com.github.nathnatica.validator.Argument;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DAOImpl implements IDAO {
    final static Logger logger = LoggerFactory.getLogger(DAOImpl.class);

    @Override
    public String getSqlFor(TableEntity table, Argument.Action action) {
        if (Argument.Action.INSERT == action) {
            String sql = getInsertSQL(table);
            logger.debug(sql);
            return sql;
        } else if (Argument.Action.DELETE == action) {
            String sql = getDeleteSQL(table);
            logger.debug(sql);
            return  sql;
        } else if (Argument.Action.CHECK == action) {
            String sql = getSelectSQL(table);
            logger.debug(sql);
            return  sql;
        }
        return null;
    }

    private String getSelectSQL(TableEntity table) {
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
    public boolean fillInsertSql(PreparedStatement preparedStatement, RecordEntity r) throws Exception {
        List<ColumnEntity> cList = r.columns;
        List<String> vList = r.values;
        logger.error("column list size {}, value list size {} ", cList.size(), vList.size());

        try {
            for (int i=0; i<cList.size(); i++) {
                ColumnEntity c = cList.get(i);
                logger.debug(i + "(" + c.name + ") = \"" + vList.get(i) + "\"");
                if (StringUtils.equalsIgnoreCase("VARCHAR2", c.type)) {
                    preparedStatement.setString(i+1, vList.get(i));
                } else if (StringUtils.equalsIgnoreCase("NUMBER", c.type)) {
                    if (StringUtils.isNotEmpty(vList.get(i))) {
                        preparedStatement.setBigDecimal(i+1, new BigDecimal(vList.get(i)));
                    } else {
                        preparedStatement.setBigDecimal(i+1, null);
                    }
                } else if (StringUtils.equalsIgnoreCase("RAW", c.type)) {
                    preparedStatement.setString(i+1, vList.get(i));
                } else if (StringUtils.equalsIgnoreCase("DATE", c.type)) {
//                        preparedStatement.setTimestamp(i+1, Timestamp.valueOf(vList.get(i).replace("/", "-")));
                    preparedStatement.setString(i+1, vList.get(i));
                } else {
                    throw new Exception("wrong column type");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return true;
    }

    @Override
    public boolean fillDeleteSql(PreparedStatement preparedStatement, RecordEntity r) throws Exception {
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
    public boolean fillSql(PreparedStatement preparedStatement, RecordEntity r, Argument.Action action) throws Exception {
        if (action == Argument.Action.INSERT) {
            return fillInsertSql(preparedStatement, r);
        } else if (action == Argument.Action.DELETE) {
            return fillDeleteSql(preparedStatement, r);
        } else if (action == Argument.Action.CHECK) {
            return fillSelectsql(preparedStatement, r);
        } else {
            throw new Exception("wrong action in DAO");
        }
    }

    private boolean fillSelectsql(PreparedStatement preparedStatement, RecordEntity r) throws Exception {
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

    public String getDeleteSQL(TableEntity table) {
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


    public String getInsertSQL(TableEntity table) {
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
    
//    public void print() {
//        System.out.println("[table]\t" + name);
//        System.out.print("[name]\t");
//        for (ColumnEntity c : columns) {
//            System.out.print(c.name + "\t");
//        }
//        System.out.println("");
//        System.out.print("[type]\t");
//        for (ColumnEntity c : columns) {
//            System.out.print(c.type+ "\t");
//        }
//        System.out.println("");
//        System.out.print("[con]\t");
//        for (ColumnEntity c : columns) {
//            System.out.print(c.condition+ "\t");
//        }
//        System.out.println("");
//        for (RecordEntity record : records) {
//            System.out.print("[" + record.type + "]\t");
//            for (String s : record.values) {
//                System.out.print(s + "\t");
//            }
//            System.out.println("");
//        }
//        System.out.println("");
//    }
}
