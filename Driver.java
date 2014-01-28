import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;


public class Driver {

// Variable declarations

// Constants
private final static String DRIVER = "com.mysql.jdbc.Driver";
private final static String DB = "jdbc:mysql://localhost:3306/airline";
private final static String USER = "root";
private final static String PW = "jasper";

private static String departMonth, departDay, departYear, custPhoneNum, resNum,
  destAirport,departAirport, custID, custFirstName, custLastName,
  custStreet, custCity, flightNum, username, password;

private static int menuChoice, flightInfoChoice, custVerification, retryFlight;

private static LinkedList<String> validFlightNums   = new LinkedList<String>();
private static LinkedList<String> fullFlights  = new LinkedList<String>();
private static LinkedList<Integer> custReservations = new LinkedList<Integer>();
private static LinkedList<String> airports = new LinkedList<String>(Arrays.asList("LGA", "BOS", "DCA", "RDU",
   "ATL", "MCO", "MIA"));

private static Scanner scan = new Scanner(System.in);


public static void main(String[] args) {

System.out.println("Welcome to AAS Airline's temporary reservation system \n" +
           "During our inaugral month - Februrary 2014 - Free flights for all! \n");

displayLogin();
}

/********************************************************************************************************************/
/** METHODS	    **/
/********************************************************************************************************************/


/**************************************************************************************/
/** This function will take the user's input and make the program do a specific task **/
/** depending on what the user has inputed using a switch statement. Switch is in a  **/
/** function so it can easily be called through different cases if needed	 **/
/**************************************************************************************/
private static void callSwitch() {

switch(menuChoice){

// View customer info and reservations
case 1:
displayCustInfo();
displayMenu();
callSwitch();
break;

// Search and reserve flights
case 2:
System.out.print("******************************************\n" +
     "* Welcome to AAS Airline's Flight Search *\n" +
     "******************************************\n\n" +
"** Please note that ALL flights only depart from or arrive to LGA **\n\n" +
"If you would like to see a list of valid airports, please enter 1\n" +
"If you already know your departure and destination, enter any other number: ");
System.out.println("");
while(!scan.hasNextInt()){
System.out.print("Invalid choice. Please enter a number: ");
scan.next();
}
flightInfoChoice = scan.nextInt();

// If user wants to see a list of the airports with their codes, display them
if(flightInfoChoice == 1)
displayAirports();

// Automatically convert all airport code entries to upper case to match database
System.out.print("\nPlease enter the airport code you would like to leave from: ");
departAirport = scan.next().toUpperCase();

// Checking if a valid airport
while(!airports.contains(departAirport)){
System.out.print("** Sorry, " + departAirport + " is not a valid choice. Please try again: ");
departAirport = scan.next().toUpperCase();
}

System.out.print("Please enter the airport code you would like to go to: ");
destAirport = scan.next().toUpperCase();

// Checking if a valid airport
while(!airports.contains(destAirport)){
System.out.print("** Sorry, " + destAirport + " is not a valid choice. Please try again: ");
destAirport = scan.next().toUpperCase();
}

// Checking error if user wants to fly to and from the same airport
// If so, recall this switch option for user to retry
if(departAirport.equals(destAirport)){
System.out.println(
    "\n***************************************************************************\n" +
"** Sorry, you can not fly to and from the same airport. Please try again **\n" +
    "***************************************************************************\n");
menuChoice = 2;
callSwitch();
}

System.out.print("Please enter your anticpated travel month, day, and year separated by spaces " +
("(e.g. 01 12 2014): "));
departMonth = scan.next();
departDay = scan.next();
departYear = scan.next();

// Checking to see if date is in correct format, if not, have user keep entering until it is
while(
((departMonth.length() != 2) || Integer.parseInt(departMonth) > 12) ||
((departDay.length() != 2) || Integer.parseInt(departDay) > 31) ||
((departYear.length() != 4) || Integer.parseInt(departYear) < 2014)){
System.out.print("\n** Sorry, you have an entered an invalid date format.\n" +
         "** Format should be in the MM DD YYYY order\n" +
         "** e.g. 01 12 2014\n" +
         "** Please try again: "
);

departMonth = scan.next();
departDay = scan.next();
departYear = scan.next();
}

// Display to user that system is searching for flights on specified constraints
System.out.println("\nLooking for flights from " + departAirport + " to " + destAirport + 
   " on " + departMonth + "-" + departDay + "-" + departYear + "...");

searchFlights();
reserveFlight();

break;

// Look up reservation info
case 3:
searchReservations();
break;

// View the list of available airports
case 4:
displayAirports();
System.out.println();
displayMenu();
callSwitch();
break;

// Quit
case 5:
System.out.println("Thank you for visiting AAS's reservation system.");
scan.close();
System.exit(0);

// Defaults to having the user enter a valid choice
default:
System.out.println("You have entered an invalid choice. Please choose again:\n");
displayMenu();
}

}


/*****************************************************/
/** Function that displays airports in a neat table **/
/*****************************************************/
static void displayAirports(){
System.out.println(
"+---------------------------------------------------------------------------+\n" +
"| Airport Code | Airport Info                                               |\n" +
"+--------------|------------------------------------------------------------+\n" +
"| LGA          | LaGuradia Airport, New York                                |\n" +
"| BOS          | Logan International Airport, Boston                        |\n" + 
"| DCA          | Ronald Reagan National Airport, Washington D.C.            |\n" +
"| RDU          | Durham International Airport, Raleigh, North Carolina      |\n" +
"| ATL          | Hartsfield Jackson International Airport, Atlanta, Georgia |\n" +
"| MCO          | Orlando International Airport, Orlando, Florida            |\n" +
"| MIA          | Miami International Airport, Miami, Florida                |\n" +
"+---------------------------------------------------------------------------+"
);
}


/*****************************************************/
/** Function that displays the menu in a neat table **/
/*****************************************************/
static void displayMenu(){
System.out.print("Welcome, " + custFirstName + "!\n\n" +
   "Please make a selection from the list below:\n" +
   "+----------------------------------------------+\n" +
       "| 1. View Your Customer Info and Reservations  |\n" +
   "| 2. Search for and reserve Flights            |\n" +
   "| 3. Get Existing Reservation Info             |\n" +
   "| 4. List of AAS hubs                          |\n" +
   "| 5. Quit                                      |\n" +
   "+----------------------------------------------+\n\n" +
   "Please enter your choice (1-5): "
);

while(!scan.hasNextInt()){
System.out.print("Invalid choice. Please enter a number: ");
scan.next();
}
menuChoice = scan.nextInt();
System.out.println();
}


/*************************************************************************************/
/** This function will connect to the database to be queried based on what the user **/
/** inputed. The flights table will be queried and displayed to the user with the   **/
/** specified restraints as String variables	     **/
/*************************************************************************************/
static void searchFlights(){

Connection conn = null; // Connection variable that will connect to DB
ResultSet rs = null;    // Variable that holds the results of the query
Statement stmt = null;  // Variable that is used to execute query

try{
// Connect to the DB
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB, USER, PW);
            
            // Set up query in a string
            String flightLookup = "SELECT * FROM flights " +
  "WHERE airport_start = '" + departAirport + "' AND airport_finish = '" +
  destAirport + "' AND travel_month = " + departMonth + " AND travel_day = " + 
  departDay + " AND travel_year = " + departYear + ";";
            
            // Create and execute the above query using the mysql DB
            stmt = conn.createStatement();
            rs = stmt.executeQuery(flightLookup);
          
           if(rs.next() == false){
String date = departMonth + "-" + departDay + "-" + departYear;
System.out.print("\n**Sorry, we do not fly from " + departAirport + " to " + destAirport +
   " on " + date + ".**\n\nEnter 1 if you would like to find another flight\n" +
   "Enter any other number if you would like to return to the main menu: ");
while(!scan.hasNextInt()){
System.out.print("Invalid choice. Please enter a number: ");
scan.next();
}
retryFlight = scan.nextInt();

if(retryFlight == 1){
System.out.println();
menuChoice = 2;
callSwitch();
}
else{
displayMenu();
callSwitch();
}
            }
            
            else{
            // Reset result set
            rs.beforeFirst();
            System.out.println("+---------------------------------------------------------------------------------" +
           "---------------------------------------------------------------------------+");
            System.out.println("| Status | Flight Number | Route Number | Airplane ID | Month | Day | Year | Depart From " +
           "| Arrive To | Depart Time | Arrival Time | Travel Time | Seats Left |");
        System.out.println("+--------+---------------+--------------+-------------+-------+-----+------+--------" +
           "-----+-----------+-------------+--------------+-------------+------------+");
        
        // For each result in the result set
        while(rs.next()){
        // Set variables for each field and format them for neatness
        String flight_num  = String.format("%" + -14 + "s", rs.getString("flight_no"));
        String route_num   = String.format("%" + -13 + "s", rs.getString("route_no"));
        String plane_id    = String.format("%" + -12 + "s", rs.getString("airplane_id"));
        String month       = String.format("%" + -6 + "s", rs.getString("travel_month"));   
        String day         = String.format("%" + -4 + "s", rs.getString("travel_day")); 
        String year    = String.format("%" + -5 + "s", rs.getString("travel_year"));
        String depart    = String.format("%" + -12 + "s", rs.getString("airport_start"));
        String arrive    = String.format("%" + -10 + "s", rs.getString("airport_finish"));
        String depart_time = String.format("%" + -12 + "s", rs.getString("depart_time"));
        String arrive_time = String.format("%" + -13 + "s", rs.getString("arrival_time"));
        String travel_time = String.format("%" + -12 + "s", rs.getString("travel_mins"));
        String seats_left  = String.format("%" + -11 + "s", rs.getInt("max_occupancy") - rs.getInt("reserved_seats"));
        String status;
        
        if(rs.getInt("max_occupancy") - rs.getInt("reserved_seats") != 0){
        status = String.format("%" + -7 + "s", "OPEN");
        validFlightNums.add(rs.getString("flight_no"));
        }
        else{
        status = String.format("%" + -7 + "s", "CLOSED");
        fullFlights.add(rs.getString("flight_no"));
        }
        
        
        // Print out results in table form
        System.out.println("| " + status	  +
           "| " + flight_num + 
           "| " + route_num + 
           "| " + plane_id + 
           "| " + month +
           "| " + day +
           "| " + year +
           "| " + depart +
           "| " + arrive +
           "| " + depart_time +
           "| " + arrive_time +
           "| " + travel_time +
           "| " + seats_left +
           "| " );
        }
        
        // Footer
        System.out.println("+------------------------------------------------------------------------------" +
           "------------------------------------------------------------------------------+");
            }	
}catch (ClassNotFoundException e) {
        System.out.println("Driver not found");
    }
    catch (SQLException e) {
        e.printStackTrace();
    }finally {
try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
}
}


