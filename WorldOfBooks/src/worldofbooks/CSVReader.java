/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package worldofbooks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author zolds
 */
public class CSVReader {
    private static final String DELIMITER = ";";
    private static Integer lineNumber;
    private static Integer orderItemId;
    private static Integer orderId = 0;
    private static String buyerName;
    private static String buyerEmail;
    private static String address;
    private static Integer postcode;
    private static Double salePrice;
    private static Double shippingPrice;
    private static String sku;
    private static String status;
    private static java.sql.Date orderDate;
    private static Double totalItemPrice;
    private static Double totalValue = 0.00;
    private static String errorMessage ="";
    private static boolean isThereError = false;
    
    /**
     * Reading and processing the test.csv file, creating the response.csv.
     * Also does numerous checks on the data, composing an errormessage if
     * needed.
     * @throws FileNotFoundException
     * @throws SQLException
     */
    public static void read() throws FileNotFoundException, SQLException{
        PrintWriter pw = new PrintWriter(new File("responseFile.csv"));
        pw.write("LineNumber;Status;Message"+"\n");
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new FileReader("inputFile.csv"));
            
            String line = "";
            
            br.readLine();
            Integer lineNr = 1;
            //Reading from the second line
            while ((line = br.readLine()) != null)
            {
                
                String[] orderDetails = line.split(DELIMITER);
                
                if(orderDetails.length > 0 )
                {
                    
                    nullFieldCheck(orderDetails);
                    if(isInteger("lineNumber",orderDetails[0]))
                        lineNumber=Integer.parseInt(orderDetails[0]);
                    if(isInteger("orderItemId",orderDetails[1]))
                        orderItemId = Integer.parseInt(orderDetails[1]);
                    if(isInteger("orderId",orderDetails[2])) {
                        if(orderId != Integer.parseInt(orderDetails[2])) {
                            totalValue = 0.00;
                        }
                        orderId = Integer.parseInt(orderDetails[2]);
                    }
                    buyerName = orderDetails[3];
                    if(isValidEmailAddress(orderDetails[4]))
                        buyerEmail = orderDetails[4];
                    
                    address = orderDetails[5];
                    if(isInteger("postcode",orderDetails[6]))
                        postcode = Integer.parseInt(orderDetails[6]);
                    if(isPositive("salePrice",Double.parseDouble(orderDetails[7])))
                        if(isSalePriceOK(Double.parseDouble(orderDetails[7])))
                            salePrice = Double.parseDouble(orderDetails[7]);
                    if(isPositive("shippingPrice",Double.parseDouble(orderDetails[8])))
                        shippingPrice = Double.parseDouble(orderDetails[8]);
                    
                    sku = orderDetails[9];
                    orderDate = formatStringToDate(orderDetails[10]);
                    if(isStatusOK(orderDetails[11]))
                        status = orderDetails[11];
                    
                    
                    isPKTaken("order_item", "order_item_id", orderItemId);
                    if(isThereError) {
                        pw.write(lineNr+": ERROR;"+errorMessage+"\n");
                    } else {
                        
                        pw.write(lineNr+": OK;"+"\n");
                        totalItemPrice = salePrice + shippingPrice;
                        totalValue += totalItemPrice;
                        insertOrder(orderId, buyerName,buyerEmail,address,postcode,orderDate,totalValue);
                        insertOrderItem(orderItemId, orderId, salePrice,shippingPrice,sku, status, totalItemPrice);
                        
                    }
                    errorMessage = "";
                    isThereError=false;
                }
                for (String orderDetail : orderDetails) {
                    System.out.println("values: " + orderDetail);
                }
                updateOrderRecord("orders", totalValue, orderId);
                lineNr++;
            }
            
        }
        catch(IOException | NumberFormatException ee)
        {
            ee.printStackTrace();
        }
        finally
        {
            try
            {
                br.close();
            }
            catch(IOException ie)
            {
                System.out.println("Error occured while closing the BufferedReader");
                ie.printStackTrace();
            }
        }
        pw.close();
    }
    /**
     * Creates a formatted java.sql.Date which can be used for insert later.
     * @param dateString String from the inputFile.csv
     * @return
     */
    public static java.sql.Date formatStringToDate(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date newDate = new Date();
        java.sql.Date sqlDate = new java.sql.Date(newDate.getTime());
        System.out.println("sql: "+sqlDate);
        if(dateString != null && !dateString.isEmpty()) {
            try {
                System.out.println("sad");
                newDate = formatter.parse(dateString);
                sqlDate = new java.sql.Date(newDate.getTime());
            } catch (ParseException e) {
                errorMessage += "OrderDate is not the correct format. ";
                isThereError = true;
                e.printStackTrace();
            }
        }
        return sqlDate;
    }
    
    /**
     * Checks the status field if it has an appropriate value.
     * @param value
     * @return
     */
    public static boolean isStatusOK(String value) {
        if("IN_STOCK".equals(value) || "OUT_OF_STOCK".equals(value)){
            return true;
        } else {
            errorMessage += "Status is not correct. ";
            isThereError = true;
            return false;
        }
    }
    
    /**
     * Checks the integer fields if they are really integers or can
     * be converted to integers.
     * @param field attributeField
     * @param value value of the attributeField
     * @return
     */
    public static boolean isInteger(String field, String value) {
        try {
            Integer.parseInt(value);
        } catch(NumberFormatException | NullPointerException e) {
            errorMessage += field+" is not an integer. ";
            isThereError = true;
            return false;
        }
        return true;
    }
    
    /**
     * Checks the boolean field if it is >= 0.00.
     * @param field
     * @param value
     * @return
     */
    public static boolean isPositive(String field, Double value) {
        if(value < 0.00) {
            errorMessage += field + " can't be lower than 0.00. ";
            isThereError=true;
            return false;
        }
        return true;
    }
    
    /**
     * Checks if the salePrice's value is >= 1.00.
     * @param value
     * @return
     */
    public static boolean isSalePriceOK(Double value) {
        if(value >= 1.00) {
            return true;
        }
        errorMessage += "salePrice must be higher than 1.00. ";
        isThereError=true;
        return false;
    }
    
    /**
     * Checks the fields if they are empty or not. If so, composes the
     * errormessage.
     * @param orderDetails
     * @return
     */
    public static boolean nullFieldCheck(String[] orderDetails) {
        for(int i = 0;i<orderDetails.length;i++) {
            
            if(orderDetails[i] != null && !orderDetails[i].isEmpty()) {
            } else {
                switcher(i);
                if(i != 10) {
                    isThereError=true;
                }
            }
        }
        if(isThereError) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Composing the errorMessage according to its parameter.
     * @param order
     */
    public static void switcher(int order) {
        switch(order) {
            case 0:
                errorMessage += "LineNumber is empty. ";
                break;
            case 1:
                errorMessage += "OrderItemId is empty. ";
                break;
            case 2:
                errorMessage += "OrderId is empty. ";
                break;
            case 3:
                errorMessage += "BuyerName is empty. ";
                break;
            case 4:
                errorMessage += "BuyerEmail is empty. ";
                break;
            case 5:
                errorMessage += "Address is empty. ";
                break;
            case 6:
                errorMessage += "Postcode is empty. ";
                break;
            case 7:
                errorMessage += "SalePrice is empty. ";
                break;
            case 8:
                errorMessage += "ShippingPrice is empty. ";
                break;
            case 9:
                errorMessage += "SKU is empty. ";
                break;
            case 10:
                break;
            case 11:
                errorMessage += "Status is empty. ";
                break;
            default:
                break;
        }
    }
    
    /**
     * Checks if the PK is already taken or not.
     * @param table name of the table
     * @param field name of the column
     * @param pkValue value of the column
     * @return
     * @throws SQLException
     * @throws FileNotFoundException
     */
    public static boolean isPKTaken(String table,String field, Integer pkValue) throws SQLException, FileNotFoundException {
        try {
            DBUtils dbUtilities = new DBUtils();
            
            String sql_stmt = "SELECT * FROM "+table+" WHERE "+field+"="+pkValue+";";
            ResultSet resultSet = dbUtilities.readRecords(sql_stmt);
            System.out.println(sql_stmt);
            if(resultSet.next()) {
                System.out.println("true");
                errorMessage += pkValue + " of "+field+" is already taken. ";
                isThereError = true;
                return true;
            }
        } catch (SQLException ex) {
            System.out.println("The following error has occured: " + ex.getMessage());
        }
        return false;
    }
    
    /**
     * Inserts a new record into the order_item table.
     * @param orderItemId
     * @param orderId
     * @param salePrice
     * @param shippingPrice
     * @param sku
     * @param status
     * @param totalItemPrice
     * @throws SQLException
     * @throws FileNotFoundException
     */
    public static void insertOrderItem(Integer orderItemId,Integer orderId,Double salePrice,Double shippingPrice,String sku,String status, Double totalItemPrice) throws SQLException, FileNotFoundException {
        
        
        DBUtils dbUtilities = new DBUtils();
        String sql_stmt =  "INSERT INTO order_item (order_item_id, order_id, sale_price, shipping_price, total_item_price, sku, status) values ("+orderItemId+","+orderId+","+salePrice+","+shippingPrice+","+totalItemPrice+",'"+sku+"','"+status+"');";
        System.out.println(sql_stmt);
        dbUtilities.executeSQLStatement(sql_stmt);
    }
    
    /**
     * Inserts a new record into the orders table.
     * @param orderId
     * @param buyerName
     * @param buyerEmail
     * @param address
     * @param postcode
     * @param orderDate
     * @param totalPrice
     * @throws SQLException
     * @throws FileNotFoundException
     */
    public static void insertOrder(Integer orderId,String buyerName, String buyerEmail, String address,Integer postcode,Date orderDate, Double totalPrice) throws SQLException, FileNotFoundException {
        
        DBUtils dbUtilities = new DBUtils();
        if(!isPKTaken("orders", "order_id", orderId)) {
            String sql_stmt = "INSERT INTO orders (order_id,buyer_name,buyer_email,order_date,order_total_value, address, postcode) VALUES (" + orderId + ",'" + buyerName + "','" + buyerEmail + "','"+orderDate+"',"+totalPrice+",'"+address+"',"+postcode+");";
            dbUtilities.executeSQLStatement(sql_stmt);
        }
    }
    
    /**
     * Updates the OrderTotalValue field in the table orders.
     * @param table
     * @param value
     * @param id
     * @throws SQLException
     */
    public static void updateOrderRecord(String table, Double value, Integer id) throws SQLException {
        DBUtils dbUtilities = new DBUtils();
        String sql_stmt = "UPDATE "+table+" SET order_total_value = "+value+" WHERE order_id = "+id+";";
        dbUtilities.executeSQLStatement(sql_stmt);
    }
    
    /**
     * Checks the e-mail address if it has a valid format.
     * @param email
     * @return
     */
    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        if(!m.matches()) {
            isThereError=true;
            errorMessage+= "Not valid e-mail. ";
        }
        return m.matches();
    }
    
}
