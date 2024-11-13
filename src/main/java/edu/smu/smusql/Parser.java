package edu.smu.smusql;

import java.util.*;

/*
 * This is a rudimentary parser you may want to use to parse smuSQL statements.
 * Use of this parser is optional.
 * You may decide to use your own parser.
 * Example usage of some code in this parser can be found in the sample implementation.
 * However, the sample implementation does not have a 'Parser' class.
 */

public class Parser {

    public void parseInsert(String[] tokens) {
        String tableName = tokens[2]; // The name of the table to be inserted into.
        String valueList = queryBetweenParentheses(tokens, 4); // Get values list between parentheses
        List<String> values = Arrays.asList(valueList.split(",")); // These are the values in the row to be inserted.
    }

    public void parseDelete(String[] tokens) {
        String tableName = tokens[2]; // The name of the table to be deleted from.

        List<String[]> whereClauseConditions = new ArrayList<>(); // Array for storing conditions from the where clause.

        // Parse WHERE clause conditions
        if (tokens.length > 3 && tokens[3].toUpperCase().equals("WHERE")) {
            for (int i = 4; i < tokens.length; i++) {
                if (tokens[i].toUpperCase().equals("AND") || tokens[i].toUpperCase().equals("OR")) {
                    // Add AND/OR conditions
                    whereClauseConditions.add(new String[] {tokens[i].toUpperCase(), null, null, null});
                } else if (isOperator(tokens[i])) {
                    // Add condition with operator (column, operator, value)
                    String column = tokens[i - 1];
                    String operator = tokens[i];
                    String value = tokens[i + 1];
                    whereClauseConditions.add(new String[] {null, column, operator, value});
                    i += 1; // Skip the value since it has been processed
                }
            }
        }
    }

    public void parseUpdate(String[] tokens){
        String tableName = tokens[1]; // name of the table to be updated
        String setColumn = tokens[3]; // column to be updated
        String newValue = tokens[5]; // new value for above column

        // Initialize whereClauseConditions list
        List<String[]> whereClauseConditions = new ArrayList<>();

        // Parse WHERE clause conditions
        if (tokens.length > 6 && tokens[6].equalsIgnoreCase("WHERE")) {
            for (int i = 5; i < tokens.length; i++) {
                if (tokens[i].equalsIgnoreCase("AND") || tokens[i].equalsIgnoreCase("OR")) {
                    // Add AND/OR conditions
                    whereClauseConditions.add(new String[] {tokens[i].toUpperCase(), null, null, null});
                } else if (isOperator(tokens[i])) {
                    // Add condition with operator (column, operator, value)
                    String column = tokens[i - 1];
                    String operator = tokens[i];
                    String value = tokens[i + 1];
                    whereClauseConditions.add(new String[] {null, column, operator, value});
                    i += 1; // Skip the value since it has been processed
                }
            }
        }
    }

    // Helper method to extract content inside parentheses
    public String queryBetweenParentheses(String[] tokens, int startIndex) {
        StringBuilder result = new StringBuilder();
        for (int i = startIndex; i < tokens.length; i++) {
            result.append(tokens[i].replaceAll("," , "").trim()).append(" ");
        }
        return result.toString().trim().replaceAll("\\(", "").replaceAll("\\)", "");
    }

    // Helper method to determine if a string is an operator
    public boolean isOperator(String token) {
        return token.equals("=") || token.equals(">") || token.equals("<") || token.equals(">=") || token.equals("<=");
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
            } else if (isOperator(arguments[i])) {
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

    public boolean evaluateWhereCondition(List <String> row , Map <String , Integer> columns , List <String[]> whereConditions){
        boolean overallMatch = true;
        boolean nextConditionShouldMatch = true;
        for (int i = 0 ; i < row.size() ; i++){
            for (String[] condition : whereConditions){
                if (condition[0] != null) { // AND/OR operator
                    nextConditionShouldMatch = condition[0].equals("AND");
                } else {
                    // Parse column, operator, and value
                    String column = condition[1];
                    String operator = condition[2];
                    String value = condition[3];

                    int columnIndex = columns.get(column);
                    String columnValue = row.get(columnIndex);
                    boolean currentMatch = evaluateCondition(columnValue, operator, value);

                    if (nextConditionShouldMatch) {
                        overallMatch = overallMatch && currentMatch;
                    } else {
                        overallMatch = overallMatch || currentMatch;
                    }
                }
            }
        }
        return overallMatch;
    }

    private boolean evaluateCondition (String columnValue , String operator , String value){
        if (columnValue == null) return false;

        // Compare strings as numbers if possible
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
            }
        } else {
            switch (operator) {
                case "=": return columnValue.equals(value);
                case ">": return columnValue.compareTo(value) > 0;
                case "<": return columnValue.compareTo(value) < 0;
                case ">=": return columnValue.compareTo(value) >= 0;
                case "<=": return columnValue.compareTo(value) <= 0;
            }
        }

        return false;
    }
    private boolean isNumeric(String value){
        try{
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}