/*********************************************************************/
/** This function will insert the customer in to the customer table **/
/** based on the information the user provides to the program	    **/
/*********************************************************************/
static void insertCust(){
Connection conn = null;  // Connection variable that will connect to DB
Statement stmt = null;   // Variable that is used to execute query
ResultSet rs = null;

try{
// Connect to the DB
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB, USER, PW);
            
            // Create the SQL statement
            stmt = conn.createStatement();

            // Store query as a String
            String insert = "INSERT INTO customer (username, password, first_name, last_name, phone_number, street, city) " +
                "VALUES('" + username + "', '" + password + "', '" + custFirstName + "', '" + custLastName +
                "', " + custPhoneNum + ", '" + custStreet + "', '" + custCity + "');";	
            
            // Execute insert statement
            stmt.executeUpdate(insert);
            
            stmt = null;
            
            stmt = conn.createStatement();
            
            String getCustID = "SELECT customer_id from customer where username = '" + username + "';";
            
            rs = stmt.executeQuery(getCustID);
            while(rs.next()){
            custID = rs.getString("customer_id");
            }
            
            
            System.out.println("\nThank you! You have successfully registered to AAS Airlines database.\n");
}catch (ClassNotFoundException e) {
        System.out.println("Driver not found");
    }
    catch (SQLException e) {
        e.printStackTrace();
    }finally {
try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
}
}


