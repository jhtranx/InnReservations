import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/*
JDBC setup (on Emily's labthreesixfive)

-- MySQL setup:
drop table if exists lab7_reservations, lab7_rooms;

grant all on etruon08.lab7_rooms to jtran179@'%';
grant all on etruon08.lab7_reservations to maprasad@'%';

CREATE TABLE IF NOT EXISTS lab7_rooms (
    RoomCode char(5) PRIMARY KEY,
    RoomName varchar(30) NOT NULL,
    Beds int(11) NOT NULL,
    bedType varchar(8) NOT NULL,
    maxOcc int(11) NOT NULL,
    basePrice DECIMAL(6,2) NOT NULL,
    decor varchar(20) NOT NULL,
    UNIQUE (RoomName)
);

CREATE TABLE IF NOT EXISTS lab7_reservations (
    CODE int(11) PRIMARY KEY,
    Room char(5) NOT NULL,
    CheckIn date NOT NULL,
    Checkout date NOT NULL,
    Rate DECIMAL(6,2) NOT NULL,
    LastName varchar(15) NOT NULL,
    FirstName varchar(15) NOT NULL,
    Adults int(11) NOT NULL,
    Kids int(11) NOT NULL,
    FOREIGN KEY (Room) REFERENCES lab7_rooms (RoomCode)
);
    
INSERT INTO lab7_rooms SELECT * FROM INN.rooms;
    
-- Use DATE_ADD to shift reservation dates to current year
INSERT INTO lab7_reservations SELECT CODE, Room,
    DATE_ADD(CheckIn, INTERVAL 132 MONTH),
    DATE_ADD(Checkout, INTERVAL 132 MONTH),
    Rate, LastName, FirstName, Adults, Kids FROM INN.reservations;

-- Shell init:
export CLASSPATH=$CLASSPATH:mysql-connector-java-8.0.16.jar:.
export HP_JDBC_URL=jdbc:mysql://db.labthreesixfive.com/winter2020?autoReconnect=true\&useSSL=false
export HP_JDBC_USER=jmustang
export HP_JDBC_PW=...
 */

