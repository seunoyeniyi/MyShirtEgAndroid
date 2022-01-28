package com.myshirt.eg.ui.cart;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myshirt.eg.MainActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.myshirt.eg.handler.PriceFormatter;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.adapter.CartAdapter;
import com.myshirt.eg.adapter.CartList;
import com.myshirt.eg.handler.UpdateCartCount;
import com.myshirt.eg.handler.UserSession;
import com.myshirt.eg.ui.AddressActivity;
import com.myshirt.eg.ui.LoginActivity;
import com.myshirt.eg.ui.MyListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CartFragment extends Fragment {

    Dialog loadingDialog;
    MyListView cartListView;
    ArrayList<CartList> cartList;
    View root;
    CartAdapter cartAdapter;
    ShimmerFrameLayout progressBar;
    TextView subtotalView;
    TextView totalView;
    TextView discountView;
    ProgressBar totalProgressBar;
    LinearLayout totalLayout, discountLayout;
    Button refreshCartBtn;
    TextView cartCounter;
    UserSession userSession;
    Button checkOutBtn;
    TextView pointsTextView;
    LinearLayout applyRewardLayout;
    TextView applyRewardText;
    Button applyRewardBtn;

    LinearLayout rewardDiscountPriceLayout;
    TextView rewardDiscountPriceView;
    TextView freeShippingAlert;

    TextView cartEmptyText;

    EditText couponCode;
    Button applyCouponBtn;

    RequestQueue requestQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        loadingDialog = new Dialog(getActivity(), android.R.style.Theme_Light);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
//        loadingDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.cartMenuIcon);
        item.setActionView(R.layout.cart_icon_update_layout);
        RelativeLayout cartCount = (RelativeLayout)   item.getActionView();
        cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
        cartCounter.setText("0");
        cartCounter.setVisibility(View.GONE);
        ImageView iconImage =  (ImageView) cartCount.findViewById(R.id.cartIconMenu);
//        iconImage.setColorFilter(this.getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
        cartAdapter = new CartAdapter(getActivity(), cartList, cartCounter);
        cartListView.setAdapter(cartAdapter);
        userSession = new UserSession(requireContext().getApplicationContext());
        new UpdateCartCount(getActivity(), userSession.userID, cartCounter);

        MenuItem wishItem = menu.findItem(R.id.nav_menu_wishlist);
        wishItem.setActionView(R.layout.wislist_icon_update_layout);
        RelativeLayout wishLayout = (RelativeLayout) wishItem.getActionView();
        TextView wishNotification = (TextView) wishLayout.findViewById(R.id.actionbar_notifcation_textview);
        if (userSession.has_wishlist()) {
            wishNotification.setVisibility(View.VISIBLE);
        } else {
            wishNotification.setVisibility(View.GONE);
        }
        wishLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(wishItem);
            }
        });

        //hide search menu
        MenuItem search = menu.findItem(R.id.nav_menu_search);
        if (search!=null)
            search.setVisible(false);


        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_menu_wishlist: {
                ((MainActivity)getActivity()).changeFragment(R.id.wishlist_fragment);
            }
            break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_cart, container, false);

        ((MainActivity) getActivity()).appBarType("title", "Cart");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        userSession = new UserSession(requireContext().getApplicationContext());

        requestQueue = Volley.newRequestQueue(requireContext());

        cartListView = (MyListView) root.findViewById(R.id.cartListView);
        progressBar = (ShimmerFrameLayout) root.findViewById((R.id.shimmer_view_container));
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startShimmer();
        totalProgressBar = (ProgressBar) root.findViewById((R.id.totalProgressBar));
        totalLayout = (LinearLayout) root.findViewById(R.id.totalLayout);
        discountLayout = (LinearLayout) root.findViewById(R.id.discountLayout);
        subtotalView = (TextView) root.findViewById(R.id.subtotalView);
        totalView = (TextView) root.findViewById(R.id.totalView) ;
        discountView = (TextView) root.findViewById(R.id.discountView) ;
        refreshCartBtn = (Button) root.findViewById(R.id.refreshCartBtn);
        checkOutBtn = (Button) root.findViewById(R.id.checkoutBtn);
        pointsTextView = (TextView) root.findViewById(R.id.pointsTextView);
        pointsTextView.setVisibility(View.GONE);

        cartEmptyText = (TextView) root.findViewById(R.id.cartEmptyText);

        rewardDiscountPriceLayout = (LinearLayout) root.findViewById(R.id.rewardPriceLayout);
        rewardDiscountPriceView = (TextView) root.findViewById(R.id.rewardPriceView);
        freeShippingAlert = (TextView) root.findViewById(R.id.free_shipping_alert);
        rewardDiscountPriceLayout.setVisibility(View.GONE);
        freeShippingAlert.setVisibility(View.GONE);

        applyRewardLayout = (LinearLayout) root.findViewById(R.id.applyRewardLayout);
        applyRewardText = (TextView) root.findViewById(R.id.rewardApplyText);
        applyRewardBtn = (Button) root.findViewById(R.id.applyDiscountBtn);
        applyRewardBtn.setVisibility(View.GONE);
        applyRewardLayout.setVisibility(View.GONE);

        applyCouponBtn = (Button) root.findViewById(R.id.applyCouponBtn);
        couponCode = (EditText) root.findViewById(R.id.couponCode);

        checkOutBtn.setOnClickListener(checkOutBtnClicked);
        cartList = new ArrayList<>();

        totalProgressBar.setVisibility(View.VISIBLE);
        totalLayout.setVisibility(View.GONE);
        discountLayout.setVisibility(View.GONE);

        fetchData(userSession.userID, getActivity());