/***************************************************************/
/** This function gets the flight number the user inputed and **/
/** inserts that data in to the reservations table   **/
/***************************************************************/
static void reserveFlight(){

Connection conn = null;
boolean flightNumCheck = false;
int selection;
Statement stmt = null;
ResultSet rs = null;

try{
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB, USER, PW);
            
            
            
System.out.print("\nEnter 1 if you would like to reserve any of these flights\n" +
       "Enter any other number if you would like to restart your flight search: ");
while(!scan.hasNextInt()){
System.out.print("Invalid choice. Please enter a number: ");
scan.next();
}
selection = scan.nextInt();


if(selection == 1){
System.out.print("\nEnter the flight number you would like to book: ");
flightNum = scan.next().toUpperCase();

while(fullFlights.contains(flightNum)){
System.out.print("\n** Sorry, " + flightNum + " is full **\n" +
"Please enter another or enter 'q' to quit: ");

flightNum = scan.next().toUpperCase();

if(flightNum.equals("Q")){
System.out.println();
displayMenu();
callSwitch();
}

}

while(flightNumCheck == false){
if(validFlightNums.contains(flightNum)){
stmt = conn.createStatement();

String fullName = custFirstName + " " + custLastName;
String reserve = "INSERT INTO reservations (customer_id, name, flight_no) VALUES(" + custID +
", '" + fullName + "', '" + flightNum + "');";
String getReservations = "SELECT reservation_no FROM reservations WHERE customer_id = " + custID + ";";
String updateSeats = "UPDATE flights " +
"SET reserved_seats = reserved_seats + 1 " +
"WHERE flight_no = '" + flightNum + "';";

stmt.executeUpdate(reserve);

stmt = null;

stmt = conn.createStatement();	
rs   = stmt.executeQuery(getReservations);

while(rs.next()){
int res_no = rs.getInt("reservation_no");
custReservations.add(res_no);
}

stmt = null;
stmt = conn.createStatement();
stmt.execute(updateSeats);

System.out.println("\n*******************************************************\n" +
   "* You have successfully reserved a flight for " + flightNum +"! *\n" +
   "*******************************************************\n\n" +
   "You will now be returned to the main menu.\n");

flightNumCheck = true;
displayMenu();
callSwitch();
}
else{
System.out.print("You have entered an invalid flight number. Please enter it again: ");
flightNum = scan.next().toUpperCase();
}
}


}
else{
menuChoice = 2;
callSwitch();
}	
}catch (ClassNotFoundException e) {
        System.out.println("Driver not found");
    }
    catch (SQLException e) {
        e.printStackTrace();
    }finally {
try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
}
}


