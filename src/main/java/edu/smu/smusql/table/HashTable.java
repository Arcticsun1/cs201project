package edu.smu.smusql.table;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class HashTable implements Table {
    private List<String> columns;
    // first string is the id, then after that is the variables
    private Map<String , ArrayList<String>> rows = new HashMap<>();
    private int numCols;

    public int getNumCols() {
        return numCols;
    }

    public Map <String , ArrayList <String>> getRows(){
        return rows;
    }
    public HashTable(String[] variables){
        columns = new ArrayList<>();
        numCols = 0;
        for (String s : variables){
            columns.add(s);
            numCols++;
        }
    }
    public void insert(String[] values){
        rows.put(values[0] , new ArrayList<>(List.of(values)));
    }
    public void update(String[] values){

    }
    public void delete(String[] values){

    }

    public void select(String[] values){

    }
}
