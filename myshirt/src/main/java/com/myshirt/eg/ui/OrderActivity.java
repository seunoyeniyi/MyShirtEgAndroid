package com.myshirt.eg.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kofigyan.stateprogressbar.StateProgressBar;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.adapter.OrderDetailsAdapter;
import com.myshirt.eg.adapter.OrderDetailsList;
import com.myshirt.eg.handler.PriceFormatter;
import com.myshirt.eg.handler.UserSession;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class OrderActivity extends AppCompatActivity {

    UserSession userSession;
    String order_id;
    ShimmerFrameLayout progressBar;
    Button refreshBtn;
    Button viewLocation;
    TextView orderInfo, subtotalView, discountView, totalView, shippingPriceView;
    MyListView orderDetailsListView;
    LinearLayout orderDetailsLayout, shippingLayout;
    ArrayList<OrderDetailsList> orderDetailsLists;
    OrderDetailsAdapter orderDetailsAdapter;
    TextView tracking_provider, tracking_number;
    TextView fullName, company, address1, address2, city, state, postcode, country, phone, email;
    Button payNow;
    StateProgressBar orderStateProgressBar;
    String[] orderStateDescription = {"Received", "Processed", "Shipped", "Out for delivery", "Delivered"};

    boolean trackingAvailable = false;

    //IN THIS APP - WE ARE USING ZOREM WP ADVANCE SHIPMENT PLUGIN
    JSONObject shipment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        userSession = new UserSession(this.getApplicationContext());

        order_id = getIntent().getStringExtra("order_id");
        getSupportActionBar().setTitle("Order #" + order_id);

        payNow = (Button) findViewById(R.id.payNow);
        viewLocation = (Button) findViewById(R.id.viewLocation);
        progressBar = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startShimmer();
        refreshBtn = (Button) findViewById(R.id.refreshOrderBtn);
        refreshBtn.setVisibility(View.GONE);
        orderInfo = (TextView) findViewById(R.id.orderInfo);
        subtotalView = (TextView) findViewById(R.id.subtotalView);
        discountView = (TextView) findViewById(R.id.discountPriceView);
        shippingPriceView = (TextView) findViewById(R.id.shippingPriceView);
        totalView = (TextView) findViewById(R.id.totalView);
        orderDetailsListView = (MyListView) findViewById(R.id.orderDetailsListView);
        orderDetailsLayout = (LinearLayout) findViewById(R.id.orderDetailsLayout);
        shippingLayout = (LinearLayout) findViewById(R.id.shippingLayout);
        orderDetailsLayout.setVisibility(View.GONE);
        shippingLayout.setVisibility(View.GONE);

        tracking_provider = (TextView) findViewById(R.id.tracking_provider);
        tracking_number = (TextView) findViewById(R.id.tracking_number);

        fullName = (TextView) findViewById(R.id.shippingFullname);
        company = (TextView) findViewById(R.id.shippingCompany);
        address1 = (TextView) findViewById(R.id.shippingStreet);
        address2 = (TextView) findViewById(R.id.shippingApartment);
        city = (TextView) findViewById(R.id.shippingCity);
        state = (TextView) findViewById(R.id.shippingState);
        postcode = (TextView) findViewById(R.id.shippingPostCode);
        country = (TextView) findViewById(R.id.shippingCountry);
        phone = (TextView) findViewById(R.id.shippingPhone);
        email = (TextView) findViewById(R.id.shippingEmail);

        orderStateProgressBar = (StateProgressBar) findViewById(R.id.your_state_progress_bar_id);
        orderStateProgressBar.setStateDescriptionData(orderStateDescription);

        payNow.setVisibility(View.GONE);

        orderDetailsLists = new ArrayList<>();
        orderDetailsAdapter = new OrderDetailsAdapter(this, orderDetailsLists);
        orderDetailsListView.setAdapter(orderDetailsAdapter);

        fetchOrder();

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.startShimmer();
                fetchOrder();
            }
        });
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void fetchOrder() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startShimmer();
        orderDetailsLayout.setVisibility(View.GONE);
        shippingLayout.setVisibility(View.GONE);
        String url = Site.ORDER + order_id + "/" + userSession.userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(OrderActivity.this, "Unable to get order.", Toast.LENGTH_LONG).show();
            progressBar.stopShimmer();
            progressBar.setVisibility(View.GONE);
            orderDetailsLayout.setVisibility(View.GONE);
            shippingLayout.setVisibility(View.GONE);
            refreshBtn.setVisibility(View.VISIBLE);
        });
        RequestQueue rQueue = Volley.newRequestQueue(OrderActivity.this);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.has("code") && object.getString("data").equals("null")) {
                Toast.makeText(OrderActivity.this, object.getString("message"), Toast.LENGTH_LONG).show();
                progressBar.startShimmer();
                progressBar.setVisibility(View.GONE);
                refreshBtn.setVisibility(View.GONE);
                orderDetailsLayout.setVisibility(View.GONE);
                shippingLayout.setVisibility(View.GONE);
            } else {
//                JSONObject dateObject = new JSONObject(object.getString("date_created"));
//                    @SuppressLint("SimpleDateFormat") Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dateObject.getString("date"));
                String statusText = "Unknown status";
                switch (object.getString("status")) {
                    case "pending":
                        statusText = "Pending payment";
                        break;
                    case "processing":
                        statusText = "Processing";
                        orderStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                        break;
                    case "shipped":
                        statusText = "Shipped";
                        orderStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
                    case "completed":
                        statusText = "Completed";
                        orderStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FIVE);
                        break;
                    case "on-hold":
                        statusText = "On hold";
                        break;
                    case "cancelled":
                        statusText = "Cancelled";
                        break;
                    case "refunded":
                        statusText = "Refunded";
                        break;
                    default:
                        break;
                }
                if (object.getString("payment_method").equals("paypal") && object.getString("status").equals("on-hold")) {
                    statusText = "Paypal Cancelled";
                }

                orderInfo.setText("Order #" + object.getString("ID") + " was placed on " + object.getString("date_modified_date") + " and is currently " + statusText + ".");



                if (object.getString("status").equals("pending")) {
                    payNow.setVisibility(View.VISIBLE);
                    payNow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String checkout_url = "";
                            try {
                                checkout_url = object.getString("checkout_payment_url").replace("localhost", Site.DOMAIN);
                                if (object.getString("payment_method").equals("stripe_cc") || object.getString("payment_method").equals("stripe")) {
                                    checkout_url += "&sk-web-payment=1&sk-stripe-checkout=1&sk-user-checkout=" + userSession.userID; //for stripe only
                                } else {
                                    checkout_url += "&sk-web-payment=1&sk-user-checkout=" + userSession.userID; //for any payment method
                                }
                                checkout_url += "&in_sk_app=1";
                                checkout_url += "&hide_elements=div*topbar.topbar, div.joinchat__button, *notificationx-frontend-root";
//                                Log.e("URL", checkout_url);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivity(new Intent(OrderActivity.this, StripeWebPay.class).putExtra("url", checkout_url));
                        }
                    });
                }





                JSONArray products = object.getJSONArray("products");
                for (int i = 0; i < products.length(); i++) {
                    JSONObject product = products.getJSONObject(i);
                    OrderDetailsList list = new OrderDetailsList();
//                    list.setId(product.getString("ID"));
                    list.setName(product.getString("name"));
                    list.setCount(product.getString("quantity"));
                    list.setPrice(product.getString("subtotal"));
                    orderDetailsLists.add(list);
                }

                orderDetailsAdapter.notifyDataSetChanged();
