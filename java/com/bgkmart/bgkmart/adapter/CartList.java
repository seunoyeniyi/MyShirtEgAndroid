package com.fatima.fabric.adapter;

public class CartList {
    String id;
    String quantity;
    String price;
    String subtotal;
    String attributes;
    String productType;
    String productTitle;
    String productImage;

    //SET
    public void setId(String id) {
        this.id = id;
    }
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
    public void setPrice(String price) {
        this.price = price;
    }
    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }
    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }
    public void setProductType(String type) {
        this.productType = type;
    }
    public void setProductTitle(String title) {
        this.productTitle = title;
    }
    public void setProductImage(String image) {
        this.productImage = image;
    }

    //GET
    public String getId() {
        return this.id;
    }
    public String getQuantity() {
        return this.quantity;
    }
    public String getPrice() {
        return this.price;
    }
    public String getSubtotal() {
        return this.subtotal;
    }
    public String getAttributes() {
        return this.attributes;
    }
    public String getProductType() {
        return this.productType;
    }
    public String getProductTitle() {
        return this.productTitle;
    }
    public String getProductImage() {
        return this.productImage;
    }
}