//        applyRewardBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                applyRewardDiscount();
//            }
//        });

        //to refresh
        refreshCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                fetchData(userSession.userID, getActivity());
            }
        });

        applyCouponBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyCouponCode();
            }
        });

        Button continueShoppingBtn = (Button) root.findViewById(R.id.continueShoppingBtn);
        continueShoppingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).changeFragment(R.id.shop_fragment);
            }
        });

        return root;
    }
    public void  fetchData(String userID, Context context) {
        progressBar.startShimmer();
//        loadingDialog.show();
//        pointsTextView.setVisibility(View.GONE);
        String url = Site.CART + userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            loadingDialog.hide();
            Toast.makeText(context, "Unable to get Cart", Toast.LENGTH_LONG).show();
            progressBar.stopShimmer();
            progressBar.setVisibility(View.GONE);
            totalProgressBar.setVisibility(View.GONE);
            refreshCartBtn.setVisibility(View.VISIBLE);
        });

        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("items");

//            if (array.length() > 0 && object.has("points")) {
//                if (Double.parseDouble(object.getString("points")) > 0) {
//                    pointsTextView.setVisibility(View.VISIBLE);
//                    pointsTextView.setText(Html.fromHtml("Complete your order and earn <b>" + object.getString("points") + "</b> Points for a discount on a future purchase."));
//                }
//            }

            if (array.length() > 0) {
                cartEmptyText.setVisibility(View.GONE);
            } else {
                cartEmptyText.setVisibility(View.VISIBLE);
            }

