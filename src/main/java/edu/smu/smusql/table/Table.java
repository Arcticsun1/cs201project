package edu.smu.smusql.table;

import java.util.*;


public interface Table {

    int getNumCols();
    Map <String , String[]> getRows();
    Collection <String> getColumns();
    void insert(String[] values);
    int update(String[] tokens);
    int delete(String[] tokens);
    List <List<String>> select(String[] tokens);
}
