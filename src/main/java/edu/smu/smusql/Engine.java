package edu.smu.smusql;

import java.util.*;

public class Engine {

    private Map <String , Table> tables = new HashMap<>();
    private Parser parser = new Parser();
    public String executeSQL(String query) {
        String[] tokens = query.trim().split("\\s+");
        String command = tokens[0].toUpperCase();

        switch (command) {
            case "CREATE":
                return create(tokens);
            case "INSERT":
                return insert(tokens);
            case "SELECT":
                return select(tokens);
            case "UPDATE":
                return update(tokens);
            case "DELETE":
                return delete(tokens);
            default:
                return "ERROR: Unknown command";
        }
    }

    public String insert(String[] tokens) {
        //TODO
        return "not implemented";
    }
    public String delete(String[] tokens) {
        //TODO
        return "not implemented";
    }

    public String select(String[] tokens) {
        //TODO
        return "not implemented";
    }
    public String update(String[] tokens) {
        //TODO
        return "not implemented";
    }
    public String create(String[] tokens) {
        //TODO
        if (!tokens[1].equalsIgnoreCase("TABLE")) {
            return "ERROR: Invalid CREATE TABLE syntax";
        }

        String tableName = tokens[2];
        String insertQuery = parser.queryBetweenParentheses(tokens, 4);
        String[] columns = insertQuery.split(" ");
        Table toAdd = new Table(columns);
        tables.put(tableName , toAdd);
        return "Table " + tableName + " created";
    }

}
