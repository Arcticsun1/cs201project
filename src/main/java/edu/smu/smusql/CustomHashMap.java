package edu.smu.smusql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class CustomHashMap<K, V> implements Map<K, V> {

    private HashMap<K, V> internalMap = new HashMap<>();

    private static class Entry<K, V> implements Map.Entry<K, V> {
        K key;
        V value;

        // Constructor to initialize key and value
        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        // Method to set the value (which might be what you were trying to use)
        public void setValue(V value) {
            this.value = value;
        }
    }

    // Example of how to store entries, assume a simple array or linked list is used
    private Entry<K, V>[] entries;
    private int size;

    public CustomHashMap() {
        entries = new Entry[10];
        size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public V get(K key) {
        for (Entry<K, V> entry : entries) {
            if (entry != null && entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        // Check if the key exists
        for (Entry<K, V> entry : entries) {
            if (entry != null && entry.getKey().equals(key)) {
                V oldValue = entry.getValue();
                entry.setValue(value);  // Update the value
                return oldValue;
            }
        }
        // If the key doesn't exist, add a new entry
        if (size < entries.length) {
            entries[size++] = new Entry<>(key, value);
        }
        return null;
    }

    @Override
    public V remove(K key) {
        for (int i = 0; i < size; i++) {
            if (entries[i].getKey().equals(key)) {
                V oldValue = entries[i].getValue();
                entries[i] = entries[--size];  // Remove the entry by shifting the array
                return oldValue;
            }
        }
        return null;
    }

    @Override
    public Iterable<K> keySet() {
        List<K> keys = new ArrayList<>();
        for (Entry<K, V> entry : entries) {
            if (entry != null) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    @Override
    public Iterable<V> values() {
        List<V> values = new ArrayList<>();
        for (Entry<K, V> entry : entries) {
            if (entry != null) {
                values.add(entry.getValue());
            }
        }
        return values;
    }

    @Override
    public Iterable<Map.Entry<K, V>> entrySet() {
        List<Map.Entry<K, V>> entriesList = new ArrayList<>();
        for (Entry<K, V> entry : entries) {
            if (entry != null) {
                entriesList.add(entry);
            }
        }
        return entriesList;
    }

    @Override
    public boolean containsKey(K key) {
        // Check if the internal map contains the specified key
        return internalMap.containsKey(key);  // This assumes you have some internal map to back your CustomHashMap
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<K, V> entry : this.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue() + ", ");
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2); // Remove trailing ", "
        }
        sb.append("}");
        return sb.toString();
    }
}