//            if (array.length() > 0 && object.has("apply_reward") && userSession.logged()) {
//                if (object.getString("apply_reward").equals("false")) {
//                    if (Double.parseDouble(object.getString("reward_discount_points")) > 0) {
//                        applyRewardLayout.setVisibility(View.VISIBLE);
//                        DecimalFormat format = new DecimalFormat("0.#");
//                        Double discount = Double.parseDouble(object.getString("reward_discount"));
//                        String reward_discount = format.format(discount);
//                        applyRewardText.setText(Html.fromHtml("Use <b>" + object.getString("reward_discount_points") + "</b> Points for a <b>" + Site.CURRENCY + reward_discount + "</b> discount on this order!"));
//                        applyRewardBtn.setVisibility(View.VISIBLE);
//                    }
//                }
//            }




            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String id = jsonObject.getString("ID");
                String quantity = jsonObject.getString("quantity");
                String price = jsonObject.getString("price");

                String product_title = jsonObject.getString("product_title");
                String product_image = jsonObject.getString("product_image");

                CartList list = new CartList();
                list.setId(id);
                list.setQuantity(quantity);
                list.setPrice(price);
                list.setProductTitle(product_title);
                list.setProductImage(product_image);
                cartList.add(list);
            }

            subtotalView.setText(Site.CURRENCY + PriceFormatter.format(object.getString("subtotal")));
            //shipping cost might have been added, and its not needed here. So, we have to calculate the total here
            double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
            double couponDiscountDouble = 0;
            double rewardDiscountDouble = 0;
            if (object.getString("has_coupon").equals("true")) {
                couponDiscountDouble = Double.parseDouble(object.getString("coupon_discount"));
            }
            if (object.getString("apply_reward").equals("true")) {
                rewardDiscountDouble = Double.parseDouble(object.getString("reward_discount"));
            }
            totalView.setText(Site.CURRENCY + PriceFormatter.format(subtotalDouble - couponDiscountDouble - rewardDiscountDouble));


//            if (subtotalDouble >= 430 && subtotalDouble < 500) {
//                freeShippingAlert.setText(Html.fromHtml("Add Products worth Rs <b>" + String.valueOf(500 - subtotalDouble) + "</b> or more, to get FREE Delivery."));
//                freeShippingAlert.setVisibility(View.VISIBLE);
//            } else if (subtotalDouble >= 500) {
//                freeShippingAlert.setText("You have got FREE Delivery.");
//                freeShippingAlert.setVisibility(View.VISIBLE);
//            } else {
//                freeShippingAlert.setVisibility(View.GONE);
//            }


            if (object.getString("has_coupon").equals("true")) {
                discountLayout.setVisibility(View.VISIBLE);
                discountView.setText("- " + Site.CURRENCY + PriceFormatter.format(object.getString("coupon_discount")));
            }
//            if (object.getString("apply_reward").equals("true")) {
//                rewardDiscountPriceLayout.setVisibility(View.VISIBLE);
//                rewardDiscountPriceView.setText("- " + Site.CURRENCY + PriceFormatter.format(object.getString("reward_discount")));
//            }

            if (Double.parseDouble(object.getString("subtotal")) > 0) {
                checkOutBtn.setEnabled(true);
            }



            totalProgressBar.setVisibility(View.GONE);
            totalLayout.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //when fetching is ready
// --Commented out by Inspection START (6/28/2021 5:38 PM):
        loadingDialog.hide();
        cartAdapter = new CartAdapter(getActivity(), cartList, cartCounter);
        cartListView.setAdapter(cartAdapter);
//        updateTotals(getActivity());
        cartAdapter.notifyDataSetChanged();
        progressBar.stopShimmer();
        progressBar.setVisibility(View.GONE);
        totalProgressBar.setVisibility(View.GONE);
        refreshCartBtn.setVisibility(View.GONE);
