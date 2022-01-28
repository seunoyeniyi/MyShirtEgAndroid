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

public class ApplyCouponWithTotalUpdate {
    Context context;
    Activity activity;
    ProgressBar totalProgress;
    LinearLayout totalLayout;
    TextView subtotalView;
    TextView totalView;
    UserSession userSession;
    TextView couponPriceView;
    String coupon;
    public ApplyCouponWithTotalUpdate(Context context, String coupon,
                                    ProgressBar totalProgress,
                                    LinearLayout totalLayout,
                                    TextView subtotalView,
                                    TextView couponPriceView,
                                    TextView totalView) throws JSONException {
        this.context = context;
        this.coupon = coupon;
        this.activity = (Activity) context;
        this.totalProgress = totalProgress;
        this.totalLayout = totalLayout;
        this.subtotalView = subtotalView;
        this.totalView = totalView;
        this.couponPriceView = couponPriceView;

        userSession = new UserSession(context.getApplicationContext());

        totalProgress.setVisibility(View.VISIBLE);
        totalLayout.setVisibility(View.GONE);

        String url = Site.UPDATE_COUPON + userSession.userID + "/" + coupon;
        JSONObject data = new JSONObject();
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
                        parseJSONData(response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(context, "Unable to apply coupon.", Toast.LENGTH_LONG);
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

           if (object.has("is_empty") && object.getString("is_empty").equals("false")) {
//               double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
               DecimalFormat formatter = new DecimalFormat("#,###.00");
               subtotalView.setText("$" + object.getString("subtotal"));
               totalView.setText("$" + object.getString("subtotal"));
               totalView.setText("$" + object.getString("subtotal"));
               totalProgress.setVisibility(View.GONE);
               totalLayout.setVisibility(View.VISIBLE);
           }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
