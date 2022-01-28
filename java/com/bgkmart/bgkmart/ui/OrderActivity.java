package com.fatima.fabric.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fatima.fabric.R;
import com.fatima.fabric.Site;
import com.fatima.fabric.adapter.OrderDetailsAdapter;
import com.fatima.fabric.adapter.OrderDetailsList;
import com.fatima.fabric.handler.UserSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class OrderActivity extends AppCompatActivity {

    UserSession userSession;
    String order_id;
    ProgressBar progressBar;
    Button refreshBtn;
    TextView orderInfo, subtotalView, discountView, totalView;
    MyListView orderDetailsListView;
    LinearLayout orderDetailsLayout;
    ArrayList<OrderDetailsList> orderDetailsLists;
    OrderDetailsAdapter orderDetailsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        userSession = new UserSession(this.getApplicationContext());

        order_id = getIntent().getStringExtra("order_id");
        getSupportActionBar().setTitle("Order #" + order_id);

        progressBar = (ProgressBar) findViewById(R.id.orderProgressBar);
        refreshBtn = (Button) findViewById(R.id.refreshOrderBtn);
        refreshBtn.setVisibility(View.GONE);
        orderInfo = (TextView) findViewById(R.id.orderInfo);
        subtotalView = (TextView) findViewById(R.id.subtotalView);
        discountView = (TextView) findViewById(R.id.discountPriceView);
        totalView = (TextView) findViewById(R.id.totalView);
        orderDetailsListView = (MyListView) findViewById(R.id.orderDetailsListView);
        orderDetailsLayout = (LinearLayout) findViewById(R.id.orderDetailsLayout);
        orderDetailsLayout.setVisibility(View.GONE);

        orderDetailsLists = new ArrayList<>();
        orderDetailsAdapter = new OrderDetailsAdapter(this, orderDetailsLists);
        orderDetailsListView.setAdapter(orderDetailsAdapter);

        fetchOrder();

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
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
        orderDetailsLayout.setVisibility(View.GONE);
        String url = Site.ORDER + order_id + "/" + userSession.userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(OrderActivity.this, "Unable to get order.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            orderDetailsLayout.setVisibility(View.GONE);
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
                progressBar.setVisibility(View.GONE);
                refreshBtn.setVisibility(View.GONE);
                orderDetailsLayout.setVisibility(View.GONE);
            } else {
                JSONObject dateObject = new JSONObject(object.getString("date_created"));
//                    @SuppressLint("SimpleDateFormat") Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dateObject.getString("date"));
                String statusText = "Unknown status";
                switch (object.getString("status")) {
                    case "pending":
                        statusText = "Pending payment";
                        break;
                    case "processing":
                        statusText = "Processing";
                        break;
                    case "completed":
                        statusText = "Completed";
                        break;
                    default:
                        break;
                }
                orderInfo.setText("Order #" + object.getString("ID") + " was placed on " + dateObject.getString("date") + " and is currently " + statusText + ".");
                JSONArray products = object.getJSONArray("products");
                for (int i = 0; i < products.length(); i++) {
                    JSONObject product = products.getJSONObject(i);
                    OrderDetailsList list = new OrderDetailsList();
                    list.setId(product.getString("ID"));
                    list.setName(product.getString("name"));
                    list.setCount(product.getString("quantity"));
                    list.setPrice(product.getString("subtotal"));
                    orderDetailsLists.add(list);
                }

                orderDetailsAdapter.notifyDataSetChanged();
//                double subtotal = Double.parseDouble(object.getString("subtotal"));
//                double discount = Double.parseDouble(object.getString("total_discount"));
//                double total = Double.parseDouble(object.getString("total"));
                DecimalFormat formatter = new DecimalFormat("#,###.00");
                subtotalView.setText("$" + object.getString("subtotal"));
                discountView.setText("$" + object.getString("total_discount"));
                totalView.setText("$" + object.getString("total"));

                progressBar.setVisibility(View.GONE);
                refreshBtn.setVisibility(View.GONE);
                orderDetailsLayout.setVisibility(View.VISIBLE);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}