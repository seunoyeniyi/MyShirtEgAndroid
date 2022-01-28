package com.myshirt.eg.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.myshirt.eg.MainActivity;
import com.myshirt.eg.Site;
import com.myshirt.eg.adapter.ProductGridAdapter;
import com.myshirt.eg.adapter.ProductList;
import com.myshirt.eg.handler.UpdateCartCount;
import com.myshirt.eg.handler.UserSession;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myshirt.eg.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class Search extends AppCompatActivity {

    TextView cartCounter;
    MyGridView productGridView;
    ArrayList<ProductList> productLists;
    ProductGridAdapter productAdapter;
    ProgressBar shopProgressBar;
    Button refreshBtn;
    String searchQuery = "";
    UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        userSession = new UserSession(getApplicationContext());

        searchQuery = (getIntent().hasExtra("search_query")) ? getIntent().getStringExtra("search_query") : "";
        getSupportActionBar().setTitle(Html.fromHtml(searchQuery));

        productGridView = (MyGridView) findViewById(R.id.productGridView);
        shopProgressBar = (ProgressBar) findViewById(R.id.shopProgessBar);
        refreshBtn = (Button) findViewById(R.id.refreshBtn);


        productLists = new ArrayList<>();
        fetchData(this);

        //to refresh
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shopProgressBar.setVisibility(View.VISIBLE);
                fetchData(Search.this);
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem item = menu.findItem(R.id.cartMenuIcon);
        item.setActionView(R.layout.cart_icon_update_layout);
        RelativeLayout cartCount = (RelativeLayout)   item.getActionView();
        cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
        cartCounter.setText("0");
        cartCounter.setVisibility(View.GONE);
        new UpdateCartCount(this, userSession.userID, cartCounter);
        cartCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(item);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        switch (item.getItemId()) {
            case android.R.id.home:
                finish(); // close this activity and return to preview activity (if there is any)
                break;
            case R.id.cartMenuIcon:{
                Intent intent = new Intent(Search.this, MainActivity.class);
                intent.putExtra("selected_tab", "cart");
                startActivity(intent);
            }
            break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void  fetchData(Context context) {
        String url = Site.SIMPLE_PRODUCTS + "?search=" + searchQuery + "&show_variation=1&user_id=" + userSession.userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(context, "Unable to get products.", Toast.LENGTH_LONG).show();
            shopProgressBar.setVisibility(View.GONE);
            refreshBtn.setVisibility(View.VISIBLE);
        });
        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("results");


            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String id = jsonObject.getString("ID");
                String name = jsonObject.getString("name");
//                String title = jsonObject.getString("title");
                String image = jsonObject.getString("image");
                String price = jsonObject.getString("price");
                String regular_price = jsonObject.getString("regular_price");
                String product_type = jsonObject.getString("product_type");
                String type = jsonObject.getString("type");
                String description = jsonObject.getString("description");
                String in_wishlist = jsonObject.getString("in_wishlist");
                String categories = jsonObject.getString("categories");
                JSONArray attributes = jsonObject.getJSONArray("attributes");
                JSONArray variations = jsonObject.getJSONArray("variations");
//
                ProductList list = new ProductList();
                list.setId(id);
//                list.setTitle(title);
                list.setName(name);
                list.setImage(image);
                list.setPrice(price);
                list.setRegular_price(regular_price);
                list.setType(type);
                list.setProduct_type(product_type);
                list.setDescription(description);
                list.setIn_wish_list(in_wishlist);
                list.setCategories(categories);
                list.setAttributes(attributes);
                list.setVariations(variations);
                productLists.add(list);
            }
//            Toast.makeText(getActivity(), String.valueOf(cartList.size()), Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //when fetching is ready
        productAdapter = new ProductGridAdapter(Search.this, productLists, cartCounter);
        productGridView.setAdapter(productAdapter);
        shopProgressBar.setVisibility(View.GONE);
        refreshBtn.setVisibility(View.GONE);


    }
}