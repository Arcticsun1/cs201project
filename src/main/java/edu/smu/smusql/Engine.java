package edu.smu.smusql;

import java.util.*;

public class Engine {
    // stores the contents of database tables in-memory
    private LinkedBinaryTree<Table> tables = new LinkedBinaryTree<>();

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
        Table table = tables.getTableByName(tableName);
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
    
        Map<String, String> row = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            // Remove quotes from string values
            String value = values.get(i).replaceAll("^'(.*)'$", "$1");
            row.put(columns.get(i), value);
        }
    
        table.addRow(row);
        return "1 row inserted into " + tableName + ".";
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
    

    public String delete(String[] tokens) {
        if (tokens.length < 3 || !tokens[1].equalsIgnoreCase("FROM")) {
            return "ERROR: Invalid DELETE syntax";
        }
    
        String tableName = tokens[2];
        Table table = tables.getTableByName(tableName);
        if (table == null) {
            return "ERROR: Table " + tableName + " does not exist.";
        }
    
        // Parse WHERE clause if it exists
        List<String[]> whereConditions = new ArrayList<>();
        if (tokens.length > 3 && tokens[3].equalsIgnoreCase("WHERE")) {
            whereConditions = parseWhereClause(Arrays.copyOfRange(tokens, 4, tokens.length));
        }
    
        // If there are WHERE conditions, process them
        if (!whereConditions.isEmpty()) {
            String column = whereConditions.get(0)[1]; // Column from WHERE condition
            String value = whereConditions.get(0)[3]; // Value to match
    
            // Create an index based on the column specified in the WHERE clause
            Map<String, List<Map<String, String>>> index = new HashMap<>();
            for (Map<String, String> row : table.getRows()) {
                String key = row.get(column);
                if (evaluateWhereConditions(row, whereConditions)) {
                    index.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
                }
            }
    
            // Retrieve rows that match the condition using the index
            List<Map<String, String>> rowsToDelete = index.getOrDefault(value, new ArrayList<>());
    
            // Remove the rows in bulk
            table.getRows().removeAll(rowsToDelete);
            
            return rowsToDelete.size() + " row(s) deleted from " + tableName + ".";
        } else {
            // If no WHERE clause, delete all rows
            int deletedCount = table.getRows().size();
            table.getRows().clear();
            return deletedCount + " row(s) deleted from " + tableName + ".";
        }
    }
    
    
    public String select(String[] tokens) {
        if (tokens.length < 4 || !tokens[1].equals("*") || !tokens[2].equalsIgnoreCase("FROM")) {
            return "ERROR: Invalid SELECT syntax";
        }
    
        String tableName = tokens[3];
        Table table = tables.getTableByName(tableName);
        if (table == null) {
            return "ERROR: Table " + tableName + " does not exist.";
        }
    
        List<Map<String, String>> rows = table.getRows();
        List<String> columns = table.getColumns();
    
        // Check for WHERE clause
        List<String[]> whereConditions = new ArrayList<>();
        if (tokens.length > 4 && tokens[4].equalsIgnoreCase("WHERE")) {
            whereConditions = parseWhereClause(Arrays.copyOfRange(tokens, 5, tokens.length));
        }
    
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columns)).append("\n"); // Header
    
        if (!whereConditions.isEmpty()) {
            // Indexing for WHERE clause processing
            Map<String, List<Map<String, String>>> index = new HashMap<>();
            for (Map<String, String> row : rows) {
                for (String col : columns) {
                    String key = row.get(col);
                    if (evaluateWhereConditions(row, whereConditions)) {
                        index.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
                    }
                }
            }
    
            // Evaluate conditions and collect results
            for (String[] condition : whereConditions) {
                String logicalOp = condition[0]; // Logical operator (AND/OR)
                String column = condition[1]; // Column from WHERE condition
                String operator = condition[2]; // Operator
                String value = condition[3]; // Value to match
    
                // Retrieve rows based on the condition
                List<Map<String, String>> matchingRows = index.getOrDefault(column + "=" + value, new ArrayList<>());
    
                // Process results
                for (Map<String, String> row : matchingRows) {
                    List<String> rowValues = new ArrayList<>();
                    for (String col : columns) {
                        rowValues.add(row.getOrDefault(col, "NULL"));
                    }
                    result.append(String.join("\t", rowValues)).append("\n");
                }
            }
        } else {
            // If no WHERE clause, return all rows
            for (Map<String, String> row : rows) {
                List<String> rowValues = new ArrayList<>();
                for (String col : columns) {
                    rowValues.add(row.getOrDefault(col, "NULL"));
                }
                result.append(String.join("\t", rowValues)).append("\n");
            }
        }
    
        return result.toString();
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
    
    // Helper method to evaluate WHERE conditions
    private boolean evaluateWhereConditions(Map<String, String> row, List<String[]> conditions) {
        boolean result = true;
    
        for (String[] condition : conditions) {
            String logicalOp = condition[0];
            String column = condition[1];
            String operator = condition[2];
            String value = condition[3];
    
            boolean conditionResult = evaluateCondition(row.get(column), operator, value);
    
            if (logicalOp == null) {
                result = conditionResult;
            } else if (logicalOp.equals("AND")) {
                result = result && conditionResult;
            } else if (logicalOp.equals("OR")) {
                result = result || conditionResult;
            }
        }
    
        return result;
    }
    
    // Reuse existing helper methods isOperator and evaluateCondition from previous code
    
