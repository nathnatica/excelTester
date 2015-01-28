package com.github.tester;

import com.github.tester.model.RecordEntity;
import com.github.tester.model.TableEntity;
import com.github.tester.sql.ISql;
import com.github.tester.util.ResourceBundleUtil;
import com.github.tester.validator.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DAO {
    final static Logger logger = LoggerFactory.getLogger(DAO.class);

    private static final String DB_DRIVER = PropertyUtil.getProperty("db.driver");
    private static final String DB_CONNECTION = ResourceBundleUtil.get("db.connection");
    private static final String DB_USER = ResourceBundleUtil.get("db.user");
    private static final String DB_PASSWORD = ResourceBundleUtil.get("db.password");

    Connection conn = getDBConnection();
    
    public boolean execute(List<TableEntity> tables, Argument.Action action) {
        
        SqlFactory sqlFactory = (SqlFactory) BeanConfigurator.getBean("sqlMaker"); 

        try {
            conn.setAutoCommit(false);

            for (TableEntity table : tables) {
                ISql sqlObject = sqlFactory.getSqlFor(action);
                executeTableSqls(conn, table, sqlObject);                
            }
            return true;
        } catch (SQLException e) {
            logger.debug(e.getMessage());
            try {
                conn.rollback();
                logger.error("rollbacked") ;
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback();
                logger.error("rollbacked");
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return false;
        }
    }
    
    public void commit(boolean result) {
        try {
            if (result) {
                conn.commit();
                logger.info("commited");
            }
        } catch (SQLException e) {
            e.printStackTrace();
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


    private void executeTableSqls(Connection dbConnection, TableEntity table, ISql sqlObject) throws Exception {

        String sql = sqlObject.getSqlFor(table);
        logger.debug(sql);
        
        PreparedStatement preparedStatement = null;
        try {
            for (RecordEntity r : table.records) {
                preparedStatement = dbConnection.prepareStatement(sql);
                boolean hasRecord = sqlObject.fillSql(preparedStatement, r);

                if (hasRecord) {
                    sqlObject.excuteSqlsForTable(preparedStatement);
                }

                sqlObject.postProcess(r);

            }
            sqlObject.validateExecutionCount();
        } catch (SQLException e) {
            logger.debug(e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.debug(e.getMessage());
            throw e;
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

}
