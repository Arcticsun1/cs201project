package edu.smu.smusql.table;

import java.util.*;


public interface Table {

    int getNumCols();
    Map <String , ArrayList <String>> getRows();
    Collection <String> getColumns();
    void insert(String[] values);
    void update(String[] tokens);
    void delete(String[] tokens);
    void select(String[] tokens);
}