/******************************************************************/
/** Display the login screen. User chooses if he wants to log in **/
/** as an existing user or to create a new account	      **/
/******************************************************************/
static void displayLogin(){
int userCheck;

System.out.print("New users enter 1\n" +
"Existing users enter 2: ");

while(!scan.hasNextInt()){
System.out.print("Invalid choice. Please enter a number: ");
scan.next();
}
userCheck = scan.nextInt();

if(userCheck == 1)
registerCust();
else
custSignIn();
}


/**************************************************************************/
/** Function that retrieves account/customer information from user input **/
/**************************************************************************/
static void registerCust(){

boolean pwCheck = false;         // Verify passwords match
boolean usernameExists = true;   // Verify username not already taken
String pw = "";	 // String to hold password

System.out.println("\nAAS Airline's welcomes you to our Family.\n" +
   "You're just a couple short steps away from your customer ID\n" +
   "which can be used to book and look up reservations.\n");

/** Gather user information **/

// Get username
while(usernameExists){

System.out.print("Please enter your desired username: ");
username = scan.next();

if(checkUsername() == 0)  // Check if username is taken already
break;
else
System.out.print("**Sorry, '" + username + "' is already taken. Please try again.**\n");
        }

// Get password
while(!pwCheck){

System.out.print("Please enter the password you wish to use: ");
password = scan.next();
pw = password;

System.out.print("Re-enter password to verify: ");
password = scan.next();

// Check to make sure passwords equal each other. If not, have them retry
if(pw.equals(password))
pwCheck = true;
else
System.out.println("**Passwords do not match. Please try again.**");

}

// Get first name
System.out.print("Please enter your first name: ");
custFirstName = scan.next();

// Get last name
System.out.print("Please enter your last name: ");
custLastName = scan.next();

// Get phone number
System.out.print("Please enter your phone number as 7 consecutive digits: ");
custPhoneNum = scan.next();

// Get phone number

// Checking whether phone number user enters is 7 digits or not
// If not, have user keep entering until it is
while(custPhoneNum.length() != 7){
System.out.print("Your phone number must be 7 digits. Please try again: ");
custPhoneNum = scan.next();
}
scan.nextLine();

// Get street
System.out.print("Please enter your street name: ");
custStreet = scan.nextLine();	

// Get city
System.out.print("Please enter the name of the city in which you reside: ");
custCity = scan.nextLine();

// Output user what they have inputed to verify if it is correct, if so, continue
// if not, have user enter information again by setting case to 1 and recalling switch
System.out.println("\n\nUsername: " + username);
System.out.println("Name: " + custFirstName + " " + custLastName);
System.out.println("Phone Number: " + custPhoneNum);
System.out.println("Address: " + custStreet + " " + custCity);
System.out.print("\nIf this information is correct, enter 1 now. If not, enter any other number to retry: ");
while(!scan.hasNextInt()){
System.out.print("Invalid choice. Please enter a number: ");
scan.next();
}
custVerification = scan.nextInt();

// If the user has verified their information, set them up in the DB
if(custVerification == 1){
insertCust();
displayMenu();
callSwitch();
}

// Else recall switch so user can reenter info
else{
System.out.println();
registerCust();
}

}


