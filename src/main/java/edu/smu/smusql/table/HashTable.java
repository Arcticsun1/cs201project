package edu.smu.smusql.table;

import edu.smu.smusql.Parser;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class HashTable implements Table {
    private Parser parser = new Parser();
    private List <String> columns;
    private Map <String , Integer> columnIndex = new HashMap<>();
    // first string is the id, then after that is the variables
    private Map<String , String[]> rows = new HashMap<>();
    private int numCols;

    public int getNumCols() {
        return numCols;
    }

    public Map<String, String[]> getRows(){
        return rows;
    }

    public List <String> getColumns(){
        return columns;
    }
    public HashTable(String[] variables){
        columns = new ArrayList<>();
        numCols = 0;
        int i = 0;
        for (String s : variables){
            if (!s.isBlank()){
                columns.add(s);
                columnIndex.put(s , i);
                i++;
                numCols++;
            }

        }
    }
    @Override
    public void insert(String[] tokens){
        rows.put(tokens[0] , tokens);
    }
    public int update(String[] tokens){
        //"UPDATE orders SET quantity = " + newQuantity + " WHERE id = " + orderId

        int result = 0;
        List <String[]> whereConditions = parser.whereConditions(tokens , 6);
        String column = tokens[3];
        String newValue = tokens[5];
        for (String id : rows.keySet()){
            String[] row = rows.get(id);
            List <String> rowList = Arrays.stream(rows.get(id)).toList();
            if (parser.evaluateWhereCondition(rowList , columnIndex , whereConditions)){
                int index = columnIndex.get(column);
                row[index] = newValue;
                result++;
            }
        }
        return result;
    }
    public int delete(String[] tokens){
        int result = 0;
        List <String[]> whereConditions = parser.whereConditions(tokens , 3);
        Set <String> idRemoved = new HashSet<>();
        for (String id : rows.keySet()){
            List <String> row = Arrays.stream(rows.get(id)).toList();
            if (parser.evaluateWhereCondition(row , columnIndex , whereConditions)){
                idRemoved.add(id);
                result++;
            }
        }
        for (String id : idRemoved){
            rows.remove(id);
        }
        return result;
    }

    public List<List<String>> select(String[] tokens){
        List <List <String>> result = new ArrayList<>();
        List <String[]> whereConditions = parser.whereConditions(tokens , 4);
        for (String id : rows.keySet()){
            List <String> row = Arrays.stream(rows.get(id)).toList();
            if (parser.evaluateWhereCondition(row , columnIndex , whereConditions)){
                result.add(row);
            }
        }
        return result;
    }
}
