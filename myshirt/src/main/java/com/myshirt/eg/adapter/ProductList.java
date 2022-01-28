package com.myshirt.eg.adapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProductList {

    String id;
    String type;
    String name;
    String description;
    String price;
    String regular_price;
    String product_type;
    String categories = "[]";
    String image;
    String in_wish_list = "";
    String stock_status = "";
    String lowest_price = "0";
    JSONArray variations = new JSONArray();
    JSONArray attributes = new JSONArray();



    //    GETTERS
    public String getId() {
        return id;
    }

    public String getIn_wish_list() {
        return in_wish_list;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIn_wish_list(String wish_list) {
        this.in_wish_list = wish_list;
    }

    public void setLowest_price(String lowest_price) {
        this.lowest_price = lowest_price;
    }
    public String getLowest_price() {
        return  lowest_price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getPrice() {
        return (price.isEmpty()) ? "0" : price;
    }
//
    public void setPrice(String price) {
        this.price = (price.isEmpty()) ? "0" : price;
    }
//
    public String getRegular_price() {
        return (regular_price.isEmpty()) ? "0" : regular_price;
    }
//
    public void setRegular_price(String regular_price) {
        this.regular_price = (regular_price.isEmpty()) ? "0" : regular_price;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }


//
    public String getCategories() {
        return categories;
    }
//
    public void setCategories(String categories) {
        this.categories = categories;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStock_status() {
        return stock_status;
    }

    public void setStock_status(String stock_status) {
        this.stock_status = stock_status;
    }

    public JSONArray getVariations() {
        return variations;
    }

    public void setVariations(JSONArray variations) {
        this.variations = variations;
    }

    public JSONArray getAttributes() {
        return attributes;
    }

    public void setAttributes(JSONArray attributes) {
        this.attributes = attributes;
    }
}
