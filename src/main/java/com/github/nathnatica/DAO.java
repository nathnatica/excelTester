package com.github.nathnatica;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DAO {

    private static final String DB_DRIVER = PropertyUtil.getProperty("db.driver");
    private static final String DB_CONNECTION = PropertyUtil.getProperty("db.connection");
    private static final String DB_USER = PropertyUtil.getProperty("db.user");
    private static final String DB_PASSWORD = PropertyUtil.getProperty("db.password");

    public static void main(String[] argv) {

        try {

            insertRecordIntoTable();

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        }

    }

    private static void insertRecordIntoTable() throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String insertTableSQL = "INSERT INTO DBUSER" + "(USER_ID, USERNAME, CREATED_BY, CREATED_DATE) VALUES" + "(?,?,?,?)";

        try {
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(insertTableSQL);

            preparedStatement.setInt(1, 11);
            preparedStatement.setString(2, "XXX");
            preparedStatement.setString(3, "XXX");
            preparedStatement.setTimestamp(4, getCurrentTimeStamp());

            // execute insert SQL stetement
            preparedStatement.executeUpdate();

            System.out.println("Record is inserted into DBUSER table!");

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
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
