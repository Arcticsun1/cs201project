package edu.smu.smusql;

import java.util.*;

public class Table {
    private String name;
    private List<String> columns;
    private List<Map<String, String>> rows;

    public Table(String name, List<String> columns) {
        this.name = name;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Map<String, String>> getRows() {
        return rows;
    }

    public void addRow(Map<String, String> row) {
        rows.add(row);
    }

    public void removeRow(Map<String, String> row) {
        rows.remove(row);
    }

    // Additional helper methods can be added as needed
}