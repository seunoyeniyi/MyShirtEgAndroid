package com.myshirt.eg.adapter;

public class ShippingMethodList {
    String title, id, cost;

    public ShippingMethodList(String title, String id, String cost) {
        this.title = title;
        this.id = id;
        this.cost = cost;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }
}