//
//
    }

    public void applyCouponCode() {
        loadingDialog.show();
        String coupon = (isEmpty(couponCode)) ? "null" : couponCode.getText().toString();
        String url = Site.UPDATE_COUPON + userSession.userID + "/" + coupon;
        JSONObject data = new JSONObject();
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
                        try {
                            JSONObject object = new JSONObject(response.toString());

                            double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
                            double couponDiscountDouble = 0;
                            double rewardDiscountDouble = 0;

                            if (object.getString("has_coupon").equals("true")) {

                                couponDiscountDouble = Double.parseDouble(object.getString("coupon_discount"));
                                //shipping cost might have been added (so, total will be calculated here)
                                subtotalView.setText(Site.CURRENCY + PriceFormatter.format(object.getString("subtotal")));
                                discountLayout.setVisibility(View.VISIBLE);
                                discountView.setText("- " + Site.CURRENCY + PriceFormatter.format(object.getString("coupon_discount")));
                                Toast.makeText(requireContext(), "Coupon Applied!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(requireContext(), "Invalid Coupon!", Toast.LENGTH_LONG).show();
                                discountLayout.setVisibility(View.GONE);
//                                (root.findViewById(R.id.innerTotalLayout)).setVisibility(View.GONE); //because discount has been reset
                            }
//                            if (object.getString("apply_reward").equals("true")) {
//                                rewardDiscountDouble = Double.parseDouble(object.getString("reward_discount"));
//                                rewardDiscountPriceLayout.setVisibility(View.VISIBLE);
//                                rewardDiscountPriceView.setText("- " + Site.CURRENCY + PriceFormatter.format(object.getString("reward_discount")));
//                                applyRewardLayout.setVisibility(View.GONE);
//                            }

                            //calculate
                            (root.findViewById(R.id.innerTotalLayout)).setVisibility(View.VISIBLE);
                            totalView.setText(Site.CURRENCY + PriceFormatter.format(subtotalDouble - couponDiscountDouble- rewardDiscountDouble));
//                                paymentAmount = object.getString("total"); //change the global payment amount
                            loadingDialog.hide();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(requireContext(), "Unable to apply coupon.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();
                loadingDialog.hide();
            }
        });

        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(postRequest);
    }
    public boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }
//    public void applyRewardDiscount() {
//        loadingDialog.show();
//        String url = Site.APPLY_REWARD + userSession.userID;
//        JSONObject data = new JSONObject();
//        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
//                new Response.Listener<JSONObject>() {
//                    @SuppressLint("SetTextI18n")
//                    @Override
//                    public void onResponse(JSONObject response) { //response.toString() to get json as string
//                        try {
//                            JSONObject object = new JSONObject(response.toString());
//
//                            double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
//                            double couponDiscountDouble = 0;
//                            double rewardDiscountDouble = 0;
//                            if (object.getString("has_coupon").equals("true")) {
//                                couponDiscountDouble = Double.parseDouble(object.getString("coupon_discount"));
//                                //shipping cost might have been added (so, total will be calculated here
//                                discountLayout.setVisibility(View.VISIBLE);
//                                discountView.setText("- " + Site.CURRENCY + PriceFormatter.format(object.getString("coupon_discount")));
//                            }
//
//                            if (object.getString("apply_reward").equals("true")) {
//                                rewardDiscountDouble = Double.parseDouble(object.getString("reward_discount"));
//                                rewardDiscountPriceLayout.setVisibility(View.VISIBLE);
//                                rewardDiscountPriceView.setText("- " + Site.CURRENCY + PriceFormatter.format(object.getString("reward_discount")));
//                                Toast.makeText(requireContext(), "Discount applied!", Toast.LENGTH_LONG).show();
//                                applyRewardLayout.setVisibility(View.GONE);
//                            } else {
//                                Toast.makeText(requireContext(), "Cannot apply discount!", Toast.LENGTH_LONG).show();
//                            }
//
//                            //total calculation must be done here - because shipping might have been added
//                            subtotalView.setText(Site.CURRENCY + PriceFormatter.format(object.getString("subtotal")));
//                            (root.findViewById(R.id.innerTotalLayout)).setVisibility(View.VISIBLE);
//                            totalView.setText(Site.CURRENCY + PriceFormatter.format(subtotalDouble - couponDiscountDouble- rewardDiscountDouble));
//                            loadingDialog.hide();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast toast = Toast.makeText(requireContext(), "Unable to apply discount.", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
//                toast.show();
//                loadingDialog.hide();
//            }
//        });
//
//        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        requestQueue.add(postRequest);
//    }

    @Override
    public void onDestroy() {
        requestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
        super.onDestroy();
    }

    public View.OnClickListener checkOutBtnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (userSession.logged()) {
                //to next address activity
                startActivity(new Intent(getActivity(), AddressActivity.class));
            } else {
                //to login and from login to address
                startActivity(new Intent(getActivity(), LoginActivity.class).putExtra("activity_from", "address"));
            }
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        requestQueue.cancelAll(requireContext());
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        requestQueue.cancelAll(requireContext());
//    }

}