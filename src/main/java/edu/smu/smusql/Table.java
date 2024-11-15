package edu.smu.smusql;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Table<K, V> {

    // A Red-Black Tree Map to store the rows
    private RBTreeMap<K, V> tableData;
    private String name;
    private List<String> columns;

    // Constructor with a key and a list of values (for initializing the table)
    public Table(String name, List<String> columns) {
        this.tableData = new RBTreeMap<>(); // Initialize the Red-Black Tree map
        this.name = name; // Insert the key and the list of values into the map
        this.columns = columns;
    }

    public String getName() {
        return name;
    }

    public List<String> getColumns() {
        return columns;
    }

    // Add a row to the table
    public void insert(K key, V value) {
        tableData.put(key, value); // Insert row with key-value pair
    }

    // Remove a row from the table by key
    public void delete(K key) {
        tableData.remove(key); // Remove row with the given key
    }

    // Get a row from the table by key
    public V get(K key) {
        return tableData.get(key); // Get row by key
    }

    // Retrieve all keys (like a primary key column)
    public List<String> getAllKeys() {
        List<String> keyList = new ArrayList<>();

        // Iterate over the keys and add their string representation to the list
        for (K key : tableData.keySet()) {
            keyList.add(key.toString()); // Add the string representation of the key to the list
        }

        return keyList; // Return the list of keys as strings
    }

    // Retrieve all rows as a List of Maps (each map represents a row)
    public List<Map<String, String>> getAllRows() {
        List<Map<String, String>> rows = new ArrayList<>();

        for (K key : tableData.keySet()) {
            V value = tableData.get(key);
            String stringvalue = value.toString().replaceAll("[{}]", "");;
            String[] values = stringvalue.split(",");
            Map<String, String> rowMap = new CustomHashMap();

            for (String item : values) {
                // Split each item by "=" to get key-value pairs
                String[] entry = item.split("=");
                if (entry.length == 2) {
                    rowMap.put(entry[0].trim(), entry[1].trim());
                }
            }
            rows.add(rowMap);
        }

        return rows; // Returns the List of Maps representing rows
    }


    public void update(K key, V updatedValues) {
        // Retrieve the existing row by key
        V existingRow = tableData.get(key);
        if (existingRow!= null){

            tableData.put(key, updatedValues);
        }else {
            return;
        }
    }

    public void clear(){
        tableData = null;
        tableData = new RBTreeMap<>();
    }
}
