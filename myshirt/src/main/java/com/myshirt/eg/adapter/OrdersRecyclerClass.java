package com.myshirt.eg.adapter;

public class OrdersRecyclerClass {

    String id;
    String date;
    String status;
    String payment_method;
    String amount;

    public OrdersRecyclerClass(String id, String date, String status, String payment_method, String amount) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.payment_method = payment_method;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public String getAmount() {
        return amount;
    }
}