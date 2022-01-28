package com.myshirt.eg.handler;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.myshirt.eg.Site;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateCartCount {
//    Handler myHandler = new Handler();
//    Context context;
    TextView cartTextView;
//    Activity activity;
    public UpdateCartCount(Context context, String userID, TextView cartTextView) {
//        this.context = context;
        this.cartTextView = cartTextView;
//        this.activity = (Activity) context;

        String url = Site.CART + userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONData(response);
            }
        }, (VolleyError error) -> {
              //handle error
        });
        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    public void parseJSONData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (Integer.parseInt(object.getString("contents_count")) > 0) {
                cartTextView.setVisibility(View.VISIBLE);
                cartTextView.setText(object.getString("contents_count"));
            } else {
                cartTextView.setVisibility(View.INVISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