//                double subtotal = Double.parseDouble(object.getString("subtotal"));
//                double discount = Double.parseDouble(object.getString("total_discount"));
//                double total = Double.parseDouble(object.getString("total"));
//                DecimalFormat formatter = new DecimalFormat("#,###.00");
                subtotalView.setText(Site.CURRENCY + PriceFormatter.format(object.getString("subtotal")));
                double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
                double totalDouble = Double.parseDouble(object.getString("total"));
                double discountTotal = totalDouble - subtotalDouble;

                discountView.setText(Site.CURRENCY + PriceFormatter.format((discountTotal < 0) ? discountTotal : 0)); //if discount is negative only
                shippingPriceView.setText(Html.fromHtml(object.getString("shipping_to_display")));
                totalView.setText(Site.CURRENCY + PriceFormatter.format(object.getString("total")));
                //set shipping address info
                fullName.setText(object.getString("shipping_first_name") + " " + object.getString("shipping_last_name"));
                company.setText(object.getString("shipping_company"));
                address1.setText(object.getString("shipping_address_1"));
                address2.setText(object.getString("shipping_address_2"));
                city.setText(object.getString("shipping_city"));
                state.setText(object.getString("shipping_state"));
                postcode.setText(object.getString("shipping_postcode"));
                country.setText(object.getString("shipping_country"));
                phone.setText(object.getString("billing_phone")); //shipping does not have phone
                email.setText(object.getString("billing_email")); //shipping does not have email


                //IN THIS APP - WE ARE USING ZOREM WP ADVANCE SHIPMENT PLUGIN
                if (object.has("zorem_tracking_items")) {
                    JSONArray shipping_items = object.getJSONArray("zorem_tracking_items");
                    if (shipping_items.length() > 0) {
                        trackingAvailable = true;
                        shipment = shipping_items.getJSONObject(0); //use the first tracking item
                        tracking_provider.setText(Html.fromHtml("Tracking Provider: <b>" + shipment.getString("formatted_tracking_provider") + "</b>"));
                        tracking_number.setText(Html.fromHtml("Tracking Number: <b>" + shipment.getString("tracking_number") + "</b>"));
                        tracking_provider.setVisibility(View.VISIBLE);
                        tracking_number.setVisibility(View.VISIBLE);
                    }
                }

                viewLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        try {

                            //THIS APP IS USING ZOREM WP ADVANCE SHIPMENT PLUGIN -- skye driver will be comment out
                            //for skye driver method
//                            if (!object.getString("skye_driver_location").isEmpty()) {
//                                JSONObject driver_location = new JSONObject(object.getString("skye_driver_location"));
//                                String address = driver_location.getString("address");
//                                String latitude = driver_location.getString("latitude");
//                                String longitude = driver_location.getString("longitude");
//
//                                //to avoid app crash of empty string
//                                Intent mapIntent = new Intent(OrderActivity.this, MapsActivity.class);
//                                if (!address.isEmpty()) {
//                                    mapIntent.putExtra("address", address);
//                                    trackingAvailable = true;
//                                }
//                                if (!latitude.isEmpty() && !longitude.isEmpty()) {
//                                    mapIntent.putExtra("latitude", longitude);
//                                    mapIntent.putExtra("longitude", longitude);
//                                    trackingAvailable = true;
//                                }
//                                if (trackingAvailable)
//                                    startActivity(mapIntent);
//                            }

                            //for zorem shipment plugin

                            if (shipment != null) {
                                    Intent intent = new Intent(OrderActivity.this, BrowserActivity.class);
                                    intent.putExtra("url", shipment.getString("ast_tracking_link"));
                                    intent.putExtra("title", "Track Order");
                                    startActivity(intent);

                                    trackingAvailable = true;
                                }



                            if (!trackingAvailable) {
                                Toast.makeText(OrderActivity.this, "Your order is not yet picked!", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });




                progressBar.startShimmer();
                progressBar.setVisibility(View.GONE);
                refreshBtn.setVisibility(View.GONE);
                orderDetailsLayout.setVisibility(View.VISIBLE);
                shippingLayout.setVisibility(View.VISIBLE);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



}