package com.myshirt.eg.handler;

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
import com.myshirt.eg.Site;

import org.json.JSONException;
import org.json.JSONObject;

public class AddToCartWithTotalUpdate {
    Context context;
    TextView cartCounter;
    ProgressBar totalProgress;
    LinearLayout totalLayout;
    TextView subtotalView;
    TextView totalView;
    TextView couponView;
//    TextView pointsTextView;
//    TextView applyRewardText;
    TextView freeShippingAlert;
    Activity activity;

    UserSession userSession;
    public AddToCartWithTotalUpdate(Context context, String productID,
                                    String quantity,
                                    Boolean replaceQuantity,
                                    TextView cartCounter,
                                    ProgressBar totalProgress,
                                    LinearLayout totalLayout,
                                    TextView subtotalView,
                                    TextView totalView, TextView couponView, TextView freeShippingAlert /*TextView pointsTextViewTextView, TextView applyRewardText*/) throws JSONException {
        this.context = context;
        this.cartCounter = cartCounter;
        this.totalProgress = totalProgress;
        this.totalLayout = totalLayout;
        this.subtotalView = subtotalView;
        this.totalView = totalView;
        this.couponView = couponView;
        this.freeShippingAlert = freeShippingAlert;
//        this.pointsTextView = pointsTextViewTextView;
//        this.applyRewardText = applyRewardText;
        userSession = new UserSession(context.getApplicationContext());
        this.activity = (Activity) context;

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
        postRequest.setShouldCache(false);
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

            double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
            double couponDiscountDouble = 0;
            double rewardDiscountDouble = 0;


            if (Integer.parseInt(object.getString("contents_count")) > 0) {
                cartCounter.setVisibility(View.VISIBLE);
                cartCounter.setText(object.getString("contents_count"));

//                if (object.has("points")) {
//                    pointsTextView.setVisibility(View.VISIBLE);
//                    pointsTextView.setText(Html.fromHtml("Complete your order and earn <b>" + object.getString("points") + "</b> Points for a discount on a future purchase."));
//                }
//                if (object.has("apply_reward") && userSession.logged()) {
//                    if (object.getString("apply_reward").equals("true")) { //if it is true only
//                        if (Double.parseDouble(object.getString("reward_discount_points")) > 0) {
//                            DecimalFormat format = new DecimalFormat("0.#");
//                            Double discount = Double.parseDouble(object.getString("reward_discount"));
//                            rewardDiscountDouble = discount;
//                            String reward_discount = format.format(discount);
//                            applyRewardText.setText(Html.fromHtml("Use <b>" + object.getString("reward_discount_points") + "</b> Points for a <b>" + Site.CURRENCY + reward_discount + "</b> discount on this order!"));
//                        }
//                    }
//                }
                if (object.getString("has_coupon").equals("true")) {
                    couponDiscountDouble = Double.parseDouble(object.getString("coupon_discount"));
                    couponView.setText("- " + Site.CURRENCY + PriceFormatter.format(object.getString("coupon_discount")));
                }


//                if (subtotalDouble >= 430 && subtotalDouble < 500) {
//                    freeShippingAlert.setText(Html.fromHtml("Add Products worth Rs <b>" + String.valueOf(500 - subtotalDouble) + "</b> or more, to get FREE Delivery."));
//                    freeShippingAlert.setVisibility(View.VISIBLE);
//                } else if (subtotalDouble >= 500) {
//                    freeShippingAlert.setText("You have got FREE Delivery.");
//                    freeShippingAlert.setVisibility(View.VISIBLE);
//                } else {
//                    freeShippingAlert.setVisibility(View.GONE);
//                }



            } else {
                cartCounter.setVisibility(View.INVISIBLE);
            }


            subtotalView.setText(Site.CURRENCY + PriceFormatter.format(object.getString("subtotal")));
            //calculate total
            totalView.setText(Site.CURRENCY + PriceFormatter.format(subtotalDouble - couponDiscountDouble - rewardDiscountDouble));
            totalProgress.setVisibility(View.GONE);
            totalLayout.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
