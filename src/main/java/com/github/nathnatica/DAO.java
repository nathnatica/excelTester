package com.github.nathnatica;

import com.github.nathnatica.model.ColumnEntity;
import com.github.nathnatica.model.RecordEntity;
import com.github.nathnatica.model.TableEntity;
import com.github.nathnatica.validator.Argument;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DAO {
    final static Logger logger = LoggerFactory.getLogger(DAO.class);

    private static final String DB_DRIVER = PropertyUtil.getProperty("db.driver");
    private static final String DB_CONNECTION = PropertyUtil.getProperty("db.connection");
    private static final String DB_USER = PropertyUtil.getProperty("db.user");
    private static final String DB_PASSWORD = PropertyUtil.getProperty("db.password");

    private IDAO dao;
    
    public void execute(List<TableEntity> tables, Argument.Action action) {
        
        System.setProperty("spring.profiles.active", PropertyUtil.getProperty("env"));
        ApplicationContext context = new ClassPathXmlApplicationContext("springBeans.xml");
        logger.info("Active spring profiles : {}", Arrays.toString(context.getEnvironment().getActiveProfiles()));
      	dao = (IDAO) context.getBean("dao"); 
        
        Connection conn = getDBConnection();

        try {
            conn.setAutoCommit(false);
                
            for (TableEntity table : tables) {
                if (Argument.isInsertAction() || Argument.isDeleteAction()) {
                    insertRecordIntoTable(conn, table, action);
                } else if (Argument.isCheckAction()) {
                    selectRecordFromTable(conn, table, action);    
                }
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

    private void selectRecordFromTable(Connection dbConnection, TableEntity table, Argument.Action action) throws Exception {
        PreparedStatement preparedStatement = null;

        String sql = dao.getSqlFor(table, action);

        int count = 0;
        for (RecordEntity r : table.records) {
            preparedStatement = dbConnection.prepareStatement(sql);

            boolean hasRecord = dao.fillSql(preparedStatement, r, action);
            
            ResultSet rs = null;
            if (hasRecord) {
                rs = preparedStatement.executeQuery();
            }
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
                    count++;
                }
                if (rs.next()) {
                    logger.error("more than 1 record is selelcted");
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                rs.close();
            }
        }
    }

    private void insertRecordIntoTable(Connection dbConnection, TableEntity table, Argument.Action action) throws Exception {

        PreparedStatement preparedStatement = null;

//        String sql = table.getSQLfor(action);
        String sql = dao.getSqlFor(table, action);

        try {
            int count = 0;
            for (RecordEntity r : table.records) {
                preparedStatement = dbConnection.prepareStatement(sql);
//                boolean hasRecord = false;
//                if (action == Argument.Action.INSERT) {
//                    hasRecord = Fill.fillInsetSQL(preparedStatement, r);
//                } else if (action == Argument.Action.DELETE) {
//                    hasRecord = Fill.fillDeleteSQL(preparedStatement, r);
//                } else {
//                    throw new Exception("wrong action in DAO");
//                }
                boolean hasRecord = dao.fillSql(preparedStatement, r, action);

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
