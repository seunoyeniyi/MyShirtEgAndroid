package com.myshirt.eg.adapter;

public class CartList {
    String id;
    String quantity;
    String price;
//    String subtotal;
//    String attributes;
//    String productType;
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
//    public void setSubtotal(String subtotal) {
//        this.subtotal = subtotal;
//    }
//    public void setAttributes(String attributes) {
//        this.attributes = attributes;
//    }
//    public void setProductType(String type) {
//        this.productType = type;
//    }
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
// --Commented out by Inspection START (6/28/2021 5:38 PM):
//    public String getSubtotal() {
//        return this.subtotal;
//    }
// --Commented out by Inspection STOP (6/28/2021 5:38 PM)
// --Commented out by Inspection START (6/28/2021 5:38 PM):
// --Commented out by Inspection START (6/28/2021 5:38 PM):
////    public String getAttributes() {
////        return this.attributes;
////    }
//// --Commented out by Inspection STOP (6/28/2021 5:38 PM)
// --Commented out by Inspection STOP (6/28/2021 5:38 PM)
//    public String getProductType() {
//        return this.productType;
//    }
    public String getProductTitle() {
        return this.productTitle;
    }
    public String getProductImage() {
        return this.productImage;
    }
}
