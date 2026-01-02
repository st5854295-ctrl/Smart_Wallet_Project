package com.example.smartwallet;

public class Goal {
    private int id;
    private String name;
    private double targetAmount;
    private double savedAmount;
    private String date;
    private String icon; // "car", "house", "education"

    public Goal(int id, String name, double targetAmount, double savedAmount, String date, String icon) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.date = date;
        this.icon = icon;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getTargetAmount() { return targetAmount; }
    public double getSavedAmount() { return savedAmount; }
    public String getDate() { return date; }
    public String getIcon() { return icon; }
}