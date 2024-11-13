package edu.smu.smusql.table;

import edu.smu.smusql.Parser;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public abstract class Table {
    protected Parser parser = new Parser();
    protected int numCols;
    protected List <String> columns;
    protected Map <String , Integer> columnIndex = new HashMap<>();

    public abstract Map <String , String[]> getRows();
    public abstract Collection <String> getColumns();
    public abstract void insert(String[] values);
    public abstract int update(String[] tokens);
    public abstract int delete(String[] tokens);
    public abstract List <List<String>> select(String[] tokens);

    public abstract void clear();
}
