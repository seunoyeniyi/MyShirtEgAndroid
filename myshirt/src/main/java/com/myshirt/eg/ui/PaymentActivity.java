package com.myshirt.eg.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

//import com.paypal.android.sdk.payments.PayPalConfiguration;
//import com.paypal.android.sdk.payments.PayPalPayment;
//import com.paypal.android.sdk.payments.PayPalService;
//import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.myshirt.eg.MainActivity;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
//import com.bgkmart.bgkmart.handler.PayPalConfig;
import com.myshirt.eg.adapter.ShippingMethodList;
import com.myshirt.eg.adapter.ShippingMethodsAdapter;
import com.myshirt.eg.handler.PriceFormatter;
import com.myshirt.eg.handler.UserSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class PaymentActivity extends AppCompatActivity {

    UserSession userSession;
    ProgressDialog loader;
    TextView subtotalView, totalView, fullNameView, address1View, address2View, cityStateCountryView, phoneView, couponPriceView;
    RadioButton paypalCheckView, stripeCheckView;
    RadioButton onDeliveryCheckView;
    EditText couponCode;
    RadioGroup paymentMethod;
    Button confirmBtn, applyCouponBtn;
//    String paymentAmount = "0";
    LinearLayout shippingLayout;
    TextView shippingMethodStatus;
    MyListView shippingListView;
    ArrayList<ShippingMethodList> shippingMethodLists = new ArrayList<>();
    ShippingMethodsAdapter shippingMethodsAdapter;

    //PAYPAL
//    public static final String clientKey = "Aejdh4qkj0Kexwygz279msd3ac1BW8Suur42NIVNCZcnJ0lMMUHFtI11nTaR3SRy3i6Mfq_g7vfC6A-9";
//    public static final int PAYPAL_REQUEST_CODE = 2546;
    //Paypal Configuration Object
    /*private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment(ENVIRONMENT_NO_NETWORK).  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_PRODUCTION)
            .clientId(clientKey); */
//    private String paypal_amount = "0";
//    private String paypal_order_id = "0";

    LinearLayout couponLayout, rewardLayout;
    TextView rewardPriceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userSession = new UserSession(this.getApplicationContext());

        subtotalView = (TextView) findViewById(R.id.subtotalView);
        totalView = (TextView) findViewById(R.id.totalView);
        fullNameView = (TextView) findViewById(R.id.fullNameView);
        address1View = (TextView) findViewById(R.id.address1View);
        address2View = (TextView) findViewById(R.id.address2View);
        cityStateCountryView = (TextView) findViewById(R.id.cityStateCountryView);
        phoneView = (TextView) findViewById(R.id.phoneView);
//        paypalCheckView = (RadioButton) findViewById(R.id.paypalCheckView);
//        stripeCheckView = (RadioButton) findViewById(R.id.stripeCheckView);
        onDeliveryCheckView = (RadioButton) findViewById(R.id.onDeliveryCheckView);
        paymentMethod = (RadioGroup) findViewById(R.id.paymentMethod);
        confirmBtn = (Button) findViewById(R.id.confirmPayButton);
        applyCouponBtn = (Button) findViewById(R.id.applyCouponBtn);
        couponCode = (EditText) findViewById(R.id.couponCode);
        couponPriceView = (TextView) findViewById(R.id.couponPriceView);

        couponLayout = (LinearLayout) findViewById(R.id.couponLayout);
        rewardLayout = (LinearLayout) findViewById(R.id.rewardLayout);
        rewardPriceView = (TextView) findViewById(R.id.rewardPriceView);

        couponLayout.setVisibility(View.GONE);
        rewardLayout.setVisibility(View.GONE);

        shippingLayout = (LinearLayout) findViewById(R.id.shippingLayout);
        shippingMethodStatus = (TextView) findViewById(R.id.no_shipping_method);
        shippingMethodStatus.setVisibility(View.INVISIBLE);

        shippingListView = (MyListView) findViewById(R.id.shipping_list);
        shippingMethodsAdapter = new ShippingMethodsAdapter(this, shippingMethodLists);
        shippingListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        shippingListView.setAdapter(shippingMethodsAdapter);

       shippingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               changeShippingMethod(shippingMethodLists.get(i).getId());
           }
       });

        confirmBtn.setOnClickListener(confirmClicked);
        applyCouponBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyCouponCode();
            }
        });

        loader = ProgressDialog.show(this, "Loading", "Please wait...", true);
        fetchCartData(this); //to confirm if cart is not empty

        //start paypal service
