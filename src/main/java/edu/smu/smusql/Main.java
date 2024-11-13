package edu.smu.smusql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    /*
     *  Main method for accessing the command line interface of the database engine.
     *  MODIFICATION OF THIS FILE IS NOT RECOMMENDED!
     */
    public static final int KB = 1024;
    public static final String[] validModes = {"hashDylan"};
    static Engine dbEngine = new Engine();
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("smuSQL Starter Code version 0.5");
        System.out.println("Have fun, and good luck!");

        while (true) {
            System.out.print("smusql> ");
            String query = scanner.nextLine();
            if (query.equalsIgnoreCase("exit")) {
                break;
            } else if (query.equalsIgnoreCase("custom evaluate")){
                customEvaluate();
            } else if (query.equalsIgnoreCase("clear")) {
                System.out.println("clearing memory");
                dbEngine.clear();
            } else if (query.equalsIgnoreCase("change mode")){
                System.out.println("which mode would you like to switch to?");
                for (int i = 0 ; i < validModes.length ; i++){
                    System.out.println(i + " :" + validModes[i]);
                }
                int option = scanner.nextInt();
                if (option < 0 || option >= validModes.length){
                    System.out.println("option invalid");
                }
                dbEngine.setMode(validModes[option]);
            }
            else{
                System.out.println(dbEngine.executeSQL(query));
            }


        }
        scanner.close();
    }


    /*
     *  Below is the code for auto-evaluating your work.
     *  DO NOT CHANGE ANYTHING BELOW THIS LINE!
     */
    public static void customEvaluate(){
        System.out.println("Size of query options");
        System.out.println("1: 100,000 queries");
        System.out.println("2: 10,000 queries");
        System.out.println("3: 2500 queries");
        int option = 0;
        Scanner scanner = new Scanner(System.in);
        try{
            option = Integer.parseInt(scanner.next());
            if (option > 3 || option <= 0){
                System.out.println("invalid option, exiting menu");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("invalid option, exiting evaluation");
            return;
        }
        //creating tables
        try {
            Scanner fileReader = new Scanner(new File("src/main/resources/createTables.txt"));
            while (fileReader.hasNextLine()){
                String line = fileReader.nextLine();
                dbEngine.executeSQL(line);
            }
            fileReader = new Scanner(new File("src/main/resources/testInsert" + option +".txt"));
            List <String> queries = new ArrayList<>();
            while (fileReader.hasNextLine()){
                String line = fileReader.nextLine();
                queries.add(line);
            }
            fileReader.close();
            Runtime runtime = Runtime.getRuntime();
            long totalMemoryBefore = runtime.totalMemory();
            long freeMemoryBefore = runtime.freeMemory();
            long memoryUsedBefore = totalMemoryBefore - freeMemoryBefore;

            long startTime = System.nanoTime();
            for (String s : queries){
                dbEngine.executeSQL(s);
            }
            long endTime = System.nanoTime();

            long totalMemoryAfter = runtime.totalMemory();
            long freeMemoryAfter = runtime.freeMemory();
            long memoryUsedAfter = totalMemoryAfter - freeMemoryAfter;

            long timeInMS = (endTime - startTime) / 1_000_000;
            long totalMemoryUsedByFunction = (memoryUsedAfter - memoryUsedBefore) / KB;
            System.out.println(totalMemoryUsedByFunction + " KB used");
            System.out.println(timeInMS + "ms to run the insert");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void operationEvaluate() {
        // Set the number of queries to execute per operation type
        int numberOfQueries = 100000;  // You can adjust this number based on your performance testing requirements

        // Create tables
        dbEngine.executeSQL("CREATE TABLE users (id, name, age, city)");
        dbEngine.executeSQL("CREATE TABLE products (id, name, price, category)");
        dbEngine.executeSQL("CREATE TABLE orders (id, user_id, product_id, quantity)");

        // Random data generator
        Random random = new Random();
        prepopulateTables(random);

        // Test INSERT performance
        long startTime = System.nanoTime();
        for (int i = 0; i < numberOfQueries; i++) {
            insertRandomData(random);
        }
        long endTime = System.nanoTime();
        double insertTime = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("INSERT operation took: " + insertTime + " seconds");

        // Test SELECT performance
        startTime = System.nanoTime();
        for (int i = 0; i < numberOfQueries; i++) {
            selectRandomData(random);
        }
        endTime = System.nanoTime();
        double selectTime = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("SELECT operation took: " + selectTime + " seconds");

        // Test UPDATE performance
        startTime = System.nanoTime();
        for (int i = 0; i < numberOfQueries; i++) {
            updateRandomData(random);
        }
        endTime = System.nanoTime();
        double updateTime = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("UPDATE operation took: " + updateTime + " seconds");

        // Test DELETE performance
        startTime = System.nanoTime();
        for (int i = 0; i < numberOfQueries; i++) {
            deleteRandomData(random);
        }
        endTime = System.nanoTime();
        double deleteTime = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("DELETE operation took: " + deleteTime + " seconds");

        // Report the total time
        double totalTime = insertTime + selectTime + updateTime + deleteTime;
        System.out.println("Total time for all operations: " + totalTime + " seconds");
    }

    private static void prepopulateTables(Random random) {
        System.out.println("Prepopulating users");
        // Insert initial users
        for (int i = 0; i < 50; i++) {
            String name = "User" + i;
            int age = 20 + (i % 41); // Ages between 20 and 60
            String city = getRandomCity(random);
            String insertCommand = String.format("INSERT INTO users VALUES (%d, '%s', %d, '%s')", i, name, age, city);
            dbEngine.executeSQL(insertCommand);
        }
        System.out.println("Prepopulating products");
        // Insert initial products
        for (int i = 0; i < 50; i++) {
            String productName = "Product" + i;
            double price = 10 + (i % 990); // Prices between $10 and $1000
            String category = getRandomCategory(random);
            String insertCommand = String.format("INSERT INTO products VALUES (%d, '%s', %.2f, '%s')", i, productName, price, category);
            dbEngine.executeSQL(insertCommand);
        }
        System.out.println("Prepopulating orders");
        // Insert initial orders
        for (int i = 0; i < 50; i++) {
            int user_id = random.nextInt(9999);
            int product_id = random.nextInt(9999);
            int quantity = random.nextInt(1, 100);
            String category = getRandomCategory(random);
            String insertCommand = String.format("INSERT INTO orders VALUES (%d, %d, %d, %d)", i, user_id, product_id, quantity);
            dbEngine.executeSQL(insertCommand);
        }
    }

    // Helper method to insert random data into users, products, or orders table
    private static void insertRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        switch (tableChoice) {
            case 0: // Insert into users table
                int id = random.nextInt(10000) + 10000;
                String name = "User" + id;
                int age = random.nextInt(60) + 20;
                String city = getRandomCity(random);
                String insertUserQuery = "INSERT INTO users VALUES (" + id + ", '" + name + "', " + age + ", '" + city + "')";
                dbEngine.executeSQL(insertUserQuery);
                break;
            case 1: // Insert into products table
                int productId = random.nextInt(1000) + 10000;
                String productName = "Product" + productId;
                double price = 50 + (random.nextDouble() * 1000);
                String category = getRandomCategory(random);
                String insertProductQuery = "INSERT INTO products VALUES (" + productId + ", '" + productName + "', " + price + ", '" + category + "')";
                dbEngine.executeSQL(insertProductQuery);
                break;
            case 2: // Insert into orders table
                int orderId = random.nextInt(10000) + 1;
                int userId = random.nextInt(10000) + 1;
                int productIdRef = random.nextInt(1000) + 1;
                int quantity = random.nextInt(10) + 1;
                String insertOrderQuery = "INSERT INTO orders VALUES (" + orderId + ", " + userId + ", " + productIdRef + ", " + quantity + ")";
                dbEngine.executeSQL(insertOrderQuery);
                break;
        }
    }

    // Helper method to randomly select data from tables
    private static void selectRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        String selectQuery;
        switch (tableChoice) {
            case 0:
                selectQuery = "SELECT * FROM users";
                break;
            case 1:
                selectQuery = "SELECT * FROM products";
                break;
            case 2:
                selectQuery = "SELECT * FROM orders";
                break;
            default:
                selectQuery = "SELECT * FROM users";
        }
        dbEngine.executeSQL(selectQuery);
    }

    // Helper method to update random data in the tables
    private static void updateRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        switch (tableChoice) {
            case 0: // Update users table
                int id = random.nextInt(10000) + 1;
                int newAge = random.nextInt(60) + 20;
                String updateUserQuery = "UPDATE users SET age = " + newAge + " WHERE id = " + id;
                dbEngine.executeSQL(updateUserQuery);
                break;
            case 1: // Update products table
                int productId = random.nextInt(1000) + 1;
                double newPrice = 50 + (random.nextDouble() * 1000);
                String updateProductQuery = "UPDATE products SET price = " + newPrice + " WHERE id = " + productId;
                dbEngine.executeSQL(updateProductQuery);
                break;
            case 2: // Update orders table
                int orderId = random.nextInt(10000) + 1;
                int newQuantity = random.nextInt(10) + 1;
                String updateOrderQuery = "UPDATE orders SET quantity = " + newQuantity + " WHERE id = " + orderId;
                dbEngine.executeSQL(updateOrderQuery);
                break;
        }
    }

    // Helper method to delete random data from tables
    private static void deleteRandomData(Random random) {
        int tableChoice = random.nextInt(3);
        switch (tableChoice) {
            case 0: // Delete from users table
                int userId = random.nextInt(10000) + 1;
                String deleteUserQuery = "DELETE FROM users WHERE id = " + userId;
                dbEngine.executeSQL(deleteUserQuery);
                break;
            case 1: // Delete from products table
                int productId = random.nextInt(1000) + 1;
                String deleteProductQuery = "DELETE FROM products WHERE id = " + productId;
                dbEngine.executeSQL(deleteProductQuery);
                break;
            case 2: // Delete from orders table
                int orderId = random.nextInt(10000) + 1;
                String deleteOrderQuery = "DELETE FROM orders WHERE id = " + orderId;
                dbEngine.executeSQL(deleteOrderQuery);
                break;
        }
    }

    // Helper method to execute a complex SELECT query with WHERE, AND, OR, >, <, LIKE
    private static void complexSelectQuery(Random random) {
        int tableChoice = random.nextInt(2);  // Complex queries only on users and products for now
        String complexSelectQuery;
        switch (tableChoice) {
            case 0: // Complex SELECT on users
                int minAge = random.nextInt(20) + 20;
                int maxAge = minAge + random.nextInt(30);
                String city = getRandomCity(random);
                complexSelectQuery = "SELECT * FROM users WHERE age > " + minAge + " AND age < " + maxAge;
                break;
            case 1: // Complex SELECT on products
                double minPrice = 50 + (random.nextDouble() * 200);
                double maxPrice = minPrice + random.nextDouble() * 500;
                complexSelectQuery = "SELECT * FROM products WHERE price > " + minPrice + " AND price < " + maxPrice;
                break;
            case 2: // Complex SELECT on products
                double minPrice2 = 50 + (random.nextDouble() * 200);
                String category = getRandomCategory(random);
                complexSelectQuery = "SELECT * FROM products WHERE price > " + minPrice2 + " AND category = " + category;
                break;
            default:
                complexSelectQuery = "SELECT * FROM users";
        }
        dbEngine.executeSQL(complexSelectQuery);
    }

    // Helper method to execute a complex UPDATE query with WHERE
    private static void complexUpdateQuery(Random random) {
        int tableChoice = random.nextInt(2);  // Complex updates only on users and products for now
        switch (tableChoice) {
            case 0: // Complex UPDATE on users
                int newAge = random.nextInt(60) + 20;
                String city = getRandomCity(random);
                String updateUserQuery = "UPDATE users SET age = " + newAge + " WHERE city = '" + city + "'";
                dbEngine.executeSQL(updateUserQuery);
                break;
            case 1: // Complex UPDATE on products
                double newPrice = 50 + (random.nextDouble() * 1000);
                String category = getRandomCategory(random);
                String updateProductQuery = "UPDATE products SET price = " + newPrice + " WHERE category = '" + category + "'";
                dbEngine.executeSQL(updateProductQuery);
                break;
        }
    }

    // Helper method to return a random city
    private static String getRandomCity(Random random) {
        String[] cities = {"New York", "Los Angeles", "Chicago", "Boston", "Miami", "Seattle", "Austin", "Dallas", "Atlanta", "Denver"};
        return cities[random.nextInt(cities.length)];
    }

    // Helper method to return a random category for products
    private static String getRandomCategory(Random random) {
        String[] categories = {"Electronics", "Appliances", "Clothing", "Furniture", "Toys", "Sports", "Books", "Beauty", "Garden"};
        return categories[random.nextInt(categories.length)];
    }
}