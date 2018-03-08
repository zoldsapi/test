/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package worldofbooks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author zolds
 */
public class DBUtils {
    static final String DATABASE_URL = "jdbc:mysql://localhost/testdb?autoReconnect=true&useSSL=false";
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    
    /**
     * Establising connection.
     * @throws SQLException
     */
    public DBUtils() throws SQLException {
        
        try {
            connection = DriverManager.getConnection(DATABASE_URL, "datauser", "0485640");
            
        } catch (SQLException ex) {
            System.out.println("The following error has occured: " + ex.getMessage());
        }
    }
    /**
     * Disconnects from the database.
     */
    public void disconnectFromDB() {
        
        try {
            resultSet.close();
            statement.close();
            connection.close();
        }
        catch (Exception ex) {
            System.out.println("The following error has occured: " + ex.getMessage());
        }
    }
    
    public ResultSet readRecords(String sql_stmt) {
        try {
            
            statement = connection.createStatement();
            
            resultSet = statement.executeQuery(sql_stmt);
            
            return resultSet;
            
        }
        catch (SQLException ex) {
            System.out.println("The following error has occured: " + ex.getMessage());
        }
        
        return resultSet;
    }
    
    /**
     * Executing an sql statement which is passed as parameter.
     * @param sql_stmt
     */
    public void executeSQLStatement(String sql_stmt) {
        try {
            statement = connection.createStatement();
            
            statement.executeUpdate(sql_stmt);
        }
        catch (SQLException ex) {
            System.out.println("The following error has occured: " + ex.getMessage());
        }
    }
    
}
