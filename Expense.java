package com.example.smartwallet;

public class Expense {
    private int id;
    private String title;
    private double amount;
    private String category;
    private String date;
    private String type; // "Income" or "Expense"

    // Empty constructor for Firebase
    public Expense() {
    }

    // Constructor for creating new transaction
    public Expense(String title, double amount, String category, String date, String type) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.type = type;
    }

    // Constructor for reading from DB
    public Expense(int id, String title, double amount, String category, String date, String type) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.type = type;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}