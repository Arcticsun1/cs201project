package edu.smu.smusql;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLOutput;
import java.util.*;

public class Main {
    /*
     *  Main method for accessing the command line interface of the database engine.
     *  MODIFICATION OF THIS FILE IS NOT RECOMMENDED!
     */
    public static final String filepath = "src/main/resources/";
    public static final int KB = 1024;
    public static final String[] validModes = {"hashDylan"};
    static Engine dbEngine = new Engine();
    public static void main(String[] args) {
        dbEngine.setMode(validModes[0]);
        Scanner scanner = new Scanner(System.in);

        System.out.println("smuSQL Starter Code version 0.5");
        System.out.println("Have fun, and good luck!");

        while (true) {
            System.out.print("smusql> ");
            String query = scanner.nextLine().toLowerCase();
            switch (query) {
                case "exit":
                    break;
                case "custom evaluate":
                    customEvaluate();
                    System.gc();
                    break;
                case "custom easyselect":
                    customRun("easySelect/testEasySelect");
                    System.gc();
                    break;
                case "custom select":
                    customRun("select/testSelect");
                    System.gc();
                    break;
                case "clear":
                    System.out.println("clearing memory");
                    dbEngine.clear();
                    System.gc();
                    break;
                case "change mode":
                    changeMode();
                    break;
                case "custom delete":
                    System.out.println("reinitialising tables");
                    dbEngine.clear();
                    System.gc();
                    customNonIdempotent("delete/testDelete");

                case "custom easyupdate":
                    customNonIdempotent("easyUpdate/testEasyUpdate");
                    System.out.println("remember to clear and reset the tables before rerunning");
                    System.gc();
                    break;
                default:
                    System.out.println(dbEngine.executeSQL(query));
                    break;
            }
            if (query.equalsIgnoreCase("exit")) {
                break;
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
        evaluate("createTables");
        evaluate("insert/testInsert" + option);

    }
    public static void changeMode(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("which mode would you like to switch to?");
        for (int i = 0; i < validModes.length; i++) {
            System.out.println(i + " :" + validModes[i]);
        }
        int option = scanner.nextInt();
        if (option < 0 || option >= validModes.length) {
            System.out.println("option invalid");
        } else {
            dbEngine.setMode(validModes[option]);
        }
    }

    public static void evaluate(String file){
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(filepath + file + ".txt"));
            List <String> queries = new ArrayList<>();
            while (scanner.hasNextLine()){
                String query = scanner.nextLine();
                queries.add(query);
            }
            scanner.close();
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
            queries.add("");
            long timeInMS = (endTime - startTime) / 1_000_000;
            long totalMemoryUsedByFunction = (memoryUsedAfter - memoryUsedBefore) / KB;
            System.out.println(totalMemoryUsedByFunction + " KB used");
            System.out.println(timeInMS + "ms to run the test");
            System.out.println(queries.size() - 1 + " queries processed from " + filepath + file);
            queries.clear();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void customRun(String function){
        for (int i = 3 ; i >= 1 ; i--) {
            evaluate(function + i);
        }
    }
    public static void customNonIdempotent(String filename){
        for (int i = 3 ; i >= 1 ; i--){
            System.out.println("reinitialising data");
            dbEngine.clear();
            readFile("createTables");
            readFile("populate/populate2");
            readFile(filename + i);
        }
    }
    public static void readFile(String filename){
        try{
            Scanner scanner = new Scanner(new File(filepath + filename +".txt"));
            List <String> queries = new ArrayList<>();
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                queries.add(line);
            }
            scanner.close();
            Runtime runtime = Runtime.getRuntime();
            long totalMemoryBefore = runtime.totalMemory();
            long freeMemoryBefore = runtime.freeMemory();
            long memoryUsedBefore = totalMemoryBefore - freeMemoryBefore;
            System.out.println("file Read , executing test queries...");
            long startTime = System.nanoTime();
            for (String s : queries){
                dbEngine.executeSQL(s);
            }
            long endTime = System.nanoTime();

            long totalMemoryAfter = runtime.totalMemory();
            long freeMemoryAfter = runtime.freeMemory();
            long memoryUsedAfter = totalMemoryAfter - freeMemoryAfter;
            queries.add("");
            long timeInMS = (endTime - startTime) / 1_000_000;
            long totalMemoryUsedByFunction = (memoryUsedAfter - memoryUsedBefore) / KB;
            System.out.println(totalMemoryUsedByFunction + " KB used");
            System.out.println(timeInMS + "ms to run the test");
            System.out.println(queries.size() - 1 + " queries processed from " + filepath + filename);
            queries.clear();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}