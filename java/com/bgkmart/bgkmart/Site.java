package com.fatima.fabric;

public class Site {
    public static final String PROTOCOL = "http";
    public static final String DOMAIN = "192.168.43.11"; // "192.168.43.223"; //"10.0.2.2"; //
    public static final String ADDRESS = PROTOCOL + "://" + DOMAIN + "/";
    public static final String CART = ADDRESS + "wp-json/skye-api/v1/cart/";
    public static final String ADD_TO_CART = ADDRESS + "wp-json/skye-api/v1/add-to-cart/";
    public static final String PRODUCTS = ADDRESS + "wp-json/skye-api/v1/products/";
    public static final String PRODUCT = ADDRESS + "wp-json/skye-api/v1/product/";
    public static final String PRODUCT_VARIATION = ADDRESS + "wp-json/skye-api/v1/product-variation/";
    public static final String LOGIN = ADDRESS + "wp-json/skye-api/v1/authenticate";
    public static final String REGISTER = ADDRESS + "wp-json/skye-api/v1/register/";
    public static final String USER = ADDRESS + "wp-json/skye-api/v1/user-info/";
    public static final String UPDATE_SHIPPING = ADDRESS + "wp-json/skye-api/v1/update-user-shipping-address/";
    public static final String CREATE_ORDER = ADDRESS + "wp-json/skye-api/v1/create-order/";
    public static final String UPDATE_COUPON = ADDRESS + "wp-json/skye-api/v1/update-cart-coupon/";
    public static final String ORDERS = ADDRESS + "wp-json/skye-api/v1/orders/";
    public static final String ORDER = ADDRESS + "wp-json/skye-api/v1/order/";
    public static final String CATEGORIES = ADDRESS + "wp-json/skye-api/v1/categories/";
}
