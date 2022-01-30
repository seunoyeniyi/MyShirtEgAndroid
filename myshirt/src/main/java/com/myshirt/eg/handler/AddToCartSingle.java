package com.myshirt.eg.handler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.tapadoo.alerter.Alerter;

import org.json.JSONException;
import org.json.JSONObject;

public class AddToCartSingle {
    Context context;
    TextView cartCounter;
    UserSession userSession;
    Activity activity;
    Alerter alerter;
    public AddToCartSingle(Context context, String productID, String quantity, Boolean replaceQuantity, TextView cartCounter) throws JSONException {
        this.context = context;
        activity = (Activity) context;
        this.cartCounter = cartCounter;
        this.alerter = alerter;
        userSession = new UserSession(context.getApplicationContext());

        alerter = Alerter.create(activity);
        alerter.setIcon(R.drawable.icons8_loading);
        alerter.setText("Adding to cart...");
        alerter.show();

        String url = Site.ADD_TO_CART + productID;
        JSONObject data = new JSONObject();
//        Toast.makeText(context, userSession.userID, Toast.LENGTH_LONG).show();
        if (!userSession.userID.equals("0")) data.put("user", userSession.userID);
        data.put("quantity", quantity);
        if (replaceQuantity) data.put("replace_quantity", "1");

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
                        parseJSONData(response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.e("JSON reply: ", error.toString());
                alerter.setBackgroundColorInt(Color.RED);
                alerter.setIcon(R.drawable.icons8_cancel_2);
                alerter.setText("Unable to update Cart. You may need to Login");
                if (!Alerter.isShowing())
                    alerter.show();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(context);
        postRequest.setShouldCache(false);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }
    public void parseJSONData(String json) {
        try {
            JSONObject object = new JSONObject(json);

            UserSession userSession = new UserSession(context.getApplicationContext());
            if (userSession.userID.equals("0") && object.getString("user_cart_exists").equals("false")) {
//                save the generated user id to session
                userSession.createLoginSession(object.getString("user"),"", "", false);
            } else if (object.has("user_cart_not_exists")) {
                Toast.makeText(context, "Can't create cart! Please login or register.", Toast.LENGTH_LONG).show();
            }


            if (Integer.parseInt(object.getString("contents_count")) > 0) {
                cartCounter.setVisibility(View.VISIBLE);
                cartCounter.setText(object.getString("contents_count"));
                alerter.setBackgroundColorRes(R.color.primary);
                alerter.setIcon(R.drawable.icons8_ok);
                alerter.setText("Product added to cart!");
                if (!Alerter.isShowing())
                    alerter.show();
            } else {
                cartCounter.setVisibility(View.INVISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