public class InnReservations {
    public static void main(String[] args) {
		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("Welcome to our CSC 365 Inn Reservation System!");
			System.out.println("Please select an option: Rooms and Rates (1), Reservations (2), Reservation Changes (3), Reservation Cancellation (4), Detailed Reservation Information (5), Revenue (6) (0 = quit)");
			int demoNum = sc.nextInt(); 
			sc.nextLine();

			InnReservations ir = new InnReservations();
				// int demoNum = Integer.parseInt(args[0]);
			while (demoNum != 0) {
				switch (demoNum) {
				case 0: break;
				case 1: ir.fr1(); break;
				case 2: ir.fr2(sc); break;
				case 3: ir.fr3(); break;
				case 4: ir.fr4(); break;
				case 5: ir.fr5(); break;
                case 6: ir.fr6(); break;
				}
				System.out.println("Please select an option: Rooms and Rates (1), Reservations (2), Reservation Changes (3), Reservation Cancellation (4), Detailed Reservation Information (5), Revenue (6) (0 = quit)");
				demoNum = sc.nextInt(); 
				sc.nextLine();
			}
			sc.close();
				
		} catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		} catch (Exception e2) {
				System.err.println("Exception: " + e2.getMessage());
			}
    }

    // FR1 - Establish JDBC connection, execute DDL statement
    private void fr1() throws SQLException {

        System.out.println("FR1: Rooms and Rates: Rooms will be listed based on popularity from highest to lowest.\r\n");
        
	// Step 0: Load MySQL JDBC Driver
	// No longer required as of JDBC 2.0  / Java 6
	try{
	    Class.forName("com.mysql.jdbc.Driver");
	    System.out.println("MySQL JDBC Driver loaded");
	} catch (ClassNotFoundException ex) {
	    System.err.println("Unable to load JDBC Driver");
	    System.exit(-1);
	}

	// Step 1: Establish connection to RDBMS
	try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
							   System.getenv("HP_JDBC_USER"),
							   System.getenv("HP_JDBC_PW"))) {
	    // Step 2: Construct SQL statement
	    // String sql = "ALTER TABLE hp_goods ADD COLUMN AvailUntil DATE";
        String sql = "SELECT * FROM res";

	    // Step 3: (omitted in this example) Start transaction

	    // Step 4: Send SQL statement to DBMS
	    try (Statement stmt = conn.createStatement();
		 ResultSet rs = stmt.executeQuery(sql)) {

		// Step 5: Receive results
		while (rs.next()) {
		    String roomCode = rs.getString("RoomCode");
		    String roomName = rs.getString("RoomName");
		    Float popularity = rs.getFloat("Popularity");
            String nextAvail = rs.getString("nextAvail");
		    String checkOut = rs.getString("CheckOut");
		    int totDays = rs.getInt("totDays");
            System.out.format("%s %s %.2f %s %s %d \n", 
			                    roomCode, roomName, popularity, 
								nextAvail, checkOut, totDays);
		}
	    }

	    // Step 6: (omitted in this example) Commit or rollback transaction
	}
	// Step 7: Close connection (handled by try-with-resources syntax)
    }
    

    // Demo2 - Establish JDBC connection, execute SELECT query, read & print result
    private void fr2(Scanner fr2sc) throws SQLException {

        System.out.println("FR2: Reservations\r\n");
        
	// Step 1: Establish connection to RDBMS
	try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
							   System.getenv("HP_JDBC_USER"),
							   System.getenv("HP_JDBC_PW"))) {
	    // Step 2: Construct SQL statement

		// get user input
		System.out.println("Please enter your first name: ");
		String firstName = fr2sc.nextLine();
		System.out.println("Please enter your last name: ");
		String lastName = fr2sc.nextLine();
		System.out.println("Please enter your room code: (Enter 'Any' to indicate no preference)");
		String roomCode = fr2sc.nextLine();
		System.out.println("Please enter your desired bed type: (Enter 'Any' to indicate not preference)");
		String bedType = fr2sc.nextLine();
		System.out.println("Please enter your anticipated check-in date (yyyy-mm-dd): ");
		String checkIn = fr2sc.nextLine();
		System.out.println("Please enter your anticipated check-out date (yyyy-mm-dd): ");
		String checkOut = fr2sc.nextLine();
		System.out.println("Please enter the number of adults: ");
		Integer numAdults = fr2sc.nextInt();
		System.out.println("Please enter the number of children: ");
		Integer numChildren = fr2sc.nextInt();
		Integer totOcc = numAdults + numChildren;

		// Check if occ exceeds maxOcc
		String getMaxOcc = "SELECT MAX(maxOcc) AS maxOccAllowed FROM lab7_rooms";
		conn.setAutoCommit(false);
	    try (PreparedStatement maxOccQ = conn.prepareStatement(getMaxOcc))
		{
			ResultSet rs = maxOccQ.executeQuery(getMaxOcc);
			while(rs.next()) {
				int maxOcc = rs.getInt("maxOccAllowed");
				if (totOcc > maxOcc){
					System.out.println("No suitable rooms are available");
					return;
					// RETURN TO MAIN MENU
				}
			}
			
		}
		String[] allBedTypeArr = {"Queen", "King", "Double"};

		ArrayList<String> roomCodeArr = new ArrayList<String>();
		
		// account for situation where roomCode = Any
		if (roomCode.equals("Any"))
		{
			String availRoomsForAnyCode = 
			"SELECT * " +
			"FROM lab7_rooms " +
			"WHERE maxOcc >= ? ";

			conn.setAutoCommit(false);
			try (PreparedStatement anyCode = conn.prepareStatement(availRoomsForAnyCode))
			{
				anyCode.setInt(1, totOcc);

				ResultSet roomOpts = anyCode.executeQuery();

				while(roomOpts.next())
				{
					roomCodeArr.add(roomOpts.getString("RoomCode"));
				}
			}
			Random r=new Random();        
			int randomNumber = r.nextInt(roomCodeArr.size());
			roomCode = roomCodeArr.get(randomNumber);
		}

		if (bedType.equals("Any"))
		{
			Random r=new Random();        
			int randomNumber = r.nextInt(allBedTypeArr.length);
			bedType = allBedTypeArr[randomNumber];
		}

		// check if there is an existing conflict in reservation dates
		String checkResAvailSQL = 
		"SELECT COUNT(*) " +
		"FROM lab7_reservations " +
		"WHERE " +
		"(CheckIn >= ? AND CheckIn < ? AND Room = ?) " +
		"OR " +
		"(CheckOut > ? AND CheckOut <= ? AND Room = ?) " +
		"OR " +
		"(CheckIn <= ? AND CheckOut >= ? AND Room = ?)";	

		conn.setAutoCommit(false);
	    try (PreparedStatement pstmt = conn.prepareStatement(checkResAvailSQL))
		{
			pstmt.setString(1, checkIn);
			pstmt.setString(2, checkOut);
			pstmt.setString(3, roomCode);

			pstmt.setString(4, checkIn);
			pstmt.setString(5, checkOut);
			pstmt.setString(6, roomCode);

			pstmt.setString(7, checkIn);
			pstmt.setString(8, checkOut);
			pstmt.setString(9, roomCode);
			ResultSet rs = pstmt.executeQuery();

			int conflictCount = -1;
			while(rs.next())
			{
				conflictCount = rs.getInt("COUNT(*)");
			}
			conn.commit();

			ArrayList<LocalDate> options = new ArrayList<LocalDate>();
			ArrayList<LocalDate> optionsEndDate = new ArrayList<LocalDate>();
			Integer optionSelected = -1;

			if (conflictCount > 0) {
				// find 5 other options since a conflict has been encountered
				System.out.println("\nSorry, those dates are not available.");
				System.out.println("Here are some other date options we have instead: \n");
				
				// first, find the maxCheckOut date the room has been booked
				String getMaxCheckOutDate = 
				"with allResForCode AS ( " +
				"	SElECT * " +
				"	FROM lab7_reservations " +
				"	WHERE Room = ? " +
				"	ORDER BY CheckOut ASC " +
				") " + 
				"\n" +
				"SELECT MAX(CheckOut) as maxCheckOut " +
				"FROM allResForCode";

				LocalDate maxDateLD;
				String maxDate = null;

				conn.setAutoCommit(false);
				try (PreparedStatement maxCheckOut = conn.prepareStatement(getMaxCheckOutDate))
				{
					maxCheckOut.setString(1, roomCode);
					ResultSet ds = maxCheckOut.executeQuery();

					while(ds.next())
					{
						maxDate = ds.getString("maxCheckOut");
					}
				}
				conn.commit();
				// turn maxDate to a LocalDate obj
				maxDateLD = LocalDate.parse(maxDate);
				
				// check if checkIn == maxDate
				// this is the start of the while loop
				// 	loop until you hit 5 options
				LocalDate checkInLD = LocalDate.parse(checkIn);
				LocalDate checkOutLD = LocalDate.parse(checkOut);
				LocalDate newDate = LocalDate.now();
				long daysToAdd = ChronoUnit.DAYS.between(checkInLD, checkOutLD);
				Integer resCountNum = -1;

				LocalDate checkInA = null;
				LocalDate checkOutA = null;
				LocalDate checkInB = null;
				LocalDate checkOutB = null;
				Integer nightsAvail = 0;

				Integer optCount = 1;

				while (options.size() < 5)
				{
					String getResCountTilLast =
					"SELECT COUNT(*) " +
					"FROM lab7_reservations " +
					"WHERE Room = ? AND CheckIn > ? " +
					"ORDER BY CheckIn";
					conn.setAutoCommit(false);
					try (PreparedStatement resCount = conn.prepareStatement(getResCountTilLast))
					{
						resCount.setString(1, roomCode);
						resCount.setString(2, checkInLD.toString());

						ResultSet rc = resCount.executeQuery();
		
						while(rc.next())
						{
							resCountNum = rc.getInt("COUNT(*)");
						}
					}
					conn.commit();

					// check if there is atleast 2 more reservation before you hit maxDate
					if (resCountNum > 1)
					{
						// run check other dates
						String checkOtherDates = 
						"with allFutureRes AS ( " +
						"	SELECT * " +
						"	FROM lab7_reservations " +
						"	WHERE Room = ? AND CheckIn > ? " +
						"	ORDER BY CheckIn " +
						"), " +
						"\n" +
						"minCheckIn AS ( " +
						"	SELECT Room as room, MIN(CheckIn) as nextResDate " +
						"	FROM allFutureRes " +
						"), " +
						"\n" +
						"minRes AS ( " +
						"	SELECT r.Room, r.CheckIn, r.CheckOut " +
						"	FROM lab7_reservations r " +
						"		INNER JOIN minCheckIn m ON m.room = r.Room " +
						"	WHERE CheckIn = (SELECT nextResDate FROM minCheckIn) " +
						"), " +
						"\n" + 
						"nextMinCheckIn AS ( " +
						"	SELECT MIN(l.CheckIn) AS secondMin " +
						"	FROM lab7_reservations l " +
						"		INNER JOIN minRes n ON l.Room = n.Room " +
						"	WHERE l.CheckIn > (SELECT n.CheckIn FROM minRes n) " +
						"), " +
						"\n" +
						"nextRes AS ( " +
						"	SELECT r.Room, r.CheckIn, r.CheckOut " +
						"	FROM lab7_reservations r " +
						"		INNER JOIN minCheckIn m ON m.room = r.Room " +
						"	WHERE CheckIn = (SELECT secondMin FROM nextMinCheckIn) " +
						"), " +
						"\n" +
						"twoDatesRes AS ( " +
						"	SELECT m.CheckIn as checkInA, m.CheckOut as checkOutA, n.CheckIn as checkInB, n.CheckOut as checkOutB " +
						"	FROM minRes m, nextRes n " +
						") " +
						"\n" +
						"SELECT *, DATEDIFF(checkInB, checkOutA) as nightsAvail " +
						"FROM twoDatesRes;";
						conn.setAutoCommit(false);
						try (PreparedStatement pstmtB = conn.prepareStatement(checkOtherDates))
						{
							pstmtB.setString(1, roomCode);
							pstmtB.setString(2, checkInLD.toString());
							
							ResultSet rsB = pstmtB.executeQuery();

							while(rsB.next())
							{
								checkInA = LocalDate.parse(rsB.getString("checkInA"));
								checkOutA = LocalDate.parse(rsB.getString("checkOutA"));

								checkInB = LocalDate.parse(rsB.getString("checkInB"));
								checkOutB = LocalDate.parse(rsB.getString("checkOutB"));

								nightsAvail = rsB.getInt("nightsAvail");
							}
							if (nightsAvail > 0)
							{
								System.out.println(optCount + ") " + roomCode + " " + checkOutA + " " + checkInB);
								options.add(checkOutA);
								optionsEndDate.add(checkInB);
								optCount += 1;
							}
							checkInLD = checkOutB;
						}
						conn.commit();
					}
					else
					{
						// go to maxDate and do interval add method
						// if checkIn date OPTION == maxDate
						// if (checkInLD.isEqual(maxDateLD) || checkInLD.isAfter(maxDateLD)) 
						// {
							// create your own date options because SQL will return null exception (empty set)
							// to create your own date options:
							// 	take maxDate + interval = newDate
						newDate = maxDateLD.plusDays(daysToAdd);
						System.out.println(optCount + ") " + roomCode + " " + maxDateLD + " " + newDate);
						options.add(maxDateLD);
						optionsEndDate.add(newDate);

						maxDateLD = newDate;
						optCount += 1;
					}
					
				}
				System.out.println("\nPlease select a different option (1-5); Enter 0 to cancel current request): ");
				optionSelected = fr2sc.nextInt();
				fr2sc.nextLine();
				if (optionSelected == 0)
				{
					return;
				}				
			}
			else {
				System.out.println("This reservation date is available!");
				optionSelected = 1;
				options.add(LocalDate.parse(checkIn));
				optionsEndDate.add(LocalDate.parse(checkOut));
				// make reservation
			}
			conn.commit();

			String getRoomInfo = 
				"SELECT * " +
				"FROM lab7_rooms " +
				"WHERE RoomCode = ?";
			String roomName = null;
			String bedTypeForRoom = null;
			Double baseCost = (double) 0;
			Double totCost = (double) 0;
				

			conn.setAutoCommit(false);
			try (PreparedStatement roomInfo = conn.prepareStatement(getRoomInfo))
			{
				roomInfo.setString(1, roomCode);
				ResultSet ri = roomInfo.executeQuery();

				while(ri.next())
				{
					roomName = ri.getString("RoomName");
					bedTypeForRoom = ri.getString("bedType");
					baseCost = ri.getDouble("basePrice");
				}
			}
			conn.commit();

			LocalDate startDate = options.get(optionSelected-1);
			long finalInterval = ChronoUnit.DAYS.between(options.get(optionSelected-1), optionsEndDate.get(optionSelected-1));
			
			for (long i = 0; i < finalInterval; i++)
			{
				LocalDate curr = startDate.plusDays(i);
				String currDate = (curr.getDayOfWeek()).toString();
				if ((currDate).equals("SATURDAY") || (currDate).equals("SUNDAY"))
				{
					totCost = totCost + (baseCost*1.1);
				}
				else
				{
					totCost = totCost + baseCost;
				}
			}

			System.out.println("RESERVATION CONFIRMATION: \n");
			System.out.println("Name: " + firstName + " " + lastName);
			System.out.println("Room Code: " + roomCode);
			System.out.println("Room Name: " + roomName);
			System.out.println("Start Date: " + options.get(optionSelected-1));
			System.out.println("End Date: " + optionsEndDate.get(optionSelected-1));
			System.out.println("Bed Type: " + bedTypeForRoom);
			System.out.println("Number of Adults: " + numAdults);
			System.out.println("Number of Children: " + numChildren);
			System.out.format("Total Cost: $%.2f\n", totCost);

			// System.out.println(roomCode + " " + options.get(optionSelected-1) + " " + optionsEndDate.get(optionSelected-1));
			System.out.println("Please confirm your reservation (0 = No, 1 = Yes): ");
			Integer yesOrNo = fr2sc.nextInt();
			fr2sc.nextLine();

			if (yesOrNo == 0)
			{
				System.out.println("Goodbye\n");
				return;
			}
			else if (yesOrNo == 1)
			{
				//make reservation
				Integer code = -1;

				String getMaxCode =
				"SELECT MAX(Code) as maxCode FROM lab7_reservations;";
				conn.setAutoCommit(false);
				try (PreparedStatement maxCodeStmt = conn.prepareStatement(getMaxCode))
				{
					ResultSet mc = maxCodeStmt.executeQuery();

					while(mc.next())
					{
						code = mc.getInt("maxCode");
					}
				}
				conn.commit();
				
				code = code + 1;
				String makeRes = 
				"INSERT INTO lab7_reservations (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids) VALUES (" +
				code + ", " + "'" + roomCode + "', " + "'" + (options.get(optionSelected-1)).toString() + "', '" + (optionsEndDate.get(optionSelected-1)).toString() + "', " +
				baseCost + ", '" + lastName + "', '" + firstName + "', " + numAdults + ", " + numChildren + ");";

				//"INSERT INTO goods (GId, Food, Flavor, Price) VALUES ('51-BLU', 'Danish', 'Blueberry', 1.15);";

				try (PreparedStatement makeResStmt = conn.prepareStatement(makeRes)) 
				{
					makeResStmt.executeUpdate();
					conn.commit();
				} catch (SQLException e) {
					conn.rollback();
					System.out.println(e.getMessage());
				}
				System.out.println("Your reservation has been confirmed! Thank you!\n");
			}
		}
		catch (SQLException e) {
			conn.rollback();

		// Step 5: Receive results
		// while (rs.next()) {
		//     String flavor = rs.getString("Flavor");
		//     String food = rs.getString("Food");
		//     float price = rs.getFloat("price");
		//     System.out.format("%s %s ($%.2f) %n", flavor, food, price);
		// }
	    }
	}

	    // Step 6: (omitted in this example) Commit or rollback transaction
	}
	// Step 7: Close connection (handled by try-with-resources syntax)


    // Demo3 - Establish JDBC connection, execute DML query (UPDATE)
    // -------------------------------------------
    // Never (ever) write database code like this!
    // -------------------------------------------
    private void fr3() throws SQLException {

    	System.out.println("FR3: Make changes to an existing reservation.\r\n");
        
		// Step 1: Establish connection to RDBMS
		try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
								System.getenv("HP_JDBC_USER"),
								System.getenv("HP_JDBC_PW"))) {
			// Step 2: Construct SQL statement
			Scanner scanner = new Scanner(System.in).useDelimiter("\n");
			System.out.print("Enter a reservation code: ");
			Integer resCode = scanner.nextInt();
			String checkExistsSQL = "SELECT COUNT(*) FROM etruon08.lab7_reservations WHERE CODE = ?";
			conn.setAutoCommit(false);
			try (PreparedStatement pstmt = conn.prepareStatement(checkExistsSQL)) 
			{
				// Step 4: Send SQL statement to DBMS
				pstmt.setInt(1, resCode);
				ResultSet rs = pstmt.executeQuery();
				while(rs.next()) {
					int resExists = rs.getInt("COUNT(*)");
					if (resExists == 0) {
						System.out.println("Reservation does not exist.");
					}
					else {
						System.out.print("Update first name (1), last name (2), begin date (3), end date (4), number of children (5), or number of adults (6) of reservation? (0 to quit) ");
						Integer updateOption = scanner.nextInt();
						while (updateOption != 0) {
							if (updateOption == 1) {
								System.out.print("Enter new first name ('no change' if no change desired): ");
								String newFirst = scanner.next();
								if(!newFirst.equals("no change")) {
									String updateFirst = "UPDATE etruon08.lab7_reservations SET FirstName = ? WHERE CODE = ?";
									try (PreparedStatement pstmtFirst = conn.prepareStatement(updateFirst)) {
										pstmtFirst.setString(1, newFirst);
										pstmtFirst.setInt(2, resCode);
										pstmtFirst.executeUpdate();
										System.out.format("Updated Reservation #%d first name to %s%n", resCode, newFirst);
										conn.commit();
									} catch (SQLException e) {
										conn.rollback();
										System.out.println(e.getMessage());
									}
								}
								else {
									System.out.format("Reservation #%d first name not updated%n", resCode);
								}
							}
							if (updateOption == 2) {
								System.out.print("Enter new last name ('no change' if no change desired): ");
								String newLast = scanner.next();
								if (!newLast.equals("no change")) {
									String updateLast = "UPDATE etruon08.lab7_reservations SET LastName = ? WHERE CODE = ?";
									try (PreparedStatement pstmtLast = conn.prepareStatement(updateLast)) {
										pstmtLast.setString(1, newLast);
										pstmtLast.setInt(2, resCode);
										pstmtLast.executeUpdate();
										System.out.format("Updated Reservation #%d last name to %s%n", resCode, newLast);
										conn.commit();
									} catch (SQLException e) {
										conn.rollback();
										System.out.println(e.getMessage());
									}
								}
								else {
									System.out.format("Reservation #%d last name not updated%n", resCode);
								}
							}
							if (updateOption == 3) {
								System.out.print("Enter new check-in date (format 'yyyy-mm-dd' or 'no change' if no change desired): ");
								String newDateStr = scanner.next();
								if (!newDateStr.equals("no change")) {
									LocalDate newCheckin = LocalDate.parse(newDateStr);
									String RoomCode = "";
									String getRoom = "SELECT Room FROM etruon08.lab7_reservations WHERE CODE = ?";
									String findConflicts = "SELECT COUNT(*) FROM etruon08.lab7_reservations WHERE ROOM = ? and CheckIn <= ? and Checkout > ? and CODE <> ?";
									try(PreparedStatement roomStmt = conn.prepareStatement(getRoom)) {
										roomStmt.setInt(1, resCode);
										ResultSet rs2 = roomStmt.executeQuery();
										while(rs2.next()) {
											RoomCode = rs2.getString("Room");
										}
									} catch (SQLException e) {
										System.out.println(e.getMessage());
									}
									try(PreparedStatement validateCheckin = conn.prepareStatement(findConflicts)) {
										validateCheckin.setString(1, RoomCode);
										validateCheckin.setDate(2, java.sql.Date.valueOf(newCheckin));
										validateCheckin.setDate(3, java.sql.Date.valueOf(newCheckin));
										validateCheckin.setInt(4, resCode);
										ResultSet rs3 = validateCheckin.executeQuery();
										while(rs3.next()) {
											//String conflictRoom = rs3.getString("Room");
											//String conflictCode = rs3.getString("CODE");
											//System.out.print(conflictRoom + " " + conflictCode);
											Integer conflictCount = rs3.getInt("COUNT(*)");
											//System.out.println(conflictCount);
											if (conflictCount > 0) {
												System.out.println("Date conflicts with other reservations, please select a different date.");
											}
											else {
												String updateCheckin = "UPDATE etruon08.lab7_reservations SET CheckIn = ? WHERE CODE = ?";
												try (PreparedStatement pstmtCheckin = conn.prepareStatement(updateCheckin)) {
													pstmtCheckin.setDate(1, java.sql.Date.valueOf(newCheckin));
													pstmtCheckin.setInt(2, resCode);
													pstmtCheckin.executeUpdate();
													System.out.format("Updated Reservation #%d checkin date to %s%n", resCode, newDateStr);
													conn.commit();
												} catch (SQLException e) {
													conn.rollback();
													System.out.println(e.getMessage());
												}
											}
										}
									} catch (SQLException e) {
										System.out.println(e.getMessage());
									}

								}
								else {
									System.out.format("Reservation #%d checkin date not updated%n", resCode);
								}
							}
							if (updateOption == 4) {
								System.out.print("Enter new check-out date (format 'yyyy-mm-dd' or 'no change' if no change desired): ");
								String newDateStr = scanner.next();
								if (!newDateStr.equals("no change")) {
									LocalDate newCheckout = LocalDate.parse(newDateStr);
									String RoomCode = "";
									String getRoom = "SELECT Room FROM etruon08.lab7_reservations WHERE CODE = ?";
									String findConflicts = "SELECT COUNT(*) FROM etruon08.lab7_reservations WHERE ROOM = ? and CheckIn < ? and Checkout >= ? and CODE <> ?";
									try(PreparedStatement roomStmt = conn.prepareStatement(getRoom)) {
										roomStmt.setInt(1, resCode);
										ResultSet rs2 = roomStmt.executeQuery();
										while(rs2.next()) {
											RoomCode = rs2.getString("Room");
										}
									} catch (SQLException e) {
										System.out.println(e.getMessage());
									}
									try(PreparedStatement validateCheckout = conn.prepareStatement(findConflicts)) {
										validateCheckout.setString(1, RoomCode);
										validateCheckout.setDate(2, java.sql.Date.valueOf(newCheckout));
										validateCheckout.setDate(3, java.sql.Date.valueOf(newCheckout));
										validateCheckout.setInt(4, resCode);
										ResultSet rs3 = validateCheckout.executeQuery();
										while(rs3.next()) {
											//String conflictRoom = rs3.getString("Room");
											//String conflictCode = rs3.getString("CODE");
											//System.out.print(conflictRoom + " " + conflictCode);
											Integer conflictCount = rs3.getInt("COUNT(*)");
											//System.out.println(conflictCount);
											if (conflictCount > 0) {
												System.out.println("Date conflicts with other reservations, please select a different date.");
											}
											else {
												String updateCheckout = "UPDATE etruon08.lab7_reservations SET Checkout = ? WHERE CODE = ?";
												try (PreparedStatement pstmtCheckout = conn.prepareStatement(updateCheckout)) {
													pstmtCheckout.setDate(1, java.sql.Date.valueOf(newCheckout));
													pstmtCheckout.setInt(2, resCode);
													pstmtCheckout.executeUpdate();
													System.out.format("Updated Reservation #%d checkout date to %s%n", resCode, newDateStr);
													conn.commit();
												} catch (SQLException e) {
													conn.rollback();
													System.out.println(e.getMessage());
												}
											}
										}
									} catch (SQLException e) {
										System.out.println(e.getMessage());
									}

								}
								else {
									System.out.format("Reservation #%d checkout date not updated%n", resCode);
								}
							}
							if (updateOption == 5) {
								System.out.print("Enter new number of children ('no change' if no change desired): ");
								String NumChildrenStr = scanner.next();
								if(!NumChildrenStr.equals("no change")) {
									int newNumChildren = Integer.parseInt(NumChildrenStr);
									String updateNumChildren = "UPDATE etruon08.lab7_reservations SET Kids = ? WHERE CODE = ?";
									try (PreparedStatement pstmtNC = conn.prepareStatement(updateNumChildren)) {
										pstmtNC.setInt(1, newNumChildren);
										pstmtNC.setInt(2, resCode);
										pstmtNC.executeUpdate();
										System.out.format("Updated Reservation #%d number of children to %s%n", resCode, newNumChildren);
										conn.commit();
									} catch (SQLException e) {
										conn.rollback();
										System.out.println(e.getMessage());
									}
								}
								else {
									System.out.format("Reservation #%d number of children not updated%n", resCode);
								}
							}
							if (updateOption == 6) {
								System.out.print("Enter new number of adults ('no change' if no change desired): ");
								String NumAdultsStr = scanner.next();
								if(!NumAdultsStr.equals("no change")) {
									int newNumAdults = Integer.parseInt(NumAdultsStr);
									String updateNumAdults = "UPDATE etruon08.lab7_reservations SET Adults = ? WHERE CODE = ?";
									try (PreparedStatement pstmtNA = conn.prepareStatement(updateNumAdults)) {
										pstmtNA.setInt(1, newNumAdults);
										pstmtNA.setInt(2, resCode);
										pstmtNA.executeUpdate();
										System.out.format("Updated Reservation #%d number of adults to %s%n", resCode, newNumAdults);
										conn.commit();
									} catch (SQLException e) {
										conn.rollback();
										System.out.println(e.getMessage());
									}
								}
								else {
									System.out.format("Reservation #%d number of adults not updated%n", resCode);
								}
							}
							System.out.print("Update first name (1), last name (2), begin date (3), end date (4), number of children (5), or number of adults (6) of reservation? (0 to quit) ");
							updateOption = scanner.nextInt();
						}
					}
				}
				// Step 5: Handle results
				//System.out.format("Updated %d records for %s pastries%n", rowCount, flavor);
				// Step 6: Commit or rollback transaction
				conn.commit();
			} catch (SQLException e) {
				conn.rollback();
			}
		}
		// Step 7: Close connection (handled implcitly by try-with-resources syntax)
    }

    // Demo4 - Establish JDBC connection, execute DML query (UPDATE) using PreparedStatement / transaction    
    private void fr4() throws SQLException {

        System.out.println("FR4: Cancel an existing reservation.\r\n");
        
		// Step 1: Establish connection to RDBMS
		try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
								System.getenv("HP_JDBC_USER"),
								System.getenv("HP_JDBC_PW"))) {
			// Step 2: Construct SQL statement
				Scanner scanner = new Scanner(System.in).useDelimiter("\n");
				System.out.print("Enter a reservation code: ");
				Integer resCode = scanner.nextInt();
				String checkExistsSQL = "SELECT COUNT(*) FROM etruon08.lab7_reservations WHERE CODE = ?";
				conn.setAutoCommit(false);
				try (PreparedStatement pstmt = conn.prepareStatement(checkExistsSQL)) 
				{
					// Step 4: Send SQL statement to DBMS
					pstmt.setInt(1, resCode);
					ResultSet rs = pstmt.executeQuery();
					while(rs.next()) {
						int resExists = rs.getInt("COUNT(*)");
						if (resExists == 0) {
							System.out.println("Reservation does not exist.");
						}
						else {
							System.out.format("Are you sure you want to delete Reservation #%d? (y/n) ", resCode);
							String userResp = scanner.next();
							if (userResp.equals("n")) {
								break;
							}
							else {
								String deleteRes = "DELETE FROM etruon08.lab7_reservations WHERE CODE = ?";
								try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteRes)) {
									pstmtDelete.setInt(1, resCode);
									pstmtDelete.executeUpdate();
									conn.commit();
									System.out.format("Reservation #%d successfully deleted.%n", resCode);
								} catch (SQLException e) {
									System.out.println(e.getMessage());
									conn.rollback();
								}
							}
						}
					}
					conn.commit();
				} catch (SQLException e) {
					System.out.println(e.getMessage());
					conn.rollback();
				}
		}
	// Step 7: Close connection (handled implcitly by try-with-resources syntax)
    }



    // FR5: Detailed Reservation Information
    private void fr5() throws SQLException {

        System.out.println("FR5: Present a search prompt \r\n");
        
        // Step 1: Establish connection to RDBMS
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                                System.getenv("HP_JDBC_USER"),
                                System.getenv("HP_JDBC_PW"))) {

            List<String> headers = Arrays.asList("FirstName","LastName","CheckIn","CheckOut","Room","Code");
            List<String> inputs = new ArrayList<String>();

            Scanner scanner = new Scanner(System.in);

            // Step 2: Asking the user for query input
            System.out.print("Filter by first name (or 'Any'): ");
            String fName = scanner.nextLine();
            inputs.add(fName);

            System.out.print("Filter by last name (or 'Any'): ");
            String lName = scanner.nextLine();
            inputs.add(lName);

            System.out.print("Filter by check in (or 'Any'): ");
            String checkIn = scanner.nextLine();
            inputs.add(checkIn);

            System.out.print("Filter by check out (or 'Any'): ");
            String checkOut = scanner.nextLine();
            inputs.add(checkOut);

            System.out.print("Filter by room (or 'Any'): ");
            String room = scanner.nextLine();
            inputs.add(room);

            System.out.print("Filter by code (or 'Any'): ");
            String code = scanner.nextLine();
            inputs.add(code);
            

            List<String> params = new ArrayList<String>();
            StringBuilder sb = new StringBuilder("SELECT * FROM lab7_reservations WHERE ");

            int firstFlag = 0;
            for (int i = 0; i < inputs.size(); i++) {

                String curr = inputs.get(i);
                String header = headers.get(i);

                if (!"any".equalsIgnoreCase(curr)) {
                    if (firstFlag != 0) {
                        sb.append(" AND ");
                    }
                    if (curr.indexOf('%') != -1) {
                        sb.append(header + " LIKE ?");
                    }
                    else {
                        sb.append(header + " = ?");
                    }

                    params.add(curr);
                    firstFlag = 1;
                }
            }
            System.out.println(sb);
                                
            try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
                int i = 1;
                for (Object p : params) {
                    pstmt.setObject(i++, p);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("Result(s) Found:");
                    int matchCount = 0;
                    while (rs.next()) {
                        System.out.format("%s %s (%s, %s) %s %s (%d, %d) %n", 
                            rs.getString("FirstName"), rs.getString("LastName"), rs.getDate("CheckIn"),
                            rs.getDate("CheckOut"), rs.getString("CODE"), rs.getString("Room"), 
                            rs.getInt("Adults"), rs.getInt("Kids"));
                        matchCount++;
                    }
                    System.out.format("----------------------%nFound %d match%s %n", matchCount, matchCount == 1 ? "" : "es");
                }
            }
        }
    }

    private static void fr6() throws SQLException
    {
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                                System.getenv("HP_JDBC_USER"),
                                System.getenv("HP_JDBC_PW"))) {

            System.out.println("FR6: Revenue \r\n");

            String january, february, march, april, may, june, july, august,
                september, october, november, december, annual, room;
            
            january = february = march = april = may = june = july = august
                = september = october = november = december = annual = room = "";
                
            String sql = "with revTable as (SELECT Room," + 
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 1 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as January, " +
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 2 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as February," +
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 3 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as March, " +
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 4 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as April, " +
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 5 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as May, " +
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 6 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as June, " +
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 7 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as July, " +
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 8 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as August, " +
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 9 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as September, " +
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 10 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as October, " +
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 11 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as November, " +
                "ROUND(SUM(CASE WHEN MONTH(Checkout) = 12 THEN DATEDIFF(Checkout, CheckIn) * rate else 0 end), 0) as December, " +
                "ROUND(SUM(DATEDIFF(Checkout, Checkin) * rate), 0) as Annual " +
            "FROM lab7_reservations GROUP BY Room ) " +
            "SELECT Room, January, February, March, April, May, June, July, August, September, October, November, December, Annual " +
            "FROM revTable UNION " +
            "SELECT 'Total', SUM(January), SUM(February), SUM(March), SUM(April), SUM(May), SUM(June), SUM(July), SUM(August)," +
                "SUM(September), SUM(October), SUM(November), SUM(December), SUM(Annual) FROM revTable;";

            try (Statement stm = conn.createStatement())
            {
                ResultSet rs = stm.executeQuery(sql);

                System.out.format("%-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s \n\n", 
                    "Room", "Jan.", "Feb.", "Mar.", "Apr.", "May", "Jun.", "Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec.", "Annual");

                while (rs.next())
                {
                    january = rs.getString("January");
                    february = rs.getString("February");
                    march = rs.getString("March");
                    april = rs.getString("April");
                    may = rs.getString("May");
                    june = rs.getString("June");
                    july = rs.getString("July");
                    august = rs.getString("August");
                    september = rs.getString("September");
                    october = rs.getString("October");
                    november = rs.getString("November");
                    december = rs.getString("December");
                    room = rs.getString("Room");
                    annual = rs.getString("Annual");

                    System.out.format("%-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-8s\n", 
                        room , january, february, march, april, may, june, july, august, 
                        september, october, november, december, annual);
                }
            }
        }
    }
}
