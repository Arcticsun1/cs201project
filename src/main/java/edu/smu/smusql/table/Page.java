package edu.smu.smusql.table;

import java.util.*;

public class Page {
    private static final int PAGE_SIZE = 4096; // bytes
    private List<Record> records;
    private int freeSpace;
    
    public Page() {
        this.records = new ArrayList<>();
        this.freeSpace = PAGE_SIZE;
    }

    public boolean hasSpace() {
        return freeSpace >= Record.RECORD_SIZE;
    }
    
    public int insertRecord(Record record) {
        if (!hasSpace()) {
            throw new IllegalStateException("Page is full");
        }
        records.add(record);
        freeSpace -= Record.RECORD_SIZE;
        return records.size() - 1; // Return offset
    }

    public List<Record> getRecords() {
        return records;
    }
    
    public Record getRecord(int offset) {
        return records.get(offset);
    }
    
    public void updateRecord(int offset, Record record) {
        records.set(offset, record);
    }
    
    public void deleteRecord(int offset) {
        records.set(offset, null);
        freeSpace += Record.RECORD_SIZE;
    }
}
