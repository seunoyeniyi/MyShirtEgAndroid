package com.fatima.fabric.handler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fatima.fabric.Site;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class GetVariationPriceView {
    Context context;
    TextView priceView;
    String productID;
    String params;
    ProgressBar priceVariationProgressBar;
    EditText hiddenVariationIdView;
    Button addToCartBtn;
    public GetVariationPriceView(Context context, String productID, String params, TextView priceView, ProgressBar priceVariationProgressBar, Button addToCartBtn, EditText hiddenVariationIdView) {
        this.context = context;
        this.priceView = priceView;
        this.productID = productID;
        this.params = params;
        this.priceVariationProgressBar = priceVariationProgressBar;
        this.addToCartBtn = addToCartBtn;
        this.hiddenVariationIdView = hiddenVariationIdView;

        priceVariationProgressBar.setVisibility(View.VISIBLE);
        addToCartBtn.setEnabled(false);
        String url = Site.PRODUCT_VARIATION + productID + "?" + params;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            priceView.setVisibility(View.INVISIBLE);
            addToCartBtn.setEnabled(false);
            hiddenVariationIdView.setText("0");
        });
        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json) {
        try {
            JSONObject object = new JSONObject(json);
                priceView.setVisibility(View.VISIBLE);
            hiddenVariationIdView.setText(object.getString("ID"));
//                double doublePrice = Double.parseDouble(object.getString("price"));
                DecimalFormat formatter = new DecimalFormat("#,###.00");
                priceView.setText("$" + object.getString("price"));
                priceVariationProgressBar.setVisibility(View.GONE);
                addToCartBtn.setEnabled(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
