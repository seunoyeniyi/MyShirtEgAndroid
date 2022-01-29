package com.myshirt.eg;

public class Site {
    public static final String PROTOCOL = "https";
    public static final String DOMAIN =     "myshirt-eg.com"; //"192.168.43.11"; // "192.168.43.223"; //"10.0.2.2"; //
    public static final String ADDRESS = PROTOCOL + "://" + DOMAIN + "/";
    public static final String INFO = ADDRESS + "wp-json/skye-api/v1/site-info/";
    public static final String CART = ADDRESS + "wp-json/skye-api/v1/cart/";
    public static final String ADD_TO_CART = ADDRESS + "wp-json/skye-api/v1/add-to-cart/";
    public static final String PRODUCTS = ADDRESS + "wp-json/skye-api/v1/products/";
    public static final String SIMPLE_PRODUCTS = ADDRESS + "wp-json/skye-api/v1/simple-products/";
    public static final String PRODUCT = ADDRESS + "wp-json/skye-api/v1/product/";
    public static final String PRODUCT_VARIATION = ADDRESS + "wp-json/skye-api/v1/product-variation/";
    public static final String LOGIN = ADDRESS + "wp-json/skye-api/v1/authenticate";
    public static final String REGISTER = ADDRESS + "wp-json/skye-api/v1/register/";
    public static final String USER = ADDRESS + "wp-json/skye-api/v1/user-info/";
    public static final String UPDATE_USER = ADDRESS + "wp-json/skye-api/v1/update-user-info/";
    public static final String UPDATE_SHIPPING = ADDRESS + "wp-json/skye-api/v1/update-user-shipping-address/";
    public static final String CREATE_ORDER = ADDRESS + "wp-json/skye-api/v1/create-order/";
    public static final String UPDATE_COUPON = ADDRESS + "wp-json/skye-api/v1/update-cart-coupon/";
    public static final String CHANGE_CART_SHIPPING = ADDRESS + "wp-json/skye-api/v1/change-cart-shipping-method/";
    public static final String ORDERS = ADDRESS + "wp-json/skye-api/v1/orders/";
    public static final String ORDER = ADDRESS + "wp-json/skye-api/v1/order/";
    public static final String UPDATE_ORDER = ADDRESS + "wp-json/skye-api/v1/update-order/";
    public static final String CATEGORIES = ADDRESS + "wp-json/skye-api/v1/categories/";
    public static final String ATTRIBUTES = ADDRESS + "wp-json/skye-api/v1/attributes/";
    public static final String TAGS = ADDRESS + "wp-json/skye-api/v1/tags/";
    public static final String ADD_TO_WISH_LIST = ADDRESS + "wp-json/skye-api/v1/add-to-wishlist/";
    public static final String REMOVE_FROM_WISH_LIST = ADDRESS + "wp-json/skye-api/v1/remove-from-wishlist/";
    public static final String WISH_LIST = ADDRESS + "wp-json/skye-api/v1/wishlists/";
    public static final String BANNERS = ADDRESS + "wp-json/skye-api/v1/banners/";
    public static final String COMPLETE_ORDER_PAGE = ADDRESS + "app-complete-order/";
    public static final String APPLY_REWARD = ADDRESS + "wp-json/skye-api/v1/apply-cart-reward/";
    public static final String SAVE_DEVICE = ADDRESS + "wp-json/skye-api/v1/save-user-device/";
    public static final String SEARCH = ADDRESS + "wp-json/skye-api/v1/search/";
    public static final String ADD_REVIEW = ADDRESS + "wp-json/skye-api/v1/add-review/";

    public static final String CURRENCY = "EGP";
    public static String payment_method_title(String slug) {
        String title = "";
        switch (slug) {
            case "cod":
                title = "Cash On Delivery";
                break;
            case "bacs":
                title = "Direct Bank Transfer";
                break;
            case "cheque":
                title = "Check Payments";
                break;
            case "paypal":
                title = "Paypal";
                break;
            case "stripe":
                title = "Stripe";
                break;
            case "stripe_cc":
                title = "Credit Cards";
                break;
            default:
                title = "No Payment method";
                break;

        }
        return title;
    }
}