/***********************************************************/
/** Function used for case 1 which displays all customer  **/
/** and reservation info to the user in a table	   **/
/***********************************************************/
static void displayCustInfo(){

Connection conn = null;  // Connection variable that will connect to DB
ResultSet rs    = null;  // Variable that holds the results of the query
Statement stmt  = null;  // Variable that is used to execute query

try{
// Connect to the DB
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB, USER, PW);
            
            // Query to get all reservation numbers from customer
            String getReservations = "SELECT reservation_no FROM reservations " +
            "WHERE customer_id = " + custID + ";";
            
            // Query to get all information of customer
            String getInfo = "SELECT * FROM customer " +
                 "WHERE username = '" + username + "';";
            
            // Reset reservation list so there are no duplicates
            if(!custReservations.isEmpty())
            custReservations.clear();
            
            
            /** Set up first statement which retrieves all customer reservation numbers **/
            /** and adds them to an array to be displayed in the next statement	     **/
            
            stmt = conn.createStatement();
        rs = stmt.executeQuery(getReservations);
        
        // Add all reservations to array
while(rs.next()){
int res_no = rs.getInt("reservation_no");
custReservations.add(res_no);
}

// After we retrieve all info, reset variables
rs   = null;
stmt = null;
            

            /** Set up second statement which retrieves all customer's **/
/** information from the database and displays it to them  **/	

            stmt = conn.createStatement();
            rs   = stmt.executeQuery(getInfo);
            
            // Print customer info table
            // Header of table
        System.out.print("+----------------------------------------+\n" +
        "|          CUSTOMER INFORMATION          |\n" +
        "+----------------------------------------+\n");
            while(rs.next()){
            String cust_id   = String.format("%" + -25 + "s", rs.getString("customer_id"));
            String fullName  = String.format("%" + -32 + "s", 
                (rs.getString("first_name") + " " + rs.getString("last_name")));
            String phone_num = String.format("%" + -24 + "s", rs.getString("phone_number"));
            String address   = String.format("%" + -29 + "s",
            (rs.getString("street") + " " + rs.getString("city")));
            
            System.out.print("| Name: "         + fullName  + " |\n" +
            "| Customer ID: "  + cust_id   + " |\n" +
                             "| Phone Number: " + phone_num + " |\n" +
                             "| Address: "      + address   + " |\n" +
                             "| Reservation #s: ");
            
            // Call loop to get customer reservation numbers
    for(int i : custReservations){
    if(i != custReservations.peekLast())  // If last element, don't add a comma
    System.out.print(i + ", ");
    else
    System.out.print(i);
    }
    
    // Footer of table
            System.out.println("\n+----------------------------------------+\n");
            } 
}catch (ClassNotFoundException e) {
        System.out.println("Driver not found");
    }
    catch (SQLException e) {
        e.printStackTrace();
    }finally {
try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
}
}


