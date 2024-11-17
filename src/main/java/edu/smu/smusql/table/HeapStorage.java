package edu.smu.smusql.table;

import java.util.List;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeapStorage {
    private List<Page> pages;

    public List<Record> getAllRecords() {
        List<Record> allRecords = new ArrayList<>();
        for (Page page : pages) {
            for (int i = 0; i < page.getRecords().size(); i++) {
                Record record = page.getRecord(i);
                if (record != null) {
                    allRecords.add(record);
                }
            }
        }
        return allRecords;
    }
    
    public HeapStorage() {
        this.pages = new ArrayList<>();
        pages.add(new Page()); // Initial page
    }
    
    public RID insertRecord(Record record) {
        // Find page with space and insert record
        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            if (page.hasSpace()) {
                int offset = page.insertRecord(record);
                return new RID(i, offset);
            }
        }
        // No space found, create new page
        Page newPage = new Page();
        pages.add(newPage);
        int offset = newPage.insertRecord(record);
        return new RID(pages.size() - 1, offset);
    }
    
    public Record getRecord(RID rid) {
        return pages.get(rid.getPageId()).getRecord(rid.getOffset());
    }
    
    public void updateRecord(RID rid, Record record) {
        pages.get(rid.getPageId()).updateRecord(rid.getOffset(), record);
    }
    
    public void deleteRecord(RID rid) {
        pages.get(rid.getPageId()).deleteRecord(rid.getOffset());
    }
    
    public void clear() {
        pages.clear();
    }
}
