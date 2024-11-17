package edu.smu.smusql.table;

public class Record {
    public static final int RECORD_SIZE = 1024; // bytes
    private String[] fields;
    
    public Record(String[] fields) {
        this.fields = fields.clone();
    }
    
    public String getId() {
        return fields[0];
    }
    
    public String[] toArray() {
        return fields.clone();
    }
    
    public void updateField(int index, String value) {
        fields[index] = value;
    }
}
