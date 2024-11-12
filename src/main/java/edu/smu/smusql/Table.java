package edu.smu.smusql;

import java.util.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Table {

    private List<String> columns;
    // first string is the id, then after that is the variables
    private Map <String , String[]> rows = new HashMap<>();
    private int numCols;

    public Table(String[] variables){
        columns = new ArrayList<>();
        numCols = 0;
        for (String s : variables){
            columns.add(s);
            numCols++;
        }
    }
    // public void insert(String[] variables){
    //     if (variables.length != numCols){
    //         return
    //     }
    // }
}
