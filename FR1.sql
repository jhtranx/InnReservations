-- Lab 7: JDBC
-- etruon08
-- Nov 16, 2021

USE `etruon08`;
-- R1: Rooms/Rates
-- ...output a list of rooms to the user sorted by popularity. Include in the list: (A) Room popularity score: number of days the room has been occupied during the previous 180 days divided by 180 (round to two decimal places) (B) Next available check-in date. (C) Length (in days) of the most recent completed stay in the room.
-- drop table if exists hp_goods, hp_customers, hp_items, hp_receipts;
-- create table hp_goods as select * from BAKERY.goods;
-- create table hp_customers as select * from BAKERY.customers;
-- create table hp_items as select * from BAKERY.items;
-- create table hp_receipts as select * from BAKERY.receipts;

-- grant all on etruon08.hp_goods to hasty@'%';
-- grant all on etruon08.hp_customers to hasty@'%';
-- grant all on etruon08.hp_items to hasty@'%';
-- grant all on etruon08.hp_receipts to hasty@'%';

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

SELECT * FROM lab7_rooms;
SELECT * FROM lab7_reservations;


USE `etruon08`;
-- R2: Reservations
-- Produce a numbered list of rooms available for booking based on a user search (dates, room information, guest count)
SELECT *
FROM lab7_reservations

-- Popularity
with calcPopularity AS (
SELECT Room, 
SUM(DATEDIFF(
    CASE
        WHEN CheckOut > '2021-02-02' THEN '2021-02-02'
        ELSE CheckOut
    END,
    CASE
        WHEN CheckIn < '2020-12-25' THEN '2020-12-21'
        ELSE CheckIn
    END)) as days
FROM lab7_reservations
WHERE NOT (CheckIn >= '2020-12-25' OR CheckOut <= '2021-02-02')
GROUP BY Room
)

SELECT *
FROM lab7_rooms r
    INNER JOIN calcPopularity res ON res.Room = r.RoomCode
    
-- SELECT rooms.RoomCode, rooms.RoomName, SUM(DATEDIFF(CASE WHEN res.CheckOut > '2010-12-31' THEN '2010-12-31' ELSE res.CheckOut END, CASE WHEN res.CheckIn < '2010-01-01' THEN '2009-12-31' ELSE res.CheckIn END)) as days
-- FROM reservations res
--     INNER JOIN rooms ON res.Room = rooms.RoomCode
-- WHERE NOT (res.CheckIn >= '2010-12-31' OR res.CheckOut <= '2010-01-01')
-- GROUP BY rooms.RoomCode
-- ORDER BY days DESC


-- Next available check-in date

with allFutureRes AS (
    SELECT Room, CheckIn
    FROM lab7_reservations
    WHERE CheckIn > CURRENT_DATE
    GROUP BY Room, CheckIn
)

SELECT Room, MIN(CheckIn)
FROM allFutureRes
GROUP BY Room;


USE `etruon08`;
-- R5: Detailed Reservation Information
-- Present the user with a search prompt or form that allows them to enter any combination of the fields listed below (a blank entry should indicate "Any"). For all fields except  dates, permit partial values using SQL LIKE wildcards.
    (A) First name
    (B) Last name
    (C) A range of dates
    (D) Room code 
    (E) Reservation code
-- No attempt


USE `etruon08`;
-- R6: Revenue
-- When this option is selected, your system shall provide a month-by-month overview of revenue for an entire year.  For reservations that span multiple months, revenue must be computed based on individual days. For example a reservations that begins on October 30th and ends on November 2nd represents 2 nights of October revenue (Oct 30 and 31st) and 1 night of November revenue (Nov 1st).

Your system shall display a list of rooms, and, for each room, 13 columns: 12 columns showing dollar revenue for each month and a 13th column to display total year revenue for the room. There shall also be a "totals" row in the table, which contains column totals.  All amounts should be rounded to the nearest whole dollar.
-- No attempt


