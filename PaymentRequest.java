package com.example.smartwallet;

public class PaymentRequest {
    private String consumerId;
    private double amount;
    private String serviceType;

    public PaymentRequest(String consumerId, double amount, String serviceType) {
        this.consumerId = consumerId;
        this.amount = amount;
        this.serviceType = serviceType;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}