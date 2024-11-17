package edu.smu.smusql.table;

import lombok.Getter;
import lombok.Setter;
import java.util.*;

@Getter
@Setter
public class BPlusTreeTable extends Table {
    private BPlusTree index;
    private HeapStorage storage;
    
    public BPlusTreeTable(String[] variables) {
        columns = new ArrayList<>();
        numCols = 0;
        int i = 0;
        for (String s : variables) {
            if (!s.isBlank()) {
                columns.add(s);
                columnIndex.put(s, i);
                i++;
                numCols++;
            }
        }
        index = new BPlusTree();
        storage = new HeapStorage();
    }

    @Override
    public Collection<String> getColumns() {
        return columns;
    }

    @Override
    public Map<String, String[]> getRows() {
        Map<String, String[]> result = new HashMap<>();
        for (Record record : storage.getAllRecords()) {
            result.put(record.getId(), record.toArray());
        }
        return result;
    }

    @Override
    public void insert(String[] values) {
        String[] cleanedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleanedValues[i] = values[i].replaceAll(",", "").trim();
        }
        
        RID rid = storage.insertRecord(new Record(cleanedValues));
        index.insert(cleanedValues[0], rid);
    }

    @Override
    public int update(String[] tokens) {
        int result = 0;
        String column = tokens[3];
        String newValue = tokens[5];
        List<String[]> whereConditions = parser.whereConditions(tokens, 6);
        
        List<Record> records = new ArrayList<>(storage.getAllRecords());
        for (Record record : records) {
            List<String> rowValues = Arrays.asList(record.toArray());
            if (parser.evaluateWhereCondition(rowValues, columnIndex, whereConditions)) {
                String oldId = record.getId();
                RID oldRid = index.search(oldId);
                
                if (oldRid != null) {
                    int columnIdx = columnIndex.get(column);
                    record.updateField(columnIdx, newValue);
                    
                    if (column.equals("id")) {
                        index.delete(oldId);
                        index.insert(newValue, oldRid);
                    }
                    
                    storage.updateRecord(oldRid, record);
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public int delete(String[] tokens) {
        int result = 0;
        List<String[]> whereConditions = parser.whereConditions(tokens, 3);
        
        for (Record record : storage.getAllRecords()) {
            List<String> rowValues = Arrays.asList(record.toArray());
            if (parser.evaluateWhereCondition(rowValues, columnIndex, whereConditions)) {
                RID rid = index.search(record.getId());
                if (rid != null) {
                    storage.deleteRecord(rid);
                    index.delete(record.getId());
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    public List<List<String>> select(String[] tokens) {
        List<List<String>> result = new ArrayList<>();
        
        if (tokens.length <= 4) {
            for (Record record : storage.getAllRecords()) {
                result.add(Arrays.asList(record.toArray()));
            }
            return result;
        }
        
        List<String[]> whereConditions = parser.whereConditions(tokens, 4);
        for (Record record : storage.getAllRecords()) {
            List<String> rowValues = Arrays.asList(record.toArray());
            if (parser.evaluateWhereCondition(rowValues, columnIndex, whereConditions)) {
                result.add(rowValues);
            }
        }
        return result;
    }

    @Override
    public void clear() {
        index.clear();
        storage.clear();
    }
}