/**************************************************************/
/** This function attempts to sign the user in by calling to **/
/** the database to see if there is a match on a tuple for   **/
/** the username and password the user entered	      **/
/**************************************************************/
static void custSignIn(){

Connection conn = null;
ResultSet rs    = null;
Statement stmt  = null;

try{
Class.forName(DRIVER);
         conn = DriverManager.getConnection(DB, USER, PW);
         
         System.out.print("\nUsername: ");
         username = scan.next();
         
         System.out.print("Password: ");
         password = scan.next();
         
         // Query that checks DB if username even exists in database
         String checkUser = "SELECT * FROM customer WHERE username = '" + username + "';";
         
         stmt = conn.createStatement();
         rs = stmt.executeQuery(checkUser);
         
         // If there is no result, tell user that name doesn't exist
         if(!rs.next()){
        System.out.print("\nSorry, username does not exist. Please try again.\n");
        custSignIn();
         }
         // If there is a result, make sure their password is correct by calling the password field in the DB
         else{
        String pw = rs.getString("password");  // Password stored in DB
        
        // If user inputed password does not equal password in the DB
        // have user try to sign in again
        if(!password.equals(pw)){
        System.out.println("You have entered an incorrect username/password combination. Please try again.");
        custSignIn();
        }
        // If they sign in, give access to the menu
        else{
        setUpCust();
        System.out.println();
        displayMenu();
        callSwitch();
        }
        
         }
         
}catch (ClassNotFoundException e) {
        System.out.println("Driver not found");
    }
    catch (SQLException e) {
        e.printStackTrace();
    }finally {
try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
}
}


/********************************************************/
/** Function that assigns the Java variables to the DB **/
/** information to make for easier coding	    **/
/********************************************************/
static void setUpCust(){

Connection conn = null;  // Connection variable that will connect to DB
ResultSet rs    = null;  // Variable that holds the results of the query
Statement stmt  = null;  // Variable that is used to execute query

try{
// Connect to the DB
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB, USER, PW);
            
            // Query that retrieves all customer information based on username
            String registerCust = "SELECT * FROM customer " +
              "WHERE username = '" + username + "';";
            
            // Create and execute the above query using the mysql DB
            stmt = conn.createStatement();
            rs = stmt.executeQuery(registerCust);
            
            // Assign
            while(rs.next()){
            custID        = rs.getString("customer_id");
            custFirstName = rs.getString("first_name");
            custLastName  = rs.getString("last_name");
            custPhoneNum  = rs.getString("phone_number");
            custStreet    = rs.getString("street");
            custCity      = rs.getString("city");
            }


}catch (ClassNotFoundException e) {
        System.out.println("Driver not found");
    }
    catch (SQLException e) {
        e.printStackTrace();
    }finally {
try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
}
}


/**********************************************************************/
/** Function that checks if username that user wants is taken or not **/
/**********************************************************************/
static int checkUsername(){

Connection conn = null;  // Connection variable that will connect to DB
ResultSet rs    = null;  // Variable that holds the results of the query
Statement stmt  = null;  // Variable that is used to execute query

try{
// Connect to the DB
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB, USER, PW);
            
            // Check if the count of the username in DB
            // If it returns 1, then username already exists, if 0 then it doesn't
            String checkUsername = "SELECT COUNT(*) FROM customer WHERE username = '" + username + "';";
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery(checkUsername);
            
            // Return the count
            while(rs.next()){
            return rs.getInt(1);
            }

}catch (ClassNotFoundException e) {
        System.out.println("Driver not found");
    }
    catch (SQLException e) {
        e.printStackTrace();
    }finally {
try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
}

return 0;
}


