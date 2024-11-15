package edu.smu.smusql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

public class Engine {

    private HashMap<String, Table> tables = new HashMap<>();

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
        if (tokens.length < 4 || !tokens[1].equalsIgnoreCase("INTO")) {
            return "ERROR: Invalid INSERT INTO syntax";
        }

        String tableName = tokens[2];
        Table table = tables.get(tableName);
        if (table == null) {
            return "ERROR: Table " + tableName + " does not exist.";
        }

        // Extract values between parentheses
        String valueList = queryBetweenParentheses(tokens, 3);
        if (valueList.isEmpty()) {
            return "ERROR: No values provided for INSERT";
        }

        // Handle potential commas within values (e.g., strings with spaces)
        List<String> values = parseValues(valueList);
        List<String> columns = table.getColumns();

        if (values.size() != columns.size()) {
            return "ERROR: Column count doesn't match value count";
        }

        Map<String, String> row = new CustomHashMap();
        for (int i = 0; i < columns.size(); i++) {
            // Remove quotes from string values
            String value = values.get(i).replaceAll("^'(.*)'$", "$1");
            row.put(columns.get(i), value);
        }

        String key = row.get(columns.get(0));

        table.insert(key, row);
        return "1 row inserted into " + tableName + ".";
    }





    public String delete(String[] tokens) {
        if (tokens.length < 3 || !tokens[1].equalsIgnoreCase("FROM")) {
            return "ERROR: Invalid DELETE syntax";
        }
    
        String tableName = tokens[2];
        Table table = tables.get(tableName);
        if (table == null) {
            return "ERROR: Table " + tableName + " does not exist.";
        }

        List<String> columnlist = table.getColumns();
    
        // Parse WHERE clause if it exists
        List<String[]> whereConditions = new ArrayList<>();
        if (tokens.length > 3 && tokens[3].equalsIgnoreCase("WHERE")) {
            whereConditions = parseWhereClause(Arrays.copyOfRange(tokens, 4, tokens.length));
        }

        //result for where condition that only have the primary key (using the redblacktree to find)
        if (whereConditions.size() == 1){
            
            String[] condition = whereConditions.get(0);
            if (condition[1].equalsIgnoreCase(columnlist.get(0)) && condition[2].equals("=")) {

                String key = condition[3];
                table.delete(key);
                return "1 row(s) deleted from " + tableName + ".";
            }
        }
    
        List<Map<String, String>> rows = table.getAllRows();
        int deletedCount = 0;
    
        // If there are conditions, evaluate them
        if (!whereConditions.isEmpty()) {
            for (Map<String, String> row : rows) {
                boolean match = evaluateWhereConditions(row, whereConditions);

                if (match) {
                    String key = row.get(columnlist.get(0));
                    table.delete(key);
                    deletedCount++;
                }
            }
        } else {
            // If no WHERE clause, delete all rows
            deletedCount = rows.size();
            rows.clear(); // Ensure the rows are cleared from the internal structure
        }
    
        if (deletedCount > 0) {
            return deletedCount + " row(s) deleted from " + tableName + ".";
        } else {
            return "No rows match the condition in the DELETE statement.";
        }
    }





    // public String select(String[] tokens) {
    //     if (tokens.length < 4 || !tokens[1].equals("*") || !tokens[2].equalsIgnoreCase("FROM")) {
    //         return "ERROR: Invalid SELECT syntax";
    //     }

    //     String tableName = tokens[3];
    //     Table table = tables.get(tableName);
    //     if (table == null) {
    //         return "ERROR: Table " + tableName + " does not exist.";
    //     }

    //     List<Map<String, String>> rows = table.getAllRows();

    //     List<String> columns = table.getColumns();

    //     // Check for WHERE clause
    //     List<String[]> whereConditions = new ArrayList<>();
    //     if (tokens.length > 4 && tokens[4].equalsIgnoreCase("WHERE")) {
    //         whereConditions = parseWhereClause(Arrays.copyOfRange(tokens, 5, tokens.length));
    //     }

    //     StringBuilder result = new StringBuilder();
    //     result.append(String.join("\t", columns)).append("\n"); // Header


    //     //result for where condition that only have the primary key (using the redblacktree to find)
    //     if (whereConditions.size() == 1){
            
    //         String[] condition = whereConditions.get(0);
    //         if (condition[1].equalsIgnoreCase(columns.get(0)) && condition[2].equals("=")) {

    //             String key = condition[3];
    //             String value = table.get(key).toString();
    //             Map<String,String> value_map = parseValueToMap(value);

    //             List<String> rowValues = new ArrayList<>();
    //             for (String col : columns) {
    //                 rowValues.add(value_map.getOrDefault(col, "NULL"));
    //             }
    //             result.append(String.join("\t", rowValues)).append("\n");
    //             return result.toString();
    //         }
    //     }

    //     //result for where condition that doesnt have primary key  
    //     if (!whereConditions.isEmpty()) {
    //         for (Map<String, String> row : rows) {
    //             boolean match = evaluateWhereConditions(row, whereConditions);

    //             if (match) {
    //                 List<String> rowValues = new ArrayList<>();
    //                 for (String col : columns) {
    //                     rowValues.add(row.getOrDefault(col, "NULL"));
    //                 }
    //                 result.append(String.join("\t", rowValues)).append("\n");
    //             }
    //         }
    //     } else {
    //         // If no WHERE clause, return all rows
    //         for (Map<String, String> row : rows) {
    //             List<String> rowValues = new ArrayList<>();
    //             for (String col : columns) {
    //                 rowValues.add(row.getOrDefault(col, "NULL"));
    //             }
    //             result.append(String.join("\t", rowValues)).append("\n");
    //         }
    //     }
    //     return result.toString();
    // }


    public String select(String[] tokens) {
        if (tokens.length < 4 || !tokens[1].equals("*") || !tokens[2].equalsIgnoreCase("FROM")) {
            return "ERROR: Invalid SELECT syntax";
        }
    
        String tableName = tokens[3];
        Table table = tables.get(tableName);
        if (table == null) {
            return "ERROR: Table " + tableName + " does not exist.";
        }
    
        List<Map<String, String>> rows = table.getAllRows();
        List<String> columns = table.getColumns();
    
        // Check for WHERE clause
        List<String[]> whereConditions = new ArrayList<>();
        if (tokens.length > 4 && tokens[4].equalsIgnoreCase("WHERE")) {
            whereConditions = parseWhereClause(Arrays.copyOfRange(tokens, 5, tokens.length));
        }
    
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columns)).append("\n"); // Header
    
        // Handle WHERE condition with only the primary key
        if (whereConditions.size() == 1) {
            String[] condition = whereConditions.get(0);
            if (condition[1].equalsIgnoreCase(columns.get(0)) && condition[2].equals("=")) {
                String key = condition[3];
                Map<String, String> valueMap = null;
                if (table.get(key) != null){
                    valueMap = parseValueToMap(table.get(key).toString());
                }else {
                    return "Key not found"; 
                }
    
                // Build row values only once
                List<String> rowValues = new ArrayList<>();
                for (String col : columns) {
                    rowValues.add(valueMap.getOrDefault(col, "NULL"));
                }
                result.append(String.join("\t", rowValues)).append("\n");
                return result.toString();
            }
        }
    
        // Filter rows using the WHERE conditions if present
        for (Map<String, String> row : rows) {
            if (whereConditions.isEmpty() || evaluateWhereConditions(row, whereConditions)) {
                // Build row values only once
                List<String> rowValues = new ArrayList<>();
                for (String col : columns) {
                    rowValues.add(row.getOrDefault(col, "NULL"));
                }
                result.append(String.join("\t", rowValues)).append("\n");
            }
        }
        return result.toString();
    }


    public String update(String[] tokens) {
        if (tokens.length < 6 || !tokens[2].equalsIgnoreCase("SET")) {
            return "ERROR: Invalid UPDATE syntax";
        }
    
        String tableName = tokens[1];
        Table table = tables.get(tableName);
        if (table == null) {
            return "ERROR: Table " + tableName + " does not exist.";
        }

        List<String> columns = table.getColumns();
    
        // Parse SET clause
        String setClause = tokens[3];
        if (!tokens[4].equals("=")) {
            return "ERROR: Invalid SET clause syntax";
        }
        String newValue = tokens[5].replaceAll("^'(.*)'$", "$1");
    
        // Check for WHERE clause
        List<String[]> whereConditions = new ArrayList<>();
        if (tokens.length > 6 && tokens[6].equalsIgnoreCase("WHERE")) {
            whereConditions = parseWhereClause(Arrays.copyOfRange(tokens, 7, tokens.length));
        }

        //result for where condition that only have the primary key (using the redblacktree to find)
        if (whereConditions.size() == 1){
            
            String[] condition = whereConditions.get(0);
            if (condition[1].equalsIgnoreCase(columns.get(0)) && condition[2].equals("=")) {
                
                String key = condition[3];
                if(key == null){
                    return "Doesnt exist"; 
                }
                Object getvalue = table.get(key);
                if(getvalue == null){
                    return "Doesnt exist";
                }
                String value = getvalue.toString();
                Map<String,String> value_map = parseValueToMap(value);

                value_map.put(setClause, newValue);
                table.update(key, value_map);

                return "1 row(s) updated in " + tableName + ".";
            }
        }
    
        int updatedCount = 0;

        List<Map<String,String>> rows = table.getAllRows();

        for (Map<String, String> row : rows) {
            if (whereConditions.isEmpty() || evaluateWhereConditions(row, whereConditions)) {

                row.put(setClause, newValue);
                String key = row.get(columns.get(0));
                table.update(key, row);
                updatedCount++;
            }
        }
    
        return updatedCount + " row(s) updated in " + tableName + ".";
    }






    public String create(String[] tokens) {
        if (tokens.length < 4 || !tokens[1].equalsIgnoreCase("TABLE")) {
            return "ERROR: Invalid CREATE TABLE syntax";
        }

        String tableName = tokens[2];
        if (tables.containsKey(tableName)) {
            return "ERROR: Table already exists";
        }

        // Extract columns between parentheses
        String columnList = queryBetweenParentheses(tokens, 3);
        if (columnList.isEmpty()) {
            return "ERROR: No columns specified";
        }

        List<String> columns = Arrays.asList(columnList.split(","));
        columns.replaceAll(String::trim); // Remove any extra spaces

        Table newTable = new Table(tableName, columns);
        tables.put(tableName, newTable);
        return "Table " + tableName + " created successfully.";
    }

    public void clear(){
        for (String s : tables.keySet()){
            Table toClear = tables.get(s);
            toClear.clear();
            toClear = null;
        }
        tables.clear();
        System.gc();
    }









    // Helper method to extract content inside parentheses
    private String queryBetweenParentheses(String[] tokens, int startIndex) {
        StringBuilder result = new StringBuilder();
        boolean start = false;
        for (int i = startIndex; i < tokens.length; i++) {
            if (tokens[i].contains("(")) {
                start = true;
                result.append(tokens[i].substring(tokens[i].indexOf("(") + 1)).append(" ");
            } else if (tokens[i].contains(")")) {
                result.append(tokens[i].substring(0, tokens[i].indexOf(")")));
                break;
            } else if (start) {
                result.append(tokens[i]).append(" ");
            }
        }
        return result.toString().trim();
    }

    // Helper method to parse values, considering quoted strings
    private List<String> parseValues(String valueList) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < valueList.length(); i++) {
            char c = valueList.charAt(i);
            if (c == '\'') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        values.add(current.toString().trim());
        return values;
    }

    // Helper method to parse WHERE clause
    private List<String[]> parseWhereClause(String[] tokens) {
        List<String[]> conditions = new ArrayList<>();
        String logicalOp = null;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].toUpperCase();
            if (token.equals("AND") || token.equals("OR")) {
                logicalOp = token;
            } else if (isOperator(token)) {
                String column = tokens[i - 1];
                String operator = token;
                String value = tokens[i + 1].replaceAll("^'(.*)'$", "$1");
                conditions.add(new String[] { logicalOp, column, operator, value });
                i++; // Skip the value token
                logicalOp = null;
            }
        }
        return conditions;
    }

    // Helper method to evaluate all conditions with AND/OR logic
    private boolean evaluateWhereConditions(Map<String, String> row, List<String[]> whereConditions) {
        boolean result = true;
        boolean isFirstCondition = true; // To handle the first condition separately

        for (String[] condition : whereConditions) {
            String logicalOp = condition[0]; // Logical operator (AND/OR or null for the first condition)
            String column = condition[1]; // Column name
            String operator = condition[2]; // Comparison operator (e.g., =, >, <)
            String value = condition[3]; // Value to compare against

            // Evaluate the current condition for the current row
            boolean currentCondition = evaluateSingleCondition(row, column, operator, value);

            if (isFirstCondition) {
                // The first condition sets the initial result
                result = currentCondition;
                isFirstCondition = false;
            } else if (logicalOp.equals("AND")) {
                // For subsequent conditions, apply AND logic
                result = result && currentCondition;
            } else if (logicalOp.equals("OR")) {
                // For subsequent conditions, apply OR logic
                result = result || currentCondition;
            }
        }

        return result;
    }

    // Helper method to evaluate a single condition
    private boolean evaluateSingleCondition(Map<String, String> row, String column, String operator, String value) {
        String columnValue = row.get(column);
        if (columnValue == null) {
            return false;
        }
    
        // Try parsing the column value to double to detect numeric columns
        try {
            double columnDoubleValue = Double.parseDouble(columnValue);
            double valueDouble = Double.parseDouble(value);
    
            // Round the values to two decimal places for better comparison
            columnDoubleValue = Math.round(columnDoubleValue * 100.0) / 100.0;
            valueDouble = Math.round(valueDouble * 100.0) / 100.0;
    
            // Apply the operator to the rounded numeric values
            switch (operator) {
                case "=":
                    return columnDoubleValue == valueDouble;
                case ">":
                    return columnDoubleValue > valueDouble;
                case "<":
                    return columnDoubleValue < valueDouble;
                case ">=":
                    return columnDoubleValue >= valueDouble;
                case "<=":
                    return columnDoubleValue <= valueDouble;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            // If the value is not a numeric column, fall back to string-based comparison
            switch (operator) {
                case "=":
                    return columnValue.equals(value);
                case ">":
                    return columnValue.compareTo(value) > 0;
                case "<":
                    return columnValue.compareTo(value) < 0;
                case ">=":
                    return columnValue.compareTo(value) >= 0;
                case "<=":
                    return columnValue.compareTo(value) <= 0;
                default:
                    return false;
            }
        }
    }

    private boolean isOperator(String token) {
        return token.equals("=") || token.equals(">") || token.equals("<") || token.equals(">=") || token.equals("<=");
    }

    private boolean evaluateCondition(String columnValue, String operator, String value) {
        if (columnValue == null)
            return false;

        boolean isNumeric = isNumeric(columnValue) && isNumeric(value);
        if (isNumeric) {
            double columnNumber = Double.parseDouble(columnValue);
            double valueNumber = Double.parseDouble(value);

            switch (operator) {
                case "=":
                    return columnNumber == valueNumber;
                case ">":
                    return columnNumber > valueNumber;
                case "<":
                    return columnNumber < valueNumber;
                case ">=":
                    return columnNumber >= valueNumber;
                case "<=":
                    return columnNumber <= valueNumber;
                default:
                    return false;
            }
        } else {
            switch (operator) {
                case "=":
                    return columnValue.equals(value);
                case ">":
                    return columnValue.compareTo(value) > 0;
                case "<":
                    return columnValue.compareTo(value) < 0;
                case ">=":
                    return columnValue.compareTo(value) >= 0;
                case "<=":
                    return columnValue.compareTo(value) <= 0;
                default:
                    return false;
            }
        }
    }

    // Helper method to determine if a string is numeric
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Map<String,String> parseValueToMap (String value){

        Map<String,String> map = new CustomHashMap<>();
        String stringvalue = value.replaceAll("[{}]", "");;
        String[] values = stringvalue.split(",");
    
        for (String item : values) {
            // Split each item by "=" to get key-value pairs
            String[] entry = item.split("=");
            if (entry.length == 2) {
                map.put(entry[0].trim(), entry[1].trim());
            }
        }
        return map;
    }

}
