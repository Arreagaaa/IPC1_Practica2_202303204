package com.usacdata.model;

public class DataPoint {
    private String category;
    private int count;

    public DataPoint(String category, int count) {
        this.category = category;
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public int getCount() {
        return count;
    }
}
