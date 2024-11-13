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
    private Map<String , ArrayList<String>> rows = new HashMap<>();
    private int numCols;

    public int getNumCols() {
        return numCols;
    }

    public Map <String , ArrayList <String>> getRows(){
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
        rows.put(tokens[0] , new ArrayList<>(List.of(tokens)));
    }
    public int update(String[] tokens){
        int result = 0;

        List <String[]> whereConditions = parser.whereConditions(tokens , 5);
        return result;
    }
    public int delete(String[] tokens){
        int result = 0;
        List <String[]> whereConditions = parser.whereConditions(tokens , 3);
        for (String id : rows.keySet()){
            List <String> row = rows.get(id);
            if (parser.evaluateWhereCondition(row , columnIndex , whereConditions)){
                rows.remove(id);
                result++;
            }
        }
        return result;
    }

    public List<List<String>> select(String[] tokens){
        List <List <String>> result = new ArrayList<>();
        List <String[]> whereConditions = parser.whereConditions(tokens , 4);
        for (String id : rows.keySet()){
            List <String> row = rows.get(id);
            if (parser.evaluateWhereCondition(row , columnIndex , whereConditions)){
                result.add(row);
            }
        }
        return result;
    }
}