public String update(String[] tokens) {
    // Ensure there are enough tokens for a valid UPDATE query
    if (tokens.length < 6 || !tokens[2].equalsIgnoreCase("SET")) {
        return "ERROR: Invalid UPDATE syntax. Expected SET after table name.";
    }

    // Get table name
    String tableName = tokens[1];
    Table table = tables.getTableByName(tableName);
    if (table == null) {
        return "ERROR: Table " + tableName + " does not exist.";
    }

    // Check the SET clause for proper column and value assignment
    if (tokens.length < 6 || !tokens[4].equals("=")) {
        return "ERROR: Invalid SET clause syntax. Expected '=' after column name.";
    }

    // Get column name and the new value to set
    String columnName = tokens[3];
    String newValue = tokens[5].replaceAll("^'(.*)'$", "$1"); // Remove surrounding quotes if they exist

    // Check if the column exists in the table
    if (!table.getRows().get(0).containsKey(columnName)) {
        return "ERROR: Column " + columnName + " does not exist in the table.";
    }

    // Parse WHERE clause, if present
    List<String[]> whereConditions = new ArrayList<>();
    if (tokens.length > 6 && tokens[6].equalsIgnoreCase("WHERE")) {
        whereConditions = parseWhereClause(Arrays.copyOfRange(tokens, 7, tokens.length));
    }

    // Apply the update to rows that match the conditions
    int updatedCount = 0;
    for (Map<String, String> row : table.getRows()) {
        // If no WHERE clause, or the WHERE clause matches the row, update the column
        if (whereConditions.isEmpty() || evaluateWhereConditions(row, whereConditions)) {
            row.put(columnName, newValue); // Update the row with new value
            updatedCount++;
        }
    }

    // Return the result
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

private boolean isOperator(String token) {
    return token.equals("=") || token.equals(">") || token.equals("<") || token.equals(">=") || token.equals("<=");
}
private boolean evaluateCondition(String columnValue, String operator, String value) {
    if (columnValue == null) return false;

    boolean isNumeric = isNumeric(columnValue) && isNumeric(value);
    if (isNumeric) {
        double columnNumber = Double.parseDouble(columnValue);
        double valueNumber = Double.parseDouble(value);

        switch (operator) {
            case "=": return columnNumber == valueNumber;
            case ">": return columnNumber > valueNumber;
            case "<": return columnNumber < valueNumber;
            case ">=": return columnNumber >= valueNumber;
            case "<=": return columnNumber <= valueNumber;
            default: return false;
        }
    } else {
        switch (operator) {
            case "=": return columnValue.equals(value);
            case ">": return columnValue.compareTo(value) > 0;
            case "<": return columnValue.compareTo(value) < 0;
            case ">=": return columnValue.compareTo(value) >= 0;
            case "<=": return columnValue.compareTo(value) <= 0;
            default: return false;
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

// Example method to apply WHERE condition using index
private List<Map<String, String>> applyWhereConditionWithIndex(Table table, List<String[]> conditions) {
    // Assuming conditions[0][1] is the column name for the first condition
    String whereColumn = conditions.get(0)[1];
    String whereValue = conditions.get(0)[3];

    // Create an index for the whereColumn
    Map<String, List<Map<String, String>>> index = new HashMap<>();
    for (Map<String, String> row : table.getRows()) {
        String key = row.get(whereColumn);
        index.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
    }

    // Retrieve matching rows from the index
    return index.getOrDefault(whereValue, Collections.emptyList());
}

public void clear(){
    tables = new LinkedBinaryTree<>();
    System.gc();
}

}