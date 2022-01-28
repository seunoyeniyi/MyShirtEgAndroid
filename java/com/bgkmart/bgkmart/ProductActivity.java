package com.fatima.fabric;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fatima.fabric.adapter.AttributesList;
import com.fatima.fabric.adapter.AttributesListAdapter;
import com.fatima.fabric.adapter.ProductGridAdapter;
import com.fatima.fabric.adapter.ProductList;
import com.fatima.fabric.handler.AddToCartWithUpdateCartCount;
import com.fatima.fabric.handler.GetVariationPriceView;
import com.fatima.fabric.handler.UpdateCartCount;
import com.fatima.fabric.handler.UserSession;
import com.fatima.fabric.ui.MyGridView;
import com.fatima.fabric.ui.MyListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ProductActivity extends AppCompatActivity implements AttributesListAdapter.AttributeCallback {

    MyGridView productGridView;
    ProgressBar priceAndCartProgressBar;
    ProgressBar relatedProgressBar;
    ProgressBar addToCartProgress;
    LinearLayout priceCartLayout;
    UserSession userSession;
    String product_id, product_title, product_image, product_description;
    ArrayList<ProductList> productLists;
    TextView cartCounter;
    Button addToCartBtn;
    Button refreshBtn;
    MyListView attributesListView;
    TextView variationPriceView;
    ProgressBar priceVariationProgressBar;
    EditText hiddenVariationIdView;
    ArrayList<AttributesList> attributesLists;
    ArrayList<SelectedOptions> selectedOptions = new ArrayList<>();
    JSONArray variations = new JSONArray();
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        userSession = new UserSession(getApplicationContext());

        context = this;
        productGridView = (MyGridView) findViewById(R.id.productGridView);
        priceAndCartProgressBar = (ProgressBar) findViewById(R.id.priceAndCartProgress);
        relatedProgressBar = (ProgressBar) findViewById(R.id.relatedProgressBar);
        addToCartProgress = (ProgressBar) findViewById(R.id.addToCartProgress);
        priceCartLayout = (LinearLayout) findViewById(R.id.priceCartLayout);
        addToCartBtn = (Button) findViewById(R.id.addToCartBtn);
        refreshBtn = (Button) findViewById(R.id.refreshDetailBtn);
        attributesListView = (MyListView) findViewById(R.id.attributesListView);
        variationPriceView = (TextView) findViewById(R.id.variationPriceView);
        priceVariationProgressBar = (ProgressBar) findViewById(R.id.priceVariationProgressBar);
        hiddenVariationIdView = (EditText) findViewById(R.id.hiddenVariationIdEditView);

        hiddenVariationIdView.setText("0"); //default value
        priceVariationProgressBar.setVisibility(View.GONE);
        addToCartProgress.setVisibility(View.GONE);
        priceCartLayout.setVisibility(View.GONE);
        product_id = getIntent().getStringExtra("product_id");
        product_title  = getIntent().getStringExtra("product_title");
        product_image  = getIntent().getStringExtra("product_image");
        product_description  = getIntent().getStringExtra("product_description");

        ImageView imageView = (ImageView) findViewById(R.id.productImage);
        TextView titleView = (TextView) findViewById(R.id.detailTitleView);
        TextView descriptionView = (TextView) findViewById(R.id.detailDescriptionView);

        new DownloadImageTask(imageView).execute(product_image.replace("localhost", Site.DOMAIN));
        titleView.setText(product_title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            descriptionView.setText(Html.fromHtml(product_description, Html.FROM_HTML_MODE_COMPACT));
        } else {
            descriptionView.setText(Html.fromHtml(product_description));
        }

        productLists = new ArrayList<>();
        attributesLists = new ArrayList<>();

        fetchDetail(userSession.userID, this);

        //to refresh
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                priceAndCartProgressBar.setVisibility(View.VISIBLE);
                new DownloadImageTask(imageView).execute(product_image.replace("localhost", Site.DOMAIN));
                fetchDetail(userSession.userID, ProductActivity.this);
            }
        });


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
                Intent intent = new Intent(ProductActivity.this, MainActivity.class);
                intent.putExtra("selected_tab", "cart");
                startActivity(intent);
            }
            break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void  fetchDetail(String userID, Context context) {
        String url = Site.PRODUCT + product_id;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseDetailData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(context, "Unable to get product.", Toast.LENGTH_LONG).show();
            priceAndCartProgressBar.setVisibility(View.GONE);
            refreshBtn.setVisibility(View.VISIBLE);
        });
        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @SuppressLint("SetTextI18n")
    public void parseDetailData(String json) {
        try {
            JSONObject object = new JSONObject(json);

            TextView priceView = (TextView) findViewById(R.id.detailPriceView);
            TextView quantityView = (TextView) findViewById(R.id.cartProductQuantity);
            Button minusBtn = (Button) findViewById(R.id.minusBtn);
            Button plus = (Button) findViewById(R.id.plusBtn);

            minusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int currentQuantity = Integer.parseInt(quantityView.getText().toString());
                    int newQuantity = currentQuantity - 1;
                    if (newQuantity < 2) {
                        newQuantity = 1;
                        minusBtn.setEnabled(false);
                    }
                        quantityView.setText(String.valueOf(newQuantity));
                }
            });
            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int currentQuantity = Integer.parseInt(quantityView.getText().toString());
                    int newQuantity = currentQuantity + 1;
                    if (newQuantity > 1) {
                        minusBtn.setEnabled(true);
                    }
                    //update quantityView
                    quantityView.setText(String.valueOf(newQuantity));
                }
            });

