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
                    break;
                case "custom easyselect":
                    customRun("testEasySelect");
                    break;
                case "custom select":
                    customRun("testSelect");
                    break;
                case "clear":
                    System.out.println("clearing memory");
                    dbEngine.clear();
                    break;
                case "change mode":
                    changeMode();
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
        evaluate("testInsert" + option);

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

            long timeInMS = (endTime - startTime) / 1_000_000;
            long totalMemoryUsedByFunction = (memoryUsedAfter - memoryUsedBefore) / KB;
            System.out.println(totalMemoryUsedByFunction + " KB used");
            System.out.println(timeInMS + "ms to run the test");
            System.out.println(queries.size() + " queries processed from " + filepath + file);
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
}