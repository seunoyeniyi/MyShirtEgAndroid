package com.fatima.fabric.handler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fatima.fabric.Site;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class AddToCartWithTotalUpdate {
    Context context;
    Activity activity;
    TextView cartCounter;
    ProgressBar totalProgress;
    LinearLayout totalLayout;
    TextView subtotalView;
    TextView totalView;
    UserSession userSession;
    public AddToCartWithTotalUpdate(Context context, String productID,
                                    String quantity,
                                    Boolean replaceQuantity,
                                    TextView cartCounter,
                                    ProgressBar totalProgress,
                                    LinearLayout totalLayout,
                                    TextView subtotalView,
                                    TextView totalView) throws JSONException {
        this.context = context;
        this.activity = (Activity) context;
        this.cartCounter = cartCounter;
        this.totalProgress = totalProgress;
        this.totalLayout = totalLayout;
        this.subtotalView = subtotalView;
        this.totalView = totalView;
        userSession = new UserSession(context.getApplicationContext());

       totalProgress.setVisibility(View.VISIBLE);
       totalLayout.setVisibility(View.GONE);

        String url = Site.ADD_TO_CART + productID;
        JSONObject data = new JSONObject();
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
                Toast toast = Toast.makeText(context, "Unable to update Cart.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(context);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }
    @SuppressLint("SetTextI18n")
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
            } else {
                cartCounter.setVisibility(View.INVISIBLE);
            }
//            double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
            DecimalFormat formatter = new DecimalFormat("#,###.00");
            subtotalView.setText("$" + object.getString("subtotal"));
            totalView.setText("$" + object.getString("subtotal"));
            totalProgress.setVisibility(View.GONE);
            totalLayout.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
