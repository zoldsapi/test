/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldofbooks;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Scanner;

/**
 *
 * @author zolds
 */
public class WorldOfBooks {

    /**
     * Shows the menu.
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, SQLException {
        displayMenu();
    }
    
    /**
     * Draws the menu, waiting for input.
     * @throws FileNotFoundException
     * @throws SQLException 
     */
  public static void displayMenu() throws FileNotFoundException, SQLException {
      Scanner userInput = new Scanner(System.in);
      
        System.out.println("*****************************************");
        System.out.println("| Options:                              |");
        System.out.println("|        1. Read a CSV file             |");
        System.out.println("|        2. Exit                        |");
        System.out.println("*****************************************"); 
        
        System.out.println("Select an option: ");
        String readOption = userInput.next();
        
        if ("1".equals(readOption)) {
            CSVReader.read();
        } else if ("2".equals(readOption)) {
            System.exit(0);
        } else {
            System.out.println("Invalid option.");
        }
                
  }  
}
