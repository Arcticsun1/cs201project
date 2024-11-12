package edu.smu.smusql;

import edu.smu.smusql.table.HashTable;
import edu.smu.smusql.table.Table;

import java.util.*;

public class Engine {

    private Map<String, Table> tables = new HashMap<>();
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
        if (!tokens[1].equalsIgnoreCase("into") || !tokens[3].equalsIgnoreCase("VALUES")) {
            return "ERROR: Invalid Insert Command";
        }
        String tableName = tokens[2];
        if (!tables.containsKey(tableName)) {
            return "ERROR: Table \"" + tableName + "\" not found";
        }

        String insertQuery = parser.queryBetweenParentheses(tokens, 4);
        String[] values = insertQuery.split(" ");
        Table toInsert = tables.get(tableName);

        if (values.length != toInsert.getNumCols()) {
            return "Invalid number of columns, expected:" + toInsert.getNumCols() + " given:" + values.length;
        }
        if (toInsert.getRows().containsKey(values[0])) {
            return "An entry with id:" + values[0] + " already exists in table:" + tableName;
        }

        toInsert.insert(values);
        return "Row inserted into:" + tableName;
    }

    public String delete(String[] tokens) {
        //TODO
        return "not implemented";
    }

    public String select(String[] tokens) {
        //TODO
        if (!tokens[1].equals("*") || !tokens[2].toUpperCase().equals("FROM")) {
            return "ERROR: Invalid SELECT syntax";
        }
        String tableName = tokens[3];
        if (!tables.containsKey(tableName)) {
            return "No such Table: " + tableName;
        }
        Table table = tables.get(tableName);
        Map<String, ArrayList<String>> rows = table.getRows();

        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", table.getColumns())).append("\n");

        for (String r : rows.keySet()){
            Collection <String> values = rows.get(r);
            for (String v : values){
                result.append(v).append("\t");
            }
            result.append("\r\n");
        }
        return result.toString();
    }

    public String update(String[] tokens) {
        //TODO
        return "not implemented";
    }

    public String create(String[] tokens) {
        if (!tokens[1].equalsIgnoreCase("TABLE")) {
            return "ERROR: Invalid CREATE TABLE syntax";
        }
        String tableName = tokens[2];
        String insertQuery = parser.queryBetweenParentheses(tokens, 3);
        if (insertQuery.isBlank()){
            return "Invalid create query, there are no column names";
        }
        String[] columns = insertQuery.split(" ");
        Table toAdd = new HashTable(columns);
        tables.put(tableName, toAdd);
        return "Table " + tableName + " created";
    }


    public List<String[]> whereConditions(String[] arguments, int startingIndex) {
        List<String[]> result = new ArrayList<>();
        if (arguments.length <= startingIndex || !arguments[startingIndex].equalsIgnoreCase("WHERE")) {
            return new ArrayList<>();
        }

        for (int i = startingIndex + 1; i < arguments.length; i++) {
            if (arguments[i].equalsIgnoreCase("AND") || arguments[i].equalsIgnoreCase("OR")) {
                // Add AND/OR conditions
                result.add(new String[]{arguments[i].toUpperCase(), null, null, null});
            } else if (parser.isOperator(arguments[i])) {
                // Add condition with operator (column, operator, value)
                String column = arguments[i - 1];
                String operator = arguments[i];
                String value = arguments[i + 1];
                result.add(new String[]{null, column, operator, value});
                i += 1; // Skip the value since it has been processed
            }
        }
        return result;
    }
}
