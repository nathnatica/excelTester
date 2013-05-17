package com.github.nathnatica;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableEntity;
import com.github.nathnatica.validator.Argument;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class DAO {
    final static Logger logger = LoggerFactory.getLogger(DAO.class);

    private static final String DB_DRIVER = PropertyUtil.getProperty("db.driver");
    private static final String DB_CONNECTION = PropertyUtil.getProperty("db.connection");
    private static final String DB_USER = PropertyUtil.getProperty("db.user");
    private static final String DB_PASSWORD = PropertyUtil.getProperty("db.password");

    
    
    public void execute(List<TableEntity> tables, Argument.Action action) {
        Connection conn = getDBConnection();

        try {
            conn.setAutoCommit(false);
                
            for (TableEntity table : tables) {
                insertRecordIntoTable(conn, table, action);
            }
//            conn.rollback();
            conn.commit();
            logger.info("commited");
        } catch (SQLException e) {
            logger.debug(e.getMessage());
            try {
                conn.rollback();
                logger.error("rollbacked") ;
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback();
                logger.error("rollbacked");
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            if (conn!= null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void insertRecordIntoTable(Connection dbConnection, TableEntity table, Argument.Action action) throws Exception {

        PreparedStatement preparedStatement = null;

        String sql = table.getSQLfor(action);

        try {
            int count = 0;
            for (RecordEntity r : table.records) {
                preparedStatement = dbConnection.prepareStatement(sql);
                boolean hasRecord = false;
                if (action == Argument.Action.INSERT) {
                    hasRecord = fillInsetSQL(preparedStatement, r);
                } else if (action == Argument.Action.DELETE) {
                    hasRecord = fillDeleteSQL(preparedStatement, r);
                } else {
                    throw new Exception("wrong action in DAO");
                }

                logger.info("hasRecord is");
                logger.info("hasRecord is {}", hasRecord);
                if (hasRecord) {
                    count += preparedStatement.executeUpdate();
                }
            }

            if (count != table.count) {
                logger.error("for table {}, expect {} records {}ed, but actual {} records", new Object[] {table.name, table.count, action, count}) ;
                throw new Exception("wrong number of insertion");   
            } else {
                logger.info("for table {}, {} records had been [{}]ed", new Object[] {table.name, count, action});
            }
        } catch (SQLException e) {
            logger.debug(e.getMessage());
        } catch (Exception e) {
            logger.debug(e.getMessage());
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }

    }

    private static boolean fillDeleteSQL(PreparedStatement preparedStatement, RecordEntity r) throws Exception {
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

    private static boolean fillInsetSQL(PreparedStatement preparedStatement, RecordEntity r) throws Exception {
        List<ColumnEntity> cList = r.columns;
        List<String> vList = r.values;
        for (int i=0; i<cList.size(); i++) {
            ColumnEntity c = cList.get(i);
            if (StringUtils.equalsIgnoreCase("VARCHAR2", c.type)) {
                logger.debug(i + " = " + vList.get(i));
                preparedStatement.setString(i+1, vList.get(i));
            } else if (StringUtils.equalsIgnoreCase("NUMBER", c.type)) {
                logger.debug(i + " = " + vList.get(i));
                preparedStatement.setBigDecimal(i+1, new BigDecimal(vList.get(i)));
            } else if (StringUtils.equalsIgnoreCase("RAW", c.type)) {
                logger.debug(i + " = " + vList.get(i));
                preparedStatement.setString(i+1, vList.get(i));
            } else if (StringUtils.equalsIgnoreCase("DATE", c.type)) {
                logger.debug(i + " = " + vList.get(i));
//                        preparedStatement.setTimestamp(i+1, Timestamp.valueOf(vList.get(i).replace("/", "-")));
                preparedStatement.setString(i+1, vList.get(i));
            } else {
                throw new Exception("wrong column type");
            }
        }
        return true;
    }

    private static Connection getDBConnection() {
        Connection dbConnection = null;
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,DB_PASSWORD);
            return dbConnection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbConnection;
    }

    private static java.sql.Timestamp getCurrentTimeStamp() {
        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());
    }


//    dbConnection.setAutoCommit(false);//commit trasaction manually
//    dbConnection.commit();


//    String sql = "DELETE FROM mockexam WHERE MockID =?";
//    PreparedStatement prest = con.prepareStatement(sql);
//    prest.setString(1, mock);
//    int val = prest.executeUpdate();

//    String selectSQL = "SELECT USER_ID, USERNAME FROM DBUSER WHERE USER_ID = ?";
//    PreparedStatement preparedStatement = dbConnection.prepareStatement(selectSQL);
//    preparedStatement.setInt(1, 1001);
//    ResultSet rs = preparedStatement.executeQuery(selectSQL );
//    while (rs.next()) {
//        String userid = rs.getString("USER_ID");
//        String username = rs.getString("USERNAME");
//    }
}