//            double doublePrice = Double.parseDouble(object.getString("price"));
            DecimalFormat formatter = new DecimalFormat("#,###.00");

//            FOR VARIABLE PRODUCT
            if (object.getString("type").equals("variable") || object.getString("product_type").equals("variable")) {
                priceView.setText("From $" + object.getString("price"));
//                variations
                variations = object.getJSONArray("variations");
                JSONArray attributes = object.getJSONArray("attributes");

                for (int i = 0; i < attributes.length(); i++) {
                    JSONObject attribute = attributes.getJSONObject(i);
                    AttributesList list = new AttributesList();
                    list.setId(attribute.getString("id"));
                    list.setName(attribute.getString("name"));
                    list.setOptions(attribute.getString("options"));
                    list.setPosition(attribute.getString("position"));
                    list.setVisible(attribute.getString("visible"));
                    list.setVariation(attribute.getString("variation"));
                    list.setIs_variation(attribute.getString("is_variation"));
                    list.setIs_taxonomy(attribute.getString("is_taxonomy"));
                    list.setValue(attribute.getString("value"));
                    list.setLabel(attribute.getString("label"));
                    attributesLists.add(list);
                }
                AttributesListAdapter attributesListAdapter = new AttributesListAdapter(ProductActivity.this, attributesLists, addToCartBtn, priceVariationProgressBar, variationPriceView);
                attributesListAdapter.setCallback(this);
                attributesListView.setAdapter(attributesListAdapter);
                attributesListView.setVisibility(View.VISIBLE);
                addToCartBtn.setEnabled(false);

                addToCartBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addToCartProgress.setVisibility(View.VISIBLE);
                        try {
                            String quantity = quantityView.getText().toString();
                            new AddToCartWithUpdateCartCount(ProductActivity.this,
                                    hiddenVariationIdView.getText().toString(), quantity, false, addToCartProgress, cartCounter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Toast toast = Toast.makeText(ProductActivity.this, "Added to Cart", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.TOP|Gravity.CENTER, 0,0);
//                        toast.show();
                    }
                });



            } else { // FOR SIMPLE PRODUCT
                priceView.setText("$" + object.getString("price"));
                attributesListView.setVisibility(View.GONE);
                addToCartBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addToCartProgress.setVisibility(View.VISIBLE);
                        try {
                            String quantity = quantityView.getText().toString();
                            new AddToCartWithUpdateCartCount(ProductActivity.this,
                                    object.getString("ID"), quantity, false, addToCartProgress, cartCounter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Toast toast = Toast.makeText(ProductActivity.this, "Added to Cart", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.TOP|Gravity.CENTER, 0,0);
//                        toast.show();
                    }
                });
            }

            parseRelatedData(object.getJSONArray("related_products"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        priceAndCartProgressBar.setVisibility(View.GONE);
        priceCartLayout.setVisibility(View.VISIBLE);
        refreshBtn.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    public void parseRelatedData(JSONArray array) {
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String id = jsonObject.getString("ID");
                String name = jsonObject.getString("name");
                String image = jsonObject.getString("image");
                String price = jsonObject.getString("price");
                String regular_price = jsonObject.getString("regular_price");
                String product_type = jsonObject.getString("product_type");
                String type = jsonObject.getString("type");
                String description = jsonObject.getString("description");
//
                ProductList list = new ProductList();
                list.setId(id);
                list.setName(name);
                list.setImage(image);
                list.setPrice(price);
                list.setType(type);
                list.setRegular_price(regular_price);
                list.setProduct_type(product_type);
                list.setDescription(description);
                productLists.add(list);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        //when fetching is ready
        ProductGridAdapter productAdapter = new ProductGridAdapter(context, productLists, cartCounter);
        productGridView.setAdapter(productAdapter);
        relatedProgressBar.setVisibility(View.GONE);



    }

    @SuppressLint("SetTextI18n")
    public void handleAddToCartOfVariation() {
        StringBuilder params = new StringBuilder();
        for (SelectedOptions option : selectedOptions) {
            params.append(option.getName()).append("=").append(option.getValue()).append("&");
        }
//        Log.e("params: ", params.toString());
    new GetVariationPriceView(this, product_id, params.toString(), variationPriceView, priceVariationProgressBar, addToCartBtn, hiddenVariationIdView);
    }
    public boolean optionNameExists(String name) {
        boolean optionExists = false;
        for (SelectedOptions option : selectedOptions) {
            if (option.getName().equals(name)) {
                optionExists = true;
                break;
            }
        }
        return  optionExists;
    }
    public int getOptionNamePosition(String name) {
        int position = -1;
        for (int i = 0; i < selectedOptions.size(); i++) {
            if (selectedOptions.get(i).getName().equals(name)) {
                position = i;
                 break;  // uncomment to get the first instance
            }
        }
        return position;
    }
    public void addToOptions(String name, String value) {

        if (optionNameExists(name)) {
            //update key name
            selectedOptions.get(getOptionNamePosition(name)).setValue(value);
        } else {
            //add new key name
            selectedOptions.add(new SelectedOptions(name, value));
        }
        handleAddToCartOfVariation();
    }

    private static class SelectedOptions {
        String name;
        String value;

        public SelectedOptions(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @SuppressLint("StaticFieldLeak")
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}