package com.myshirt.eg.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.myshirt.eg.MainActivity;
import com.myshirt.eg.handler.UpdateCartCount;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.adapter.ProductGridAdapter;
import com.myshirt.eg.adapter.ProductList;
import com.myshirt.eg.handler.UserSession;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WishListFragment extends Fragment {

    View root;
    TextView cartCounter;
    MyGridView productGridView;
    ArrayList<ProductList> productLists;
    ProductGridAdapter productAdapter;
    ShimmerFrameLayout shopProgressBar;
    Button refreshBtn;
    UserSession userSession;
    TextView noQuery;

    RequestQueue requestQueue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        productLists = new ArrayList<>();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ((MainActivity) getActivity()).appBarType("title", "Wishlist");

        if (root != null) { //to avoid reload on back tapped
            return root;
        }

        root = inflater.inflate(R.layout.fragment_wish_list, container, false);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        userSession = new UserSession(requireActivity().getApplicationContext());

        requestQueue = Volley.newRequestQueue(requireActivity());

        noQuery = (TextView) root.findViewById(R.id.noQuery);
        noQuery.setVisibility(View.GONE);
        productGridView = (MyGridView) root.findViewById(R.id.productGridView);
        shopProgressBar = (ShimmerFrameLayout) root.findViewById(R.id.shimmer_view_container);
        shopProgressBar.setVisibility(View.VISIBLE);
        shopProgressBar.startShimmer();
        refreshBtn = (Button) root.findViewById(R.id.refreshBtn);

        productLists = new ArrayList<>();
        fetchData(requireActivity());

        //to refresh
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shopProgressBar.setVisibility(View.VISIBLE);
                fetchData(requireActivity());
            }
        });

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

            MenuItem item = menu.findItem(R.id.cartMenuIcon);
            item.setActionView(R.layout.cart_icon_update_layout);
            RelativeLayout cartCount = (RelativeLayout) item.getActionView();
            cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
            cartCounter.setText("0");
            cartCounter.setVisibility(View.GONE);

        if (cartCounter == null) {
            productLists = new ArrayList<>();
            productAdapter = new ProductGridAdapter(requireActivity(), productLists, cartCounter);
            productGridView.setAdapter(productAdapter);
        }

            new UpdateCartCount(requireActivity(), userSession.userID, cartCounter);
            cartCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onOptionsItemSelected(item);
                }
            });

        MenuItem wishItem = menu.findItem(R.id.nav_menu_wishlist);
        wishItem.setActionView(R.layout.wislist_icon_update_layout);
        RelativeLayout wishLayout = (RelativeLayout) wishItem.getActionView();
        TextView wishNotification = (TextView) wishLayout.findViewById(R.id.actionbar_notifcation_textview);
        if (userSession.has_wishlist()) {
            wishNotification.setVisibility(View.VISIBLE);
        } else {
            wishNotification.setVisibility(View.GONE);
        }


        //hide search
        MenuItem search = menu.findItem(R.id.nav_menu_search);
        if (search!=null)
            search.setVisible(false);


        super.onCreateOptionsMenu(menu, inflater);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        switch (item.getItemId()) {
            case R.id.cartMenuIcon:{
                ((MainActivity) requireActivity()).changeFragment(R.id.navigation_cart);
            }
            break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void  fetchData(Context context) {
        shopProgressBar.setVisibility(View.VISIBLE);
        shopProgressBar.startShimmer();
        String url = Site.WISH_LIST + userSession.userID + "?hide_description=1&show_variation=1";
        noQuery.setVisibility(View.GONE);
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Activity activity = getActivity();
                if(activity == null || !isAdded()){
                    return; //to avoid crash
                }
                parseJSONData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            Activity activity = getActivity();
            if(activity == null || !isAdded()){
                return; //to avoid crash
            }
            Toast.makeText(context, "Unable to get products.", Toast.LENGTH_LONG).show();
            shopProgressBar.stopShimmer();
            shopProgressBar.setVisibility(View.GONE);
            refreshBtn.setVisibility(View.VISIBLE);
            noQuery.setVisibility(View.VISIBLE);
        });
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json) {
        if (json.isEmpty()) {
            userSession.has_wishlist(false);
        }
        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("results");

            if (array.length() < 1) {
                noQuery.setVisibility(View.VISIBLE);
                userSession.has_wishlist(false);
            } else {
                noQuery.setVisibility(View.GONE);
                userSession.has_wishlist(true);
            }

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
                String stock_status = jsonObject.getString("stock_status");
                String lowest_price = jsonObject.getString("lowest_variation_price");
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
                list.setStock_status(stock_status);
                list.setLowest_price(lowest_price);
                list.setAttributes(attributes);
                list.setVariations(variations);
                productLists.add(list);
            }
//            Toast.makeText(getActivity(), String.valueOf(cartList.size()), Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //when fetching is ready
        productAdapter = new ProductGridAdapter(requireActivity(), productLists, cartCounter);
        productGridView.setAdapter(productAdapter);
        shopProgressBar.stopShimmer();
        shopProgressBar.setVisibility(View.GONE);
        refreshBtn.setVisibility(View.GONE);


    }

    @Override
    public void onStop() {
        super.onStop();
        requestQueue.cancelAll(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        requestQueue.cancelAll(requireActivity());
    }

}