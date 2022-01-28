package com.fatima.fabric.ui.cart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.fatima.fabric.R;
import com.fatima.fabric.Site;
import com.fatima.fabric.adapter.CartAdapter;
import com.fatima.fabric.adapter.CartList;
import com.fatima.fabric.handler.UpdateCartCount;
import com.fatima.fabric.handler.UserSession;
import com.fatima.fabric.ui.AddressActivity;
import com.fatima.fabric.ui.LoginActivity;
import com.fatima.fabric.ui.MyListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CartFragment extends Fragment {

    MyListView cartListView;
    ArrayList<CartList> cartList;
    View root;
    CartAdapter cartAdapter;
    ProgressBar progressBar;
    TextView subtotalView;
    TextView totalView;
    ProgressBar totalProgressBar;
    LinearLayout totalLayout;
    Button refreshCartBtn;
    TextView cartCounter;
    UserSession userSession;
    RequestQueue requestQueue;
    Button checkOutBtn;

    public CartFragment() {
        // Required empty public constructor
    }


    public static CartFragment newInstance(String param1, String param2) {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.cartMenuIcon);
        item.setActionView(R.layout.cart_icon_update_layout);
        RelativeLayout cartCount = (RelativeLayout)   item.getActionView();
        cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
        cartCounter.setText("0");
        cartCounter.setVisibility(View.GONE);
        cartAdapter = new CartAdapter(getActivity(), cartList, cartCounter);
        cartListView.setAdapter(cartAdapter);
        userSession = new UserSession(getActivity().getApplicationContext());
        new UpdateCartCount(getActivity(), userSession.userID, cartCounter);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_cart, container, false);
        userSession = new UserSession(getActivity().getApplicationContext());

        cartListView = (MyListView) root.findViewById(R.id.cartListView);
        progressBar = (ProgressBar) root.findViewById((R.id.progressBar));
        totalProgressBar = (ProgressBar) root.findViewById((R.id.totalProgressBar));
        totalLayout = (LinearLayout) root.findViewById(R.id.totalLayout);
        subtotalView = (TextView) root.findViewById(R.id.subtotalView);
        totalView = (TextView) root.findViewById(R.id.totalView) ;
        refreshCartBtn = (Button) root.findViewById(R.id.refreshCartBtn);
        checkOutBtn = (Button) root.findViewById(R.id.checkoutBtn);
        checkOutBtn.setOnClickListener(checkOutBtnClicked);
        cartList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(requireActivity());

        totalProgressBar.setVisibility(View.VISIBLE);
        totalLayout.setVisibility(View.GONE);

        fetchData(userSession.userID, getActivity());

        //to refresh
        refreshCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                fetchData(userSession.userID, getActivity());
            }
        });

        Button continueShoppingBtn = (Button) root.findViewById(R.id.continueShoppingBtn);
        continueShoppingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomNavigationView bottomNav = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
                bottomNav.setSelectedItemId(R.id.shopBottomBtn);
            }
        });

        return root;
    }
    public void  fetchData(String userID, Context context) {
        String url = Site.CART + userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(context, "Unable to get Cart", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            totalProgressBar.setVisibility(View.GONE);
            refreshCartBtn.setVisibility(View.VISIBLE);
        });
        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("items");


            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String id = jsonObject.getString("ID");
                String quantity = jsonObject.getString("quantity");
                String price = jsonObject.getString("price");
                String subtotal = jsonObject.getString("subtotal");
                String product_type = jsonObject.getString("product_type");
                String product_title = jsonObject.getString("product_title");
                String product_image = jsonObject.getString("product_image");
                String attributes = jsonObject.getString("attributes");
//
                CartList list = new CartList();
                list.setId(id);
                list.setQuantity(quantity);
                list.setPrice(price);
                list.setSubtotal(subtotal);
                list.setProductType(product_type);
                list.setProductTitle(product_title);
                list.setProductImage(product_image);
                list.setAttributes(attributes);
                cartList.add(list);
            }
//            Toast.makeText(getActivity(), String.valueOf(cartList.size()), Toast.LENGTH_LONG).show();
//            double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
            DecimalFormat formatter = new DecimalFormat("#,###.00");
            subtotalView.setText("$" + object.getString("subtotal"));
            totalView.setText("$" + object.getString("subtotal"));

            if (Double.parseDouble(object.getString("subtotal")) > 0) {
                checkOutBtn.setEnabled(true);
            }

            totalProgressBar.setVisibility(View.GONE);
            totalLayout.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //when fetching is ready
//        cartAdapter = new CartAdapter(getActivity(), cartList, cartCounter);
//        cartListView.setAdapter(cartAdapter);
//        updateTotals(getActivity());
        cartAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        totalProgressBar.setVisibility(View.GONE);
        refreshCartBtn.setVisibility(View.GONE);


    }

    public void updateTotals(Context context) {
        totalProgressBar.setVisibility(View.VISIBLE);
        totalLayout.setVisibility(View.GONE);
        String url = Site.CART + userSession.userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONDataForTotals(response);
                new UpdateCartCount(getActivity(), userSession.userID, cartCounter);
            }
        }, (VolleyError error) -> {
            //handle error
            totalProgressBar.setVisibility(View.GONE);
        });
//        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request); //using global requestQueue to be able to be destroyed on fragment destroyed
    }
    @SuppressLint("SetTextI18n")
    public void parseJSONDataForTotals(String json) {
        try {
            JSONObject object = new JSONObject(json);

//            double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
            DecimalFormat formatter = new DecimalFormat("#,###.00");
            subtotalView.setText("$" + object.getString("subtotal"));
            totalView.setText("$" + object.getString("subtotal"));

            if (Double.parseDouble(object.getString("subtotal")) > 0) {
                checkOutBtn.setEnabled(true);
            }
            totalProgressBar.setVisibility(View.GONE);
            totalLayout.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
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
}