/**********************************************************************/
/** Function that shows user all of their existing reservations they **/
/** made already (if any), and then the user is able to pick from    **/
/** that list of reservation numbers to view the reservation info of **/
/** that reservation such is customer and flight information	 **/
/**********************************************************************/
public static void searchReservations(){

Connection conn = null;  // Connection variable that will connect to DB
ResultSet rs    = null;  // Variable that holds the results of the query
Statement stmt  = null;  // Variable that is used to execute query

try{
// Connect to the DB
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB, USER, PW);
            
            // First query will select all existing reservations customer has
            String getReservations = "SELECT reservation_no FROM reservations WHERE customer_id = " + custID + ";";
            
            // Second query will join reservations table and flights table on flight number
            String resLookup;
            
            stmt = conn.createStatement();
            
            // If user has no reservations, check DB to see if they do and add them to array
            if(custReservations.isEmpty()){
            rs = stmt.executeQuery(getReservations);
while(rs.next()){
int res_no = rs.getInt("reservation_no");
custReservations.add(res_no);
}

// If the query returns no reservations, then the customer has none
// and will be prompted with a message that they don't and will be returned
// to the main menu
if(custReservations.isEmpty()){
System.out.println("You have no existing reservations. You will be returned to the main menu\n");
displayMenu();
callSwitch();
}

// Reset variables
rs   = null;
stmt = null;
}
            
            stmt = conn.createStatement();

            // Show reservation numbers to customer
System.out.print("List of existing reservations for " + custFirstName + ": ");
for(int i : custReservations)
System.out.print(i + " ");

System.out.print("\nSelect a reservation you would like to see info for: ");
resNum = scan.next();

// Get reservation info by joining
System.out.println("\nDisplaying reservation info for reservation number: " + resNum + "...");
resLookup = "select * from reservations as a " +
"join flights as b " +
"on a.flight_no = b.flight_no " +
"where a.reservation_no = " + resNum + ";";

rs = stmt.executeQuery(resLookup);

// Header of table
System.out.println("+--------------------------------------------------------------------------------" +
   "------------------------------------------------------------------------------------------+");
System.out.println("| Reservation # | Name                            | Flight Number | Route Number " +
   "| Airplane ID | Month | Day | Year | Depart From | Arrive To | Depart Time | Arrival Time |");
System.out.println("+---------------+---------------------------------+---------------+--------------" +
    "+-------------+-------+-----+------+-------------+-----------+-------------+--------------+");
while(rs.next()){
// Set variables for each field and format them for neatness
String res_num     = String.format("%" + -14 + "s", rs.getString("reservation_no"));
String name        = String.format("%" + -32 + "s", rs.getString("name"));
String flight_num  = String.format("%" + -14 + "s", rs.getString("flight_no"));
String route_num   = String.format("%" + -13 + "s", rs.getString("route_no"));
String plane_id    = String.format("%" + -12 + "s", rs.getString("airplane_id"));
String month       = String.format("%" + -6  + "s", rs.getString("travel_month"));   
String day         = String.format("%" + -4  + "s", rs.getString("travel_day")); 
String year        = String.format("%" + -5  + "s", rs.getString("travel_year"));
String depart      = String.format("%" + -12 + "s", rs.getString("airport_start"));
String arrive      = String.format("%" + -10 + "s", rs.getString("airport_finish"));
String depart_time = String.format("%" + -12 + "s", rs.getString("depart_time"));
String arrive_time = String.format("%" + -13 + "s", rs.getString("arrival_time"));

// Print out results in table form
System.out.println("| " + res_num     +
   "| " + name	      +
   "| " + flight_num  + 
   "| " + route_num   + 
   "| " + plane_id    + 
   "| " + month       +
   "| " + day         +
   "| " + year        +
   "| " + depart      +
   "| " + arrive      +
   "| " + depart_time +
   "| " + arrive_time +
   "| " );
}

// Table Footer
System.out.println("+--------------------------------------------------------------------------------" +
      "------------------------------------------------------------------------------------------+");

System.out.println("You will now be returned to the main menu.\n");

displayMenu();
callSwitch();

}catch (ClassNotFoundException e) {
        System.out.println("Driver not found");
    }
    catch (SQLException e) {
        e.printStackTrace();
    }finally {
try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
}
}

}