//        Intent intent = new Intent(this, PayPalService.class);
//        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
//        startService(intent);

    }
    @Override
    public void onDestroy() {
//        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        switch (item.getItemId()) {
            case android.R.id.home:
                finish(); // close this activity and return to preview activity (if there is any)
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fetchCartData(Context context) {
        String url = Site.CART + userSession.userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseCartData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Alert");
            builder.setMessage("Unable to collect cart.");
            builder.setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //refresh this activity
                    startActivity(getIntent());
                    finish();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //go back to cart activity
                    startActivity(new Intent(PaymentActivity.this, MainActivity.class).putExtra("selected_tab", "cart"));
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            loader.hide();
            dialog.show();
        });
        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }

    @SuppressLint("SetTextI18n")
    public void parseCartData(String json) {
        try {
            JSONObject object = new JSONObject(json);
//            paymentAmount = object.getString("total");
            if (object.has("items")) {
                JSONArray array = object.getJSONArray("items");
                if (array.length() > 0) {
                    //cart items is not empty
                    //set views value for summary
//                    double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
//                    double totalDouble = Double.parseDouble(object.getString("total"));
//                    DecimalFormat formatter = new DecimalFormat("#,###.00");
                    subtotalView.setText(Site.CURRENCY + PriceFormatter.format(object.getString("subtotal")));
                    totalView.setText(Site.CURRENCY + PriceFormatter.format(object.getString("total")));
                    if (object.getString("has_coupon").equals("true")) {
                        double couponDiscount = Double.parseDouble(object.getString("coupon_discount"));
                        couponLayout.setVisibility(View.VISIBLE);
                        couponPriceView.setText("- " + Site.CURRENCY + PriceFormatter.format(couponDiscount));
                    } else {
                        couponLayout.setVisibility(View.GONE);
                    }
                    if (object.getString("apply_reward").equals("true")) {
                        double rewardDiscount = Double.parseDouble(object.getString("reward_discount"));
                        rewardLayout.setVisibility(View.VISIBLE);
                        rewardPriceView.setText("- " + Site.CURRENCY + PriceFormatter.format(rewardDiscount));
                    } else {
                        rewardLayout.setVisibility(View.GONE);
                    }
                    if (object.getString("has_shipping").equals("true")) {

                        JSONArray shipping_methods = object.getJSONArray("shipping_methods"); //Skye API V2

                        for (int i = 0; i < shipping_methods.length(); i++) {
                            JSONObject method = shipping_methods.getJSONObject(i);
                            shippingMethodLists.add(new ShippingMethodList(method.getString("title"), method.getString("rate_id"), method.getString("cost")));
                        }

                        shippingMethodsAdapter = new ShippingMethodsAdapter(this, shippingMethodLists);
                        shippingListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                        shippingListView.setAdapter(shippingMethodsAdapter);


                        if (shippingMethodLists.size() > 0) {
                            shippingListView.setItemChecked(0, true);
                            shippingMethodStatus.setVisibility(View.GONE);
                        } else {
                            shippingMethodStatus.setVisibility(View.VISIBLE);
                        }



                    } else {
                        shippingLayout.setVisibility(View.GONE);
                    }
                    //fetch address
                    fetchAddress();
                } else {
                    //cart items is empty
                    cartItemIsEmpty();
                }
            } else {
                //cart items is empty
                cartItemIsEmpty();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //when fetching is ready
    }

    public void cartItemIsEmpty() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Oops!");
        builder.setMessage("Your Cart is empty. Please go back and select product to buy.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //go back to shop
                finish();
                startActivity(new Intent(PaymentActivity.this, MainActivity.class).putExtra("selected_tab", "shop"));
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        loader.hide();
        dialog.show();
    }

    public void fetchAddress() {
        loader.show();
        String url = Site.USER + userSession.userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(String response) {
                JSONObject object = null;
                try {
                    object = new JSONObject(response);
                    JSONObject address = new JSONObject(object.getString("shipping_address"));
                    fullNameView.setText(address.getString("shipping_first_name") + " " + address.getString("shipping_last_name"));
                    address1View.setText(address.getString("shipping_address_1"));
                    address2View.setText(address.getString("shipping_address_2"));
                    cityStateCountryView.setText(address.getString("shipping_city") + " " + address.getString("shipping_state") + ", " + address.getString("shipping_country"));
                    phoneView.setText(address.getString("shipping_phone"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loader.hide();
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(PaymentActivity.this, "Can't fetch your address, Try to go back.", Toast.LENGTH_LONG).show();
            loader.hide();
        });
        RequestQueue rQueue = Volley.newRequestQueue(PaymentActivity.this);
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }

    public View.OnClickListener confirmClicked = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
//            Toast.makeText(PaymentActivity.this, "Opening Payment Gateway...", Toast.LENGTH_LONG).show();
////            createOrder("web", "wc-pending");
//            String url = Site.COMPLETE_ORDER_PAGE + "?user=" + userSession.userID + "&clear_cart=1";
//            //add current timestamp to prevent repeating one page cache eg webview
//            long timestamp = System.currentTimeMillis()/1000;
//            url += "&timestamp=" + Long.toString(timestamp);
//
//            startActivity(new Intent(PaymentActivity.this, StripeWebPay.class)
//                                    .putExtra("url", url));




            //get payment method
//            switch (paymentMethod.getCheckedRadioButtonId()) {
//                case R.id.onDeliveryCheckView:
//                    createOrder("cod", "wc-processing");
//                    break;
//                case R.id.paypal:
//                    createOrder("paypal", "wc-on-hold");
//                    break;
//                case R.id.credit_cards:
//                    createOrder("stripe_cc", "wc-pending");
//                    break;
//                default:
//                    createOrder("web", "wc-pending");
//                    break;
//            }

            createOrder("web", "wc-pending");

        }
    };

    public void createOrder(String paymentMethod, String status) {
        String post_url = Site.CREATE_ORDER + userSession.userID;
        loader.setMessage("Creating your order...");
        loader.show();
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    URL url = new URL(post_url);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
//                    jsonParam.put("payment_method", paymentMethod); //no payment method set since it's web pay
                    jsonParam.put("status", status);
                    if (!paymentMethod.equals("web")) {
                        jsonParam.put("payment_method", paymentMethod);
                    }
                    if (!paymentMethod.equals("paypal")) { //dont first clear cart of paypal
                        jsonParam.put("clear_cart", "1"); //clear cart after order created
                    }


//                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

//                    Log.e("STATUS", String.valueOf(conn.getResponseCode()));
//                    Log.e("MSG" , conn.getResponseMessage());

                    BufferedReader br = new BufferedReader(new InputStreamReader( conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder json = new StringBuilder();
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        json.append(line + "\n");
                    }
                    br.close();
                    //parse the json to JSONObject
//                    Log.e("RESPONSE" , json.toString());
                    if (isJSONValid(json.toString())) {
                        JSONObject object = new JSONObject(json.toString());
                        if (object.getString("cart_empty").equals("true") || object.getString("cart_exists").equals("false") || !object.has("info")) {
                            //cannot create order because cart is empty or not found
                            AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
                            builder.setCancelable(false);
                            builder.setTitle("Alert");
                            builder.setMessage("Cannot create order because cart is empty or not found.");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //refresh this activity
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog dialog = builder.create();
                            loader.hide();
                            dialog.show();
                        } else {
                            //order created
                            if (paymentMethod.equals("web")) {
                                //get pending payment url and parse to StripeWebPay
                                JSONObject info = new JSONObject(object.getString("info"));
                                String checkout_url = info.getString("checkout_payment_url").replace("localhost", Site.DOMAIN);
                                checkout_url += "&sk-web-payment=1&sk-user-checkout=" + userSession.userID;
//                                checkout_url += "&in_sk_app=1";
//                                checkout_url += "&hide_elements=div*topbar.topbar, div.joinchat__button";
                                startActivity(new Intent(PaymentActivity.this, StripeWebPay.class)
                                        .putExtra("url", checkout_url));
                                finish();
//                            } else if (paymentMethod.equals("paypal")) {
//                                //handle paypal gateway
//                                JSONObject info = new JSONObject(object.getString("info"));
//                                paypal_amount = info.getString("total");
//                                paypal_order_id = info.getString("ID");
//                                paypalAction(paypal_order_id, paypal_amount);
                            } else if (paymentMethod.equals("stripe_cc") || paymentMethod.equals("stripe")) {
                                //get pending payment url and parse to StripeWebPay
                                JSONObject info = new JSONObject(object.getString("info"));
                                String checkout_url = info.getString("checkout_payment_url").replace("localhost", Site.DOMAIN);
                                checkout_url += "&sk-web-payment=1&sk-stripe-checkout=1&sk-user-checkout=" + userSession.userID;
//                                checkout_url += "&in_sk_app=1";
//                                checkout_url += "&hide_elements=div*topbar.topbar, div.joinchat__button";
                                startActivity(new Intent(PaymentActivity.this, StripeWebPay.class)
                                        .putExtra("url", checkout_url));
                                finish();
                            } else {
                                if (status.equals("wc-processing")) {
                                    startActivity(new Intent(PaymentActivity.this, OrderPlacedActivity.class).putExtra("created", "sure"));
                                } else {
                                    //go to pending order page
                                    startActivity(new Intent(PaymentActivity.this, MainActivity.class).putExtra("selected_tab", "orders_pending"));
                                    //                                        Toast.makeText(PaymentActivity.this, "Go to Pending Orders", Toast.LENGTH_LONG).show();
                                }
                                finish();
                            }
                            loader.hide();
                            finish();
                        }
                    } else {
                        startActivity(new Intent(PaymentActivity.this, OrderPlacedActivity.class).putExtra("created", "unsure"));
                        finish();
                    }


                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. eg case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }
    public void applyCouponCode() {
        loader.show();
        String coupon = (isEmpty(couponCode)) ? "null" : couponCode.getText().toString();
        String url = Site.UPDATE_COUPON + userSession.userID + "/" + coupon;
        JSONObject data = new JSONObject();
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
                        try {
                            JSONObject object = new JSONObject(response.toString());
                            if (object.getString("has_coupon").equals("true")) {
                                double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
                                double totalDouble = Double.parseDouble(object.getString("total"));
                                double couponDiscountDouble = Double.parseDouble(object.getString("coupon_discount"));
//                                DecimalFormat formatter = new DecimalFormat("#,###.00");
                                subtotalView.setText(Site.CURRENCY + PriceFormatter.format(subtotalDouble));
                                couponPriceView.setText("- " + Site.CURRENCY + PriceFormatter.format(couponDiscountDouble));
                                totalView.setText(Site.CURRENCY + PriceFormatter.format(totalDouble));
//                                paymentAmount = object.getString("total"); //change the global payment amount
                                Toast.makeText(PaymentActivity.this, "Coupon Applied!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(PaymentActivity.this, "Invalid Coupon!", Toast.LENGTH_LONG).show();
                            }
                            loader.hide();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(PaymentActivity.this, "Unable to apply coupon.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();
                loader.hide();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(PaymentActivity.this);
        postRequest.setShouldCache(false);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }
    public void changeShippingMethod(String method) {
        loader.show();
        String url = Site.CHANGE_CART_SHIPPING + userSession.userID + "/" + method;
        JSONObject data = new JSONObject();
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
                        try {
                            JSONObject object = new JSONObject(response.toString());
                            if (object.getString("has_shipping").equals("true")) {
                                double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
                                double totalDouble = Double.parseDouble(object.getString("total"));
                                double couponDiscountDouble = Double.parseDouble(object.getString("coupon_discount"));
//                                DecimalFormat formatter = new DecimalFormat("#,###.00");
                                subtotalView.setText(Site.CURRENCY + PriceFormatter.format(subtotalDouble));
                                couponPriceView.setText("- " + Site.CURRENCY + PriceFormatter.format(couponDiscountDouble));
                                totalView.setText(Site.CURRENCY + PriceFormatter.format(totalDouble));
//                                paymentAmount = object.getString("total"); //change the global payment amount
                            } else {
                                Toast.makeText(PaymentActivity.this, "Cart has no shipping go back and update your address!", Toast.LENGTH_LONG).show();
                            }
                            loader.hide();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(PaymentActivity.this, "Unable to update shipping method!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();
                loader.hide();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(PaymentActivity.this);
        postRequest.setShouldCache(false);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }


//    public void paypalAction(String orderID, String amount) {
//        loader.setMessage("Starting Paypal..");
//        loader.show();
//        //if global paypal_amount and paypal_order_id are not zeor(0)
//        if (paypal_amount.equals("0") || paypal_order_id.equals("0")) {
//            Toast.makeText(this, "Can't process amount and your order.", Toast.LENGTH_LONG).show();
//            return;
//        }
//        // Creating a paypal payment on below line.
//        PayPalPayment payment = new PayPalPayment(new BigDecimal(amount), "USD", "Order #" + orderID + " Fees",
//                PayPalPayment.PAYMENT_INTENT_SALE);
//        // Creating Paypal Payment activity intent
//        Intent intent = new Intent(this, com.paypal.android.sdk.payments.PaymentActivity.class);
//        //putting the paypal configuration to the intent
//        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
//        // Putting paypal payment to the intent
//        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
//        // Starting the intent activity for result
//        // the request code will be used on the method onActivityResult
//        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
//    }
        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
        //If the result is from paypal
//        if (requestCode == PAYPAL_REQUEST_CODE) {
//
//            //If the result is OK i.e. user has not canceled the payment
//            if (resultCode == Activity.RESULT_OK) {
//                //Getting the payment confirmation
//                PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
//
//                //if confirmation is not null
//                if (confirm != null) {
//                    try {
//                        //Getting the payment details
//                        String paymentDetails = confirm.toJSONObject().toString(4);
////                        Log.i("paymentExample", paymentDetails);
//
//                        //Starting a new activity for the payment details and also putting the payment details with intent
//                        //response sample
////                        {
////                            "client": {
////                            "environment": "sandbox",
////                                    "paypal_sdk_version": "2.0.0",
////                                    "platform": "iOS",
////                                    "product_name": "PayPal iOS SDK;"
////                        },
////                            "response": {
////                            "create_time": "2014-02-12T22:29:49Z",
////                                    "id": "PAY-564191241M8701234KL57LXI",
////                                    "intent": "sale",
////                                    "state": "approved"
////                        },
////                            "response_type": "payment"
////                        }
//
//                        JSONObject jsonDetails = new JSONObject(paymentDetails);
//                        JSONObject paypalResponse = jsonDetails.getJSONObject("response");
//                        if (paypalResponse.getString("state").toLowerCase().equals("approved")) {
//                            paypalUpdateOrder(paypal_order_id, "wc-processing", paypalResponse.getString("id"));
//                            Toast.makeText(PaymentActivity.this, "Payment Approved!", Toast.LENGTH_LONG).show();
//                        } else if (paypalResponse.getString("state").toLowerCase().equals("created")) {
//                            paypalUpdateOrder(paypal_order_id, "wc-pending", paypalResponse.getString("id"));
//                            Toast.makeText(PaymentActivity.this, "Payment Pending!", Toast.LENGTH_LONG).show();
//                        } else {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
//                            builder.setCancelable(false);
//                            builder.setTitle("Payment Error");
//                            builder.setMessage(" ID: " + paypalResponse.getString("id") + "\n \n Status: " +paypalResponse.getString("state") + "\n \n Amount: " + paypal_amount);
//                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    //refresh this activity
//                                    dialog.dismiss();
//                                }
//                            });
//
//                            AlertDialog dialog = builder.create();
//                            loader.hide();
//                            dialog.show();
//                        }
//
//                    } catch (JSONException e) {
//                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
//                    }
//                }
//            } else if (resultCode == Activity.RESULT_CANCELED) {
////                Log.i("paymentExample", "The user canceled.");
//                Toast.makeText(PaymentActivity.this, "You canceled payment.", Toast.LENGTH_LONG).show();
//                loader.hide();
//            } else if (resultCode == com.paypal.android.sdk.payments.PaymentActivity.RESULT_EXTRAS_INVALID) {
////                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
//                Toast.makeText(PaymentActivity.this, "An invalid Payment or PayPalConfiguration was submitted.", Toast.LENGTH_LONG).show();
//                loader.hide();
//            }
//        }
    }
//    public void paypalUpdateOrder(String orderID, String order_status, String paymentID) {
//        loader.setMessage("Completing your Order...");
//        loader.show();
//        try {
//            String url = Site.UPDATE_ORDER + orderID + "/" + userSession.userID;
//            JSONObject data = new JSONObject();
//            data.put("status", order_status);
//            data.put("paypal_payment_id", paymentID);
//            data.put("clear_cart", "1"); //clear cart after order updated
//
//            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
//                    new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject response) { //response.toString() to get json as string
//                            try {
//                                if (response.getString("order_exists").equals("true") && response.has("order_info")) {
//                                    JSONObject info = response.getJSONObject("order_info");
//                                    if (order_status.equals("wc-processing")) {
//                                        Toast.makeText(PaymentActivity.this, "Order placed!", Toast.LENGTH_LONG).show();
//                                        startActivity(new Intent(PaymentActivity.this, OrderPlacedActivity.class).putExtra("created", "sure"));
//                                    } else {
//                                        startActivity(new Intent(PaymentActivity.this, OrdersFragment.class).putExtra("order_status", order_status));
//                                    }
//                                    finish();
//                                } else {
//                                    AlertDialog alertDialog = new AlertDialog.Builder(PaymentActivity.this).create();
//                                    alertDialog.setTitle("Ops!!!");
//                                    alertDialog.setMessage("Can't update your order payment status.. Please contact us.");
//                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                                            new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    dialog.dismiss();
//                                                }
//                                            });
//                                    alertDialog.show();
//                                    loader.hide();
//                                }
//
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    AlertDialog alertDialog = new AlertDialog.Builder(PaymentActivity.this).create();
//                    alertDialog.setTitle("Ops!!!");
//                    alertDialog.setMessage("Can't update your order payment status.. Please contact us.");
//                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                    alertDialog.show();
//                    loader.hide();
//                }
//            });
//            RequestQueue rQueue = Volley.newRequestQueue(this.getApplicationContext());
//            postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//            rQueue.add(postRequest);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}