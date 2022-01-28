package com.fatima.fabric.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fatima.fabric.R;
import com.fatima.fabric.Site;
import com.fatima.fabric.adapter.OrderList;
import com.fatima.fabric.adapter.OrderListAdapter;
import com.fatima.fabric.handler.UserSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class OrdersActivity extends AppCompatActivity {
    String order_status;
    MyListView orderListView;
    ArrayList<OrderList> orderLists;
    OrderListAdapter orderListAdapter;
    ProgressBar progressBar;
    Button refreshBtn;
    UserSession userSession;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        userSession = new UserSession(this.getApplicationContext());

        order_status = (getIntent().hasExtra("order_status")) ? getIntent().getStringExtra("order_status") : "all";

        orderListView = (MyListView) findViewById(R.id.orderListView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        refreshBtn = (Button) findViewById(R.id.refreshOrdersBtn);

        orderLists = new ArrayList<>();
        orderListAdapter = new OrderListAdapter(this, orderLists);
        orderListView.setAdapter(orderListAdapter);




        //change
        String activityTitle = "Orders";
        switch (order_status) {
            case "complete":
                activityTitle = "Completed Orders";
                break;
            case "processing":
                activityTitle = "Processing Orders";
                break;
            case "pending":
                activityTitle = "Pending Orders";
                break;
            default:
                break;
        }
        getSupportActionBar().setTitle(activityTitle);

        fetchOrders();

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                fetchOrders();
            }
        });
        orderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(OrdersActivity.this, OrderActivity.class).putExtra("order_id", orderLists.get(i).getId()));
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

    public void fetchOrders() {
        progressBar.setVisibility(View.VISIBLE);
        String url = Site.ORDERS + userSession.userID;
        switch (order_status) {
            case "complete":
                url = Site.ORDERS + userSession.userID + "?status=wc-completed";
                break;
            case "processing":
                url = Site.ORDERS + userSession.userID + "?status=wc-processing";
                break;
            case "pending":
                url = Site.ORDERS + userSession.userID + "?status=wc-pending";
                break;
            default:
                break;
        }
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(OrdersActivity.this, "Unable to get orders.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            refreshBtn.setVisibility(View.VISIBLE);
        });
        RequestQueue rQueue = Volley.newRequestQueue(OrdersActivity.this);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("orders");


            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String id = jsonObject.getString("ID");
                String date = jsonObject.getString("date_created");
                String status = jsonObject.getString("status");
                String price = jsonObject.getString("total");
                String item_count = jsonObject.getString("item_count");
//
                OrderList list = new OrderList();
                list.setId(id);
                list.setDate(date);
                list.setStatus(status);
                list.setPrice(price);
                list.setItem_count(item_count);
                orderLists.add(list);
            }
//            Toast.makeText(getActivity(), String.valueOf(cartList.size()), Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //when fetching is ready
        orderListAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        refreshBtn.setVisibility(View.GONE);


    }
}