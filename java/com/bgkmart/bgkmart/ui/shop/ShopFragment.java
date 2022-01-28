package com.fatima.fabric.ui.shop;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.fatima.fabric.ArchiveActivity;
import com.fatima.fabric.R;
import com.fatima.fabric.Site;
import com.fatima.fabric.adapter.ProductGridAdapter;
import com.fatima.fabric.adapter.ProductList;
import com.fatima.fabric.handler.UpdateCartCount;
import com.fatima.fabric.handler.UserSession;
import com.fatima.fabric.ui.MyGridView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.RangeSlider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ShopFragment extends Fragment {
    MyGridView productGridView;
    View root;
    ArrayList<ProductList> productLists;
    ProductGridAdapter productAdapter;
    ProgressBar shopProgressBar;
    BottomSheetDialog filterSheet;
    RangeSlider priceRange;
    Button refreshBtn;
    TextView cartCounter;
    Dialog loadingDialog;
    Spinner sortSpinner;
    UserSession userSession;
    String order_by = "menu_order";

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                MenuItem item = menu.findItem(R.id.cartMenuIcon);
                item.setActionView(R.layout.cart_icon_update_layout);
                RelativeLayout cartCount = (RelativeLayout)   item.getActionView();
                cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
                cartCounter.setText("0");
                cartCounter.setVisibility(View.GONE);
                productAdapter = new ProductGridAdapter(getActivity(), productLists, cartCounter);
                productGridView.setAdapter(productAdapter);
                userSession = new UserSession(getActivity().getApplicationContext());

                new UpdateCartCount(getActivity(), userSession.userID, cartCounter);
                cartCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onOptionsItemSelected(item);
                }
            });
                super.onCreateOptionsMenu(menu, inflater);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
            if (id == R.id.cartMenuIcon) {
                BottomNavigationView bottomNav = (BottomNavigationView) requireActivity().findViewById(R.id.nav_view);
                bottomNav.setSelectedItemId(R.id.cartBottomBtn);
            }
                return super.onOptionsItemSelected(item);
        }

        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_shop, container, false);
        userSession = new UserSession(getActivity().getApplicationContext());

        loadingDialog = new Dialog(getActivity(), android.R.style.Theme_Light);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        filterSheet = new BottomSheetDialog(getActivity());
        filterSheet.setContentView(R.layout.filter_bottom_sheet_dialog);
        priceRange = (RangeSlider) filterSheet.findViewById(R.id.priceRange);


        productGridView = (MyGridView) root.findViewById(R.id.productGridView);
        shopProgressBar = (ProgressBar) root.findViewById(R.id.shopProgessBar);
        refreshBtn = (Button) root.findViewById(R.id.refreshBtn);
        refreshBtn.setVisibility(View.GONE);
        productLists = new ArrayList<>();

            sortSpinner = (Spinner) root.findViewById(R.id.sortSpinner);
            List<String> sortOptions = new ArrayList<String>();
            sortOptions.add("Default sorting"); //0 - menu_order
            sortOptions.add("Sort by popularity"); //1 - popularity
            sortOptions.add("Sort by average rating"); //2 - rating
            sortOptions.add("Sort by latest"); //3 - date
            sortOptions.add("Sort by price: low to high"); //4 - price
            sortOptions.add("Sort by price: high to low"); //5 - price-desc
            ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_item, sortOptions);
            sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
            sortSpinner.setAdapter(sortAdapter);

            sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    productLists.clear();
                    switch (i) {
                        case 1:
                            order_by = "popularity";
                            break;
                        case 2:
                            order_by = "rating";
                            break;
                        case 3:
                            order_by = "date";
                            break;
                        case 4:
                            order_by = "price";
                            break;
                        case 5:
                            order_by = "price-desc";
                            break;
                        default:
                            order_by = "menu_order";
                            break;
                    }
                    fetchData(getContext(), order_by);
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            Button filterBtn = (Button) root.findViewById(R.id.filterBtn);
            filterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    filterSheet.show();
                }
            });
            Button applyFilter = (Button) filterSheet.findViewById(R.id.applyFilter);
            assert applyFilter != null;
            applyFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    productLists.clear();
                    fetchData(getContext(), "price-range");
                    filterSheet.hide();
                }
            });


//        fetchData(getActivity(), order_by);

        //to refresh
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchData(getActivity(), order_by);
            }
        });
        return root;
        }

        public void  fetchData(Context context, String order_by) {
                loadingDialog.show();
                shopProgressBar.setVisibility(View.VISIBLE);
                productLists.clear();
                Log.e("ORDERBY", order_by);
                String url = Site.PRODUCTS + "?orderby=" + order_by + "&per_page=100";
            switch (order_by) {
                case "price":
                    url = Site.PRODUCTS + "?orderby=meta_value_num&meta_key=_price&order=asc&per_page=100";
                    break;
                case "price-desc":
                    url = Site.PRODUCTS + "?orderby=meta_value_num&meta_key=_price&order=desc&per_page=100";
                    break;
                case "date":
                    url = Site.PRODUCTS + "?orderby=date&order=DESC&per_page=100";
                    break;
                case "price-range":
                    List<Float> range = priceRange.getValues();
                    url = Site.PRODUCTS + "?price_range=" + String.valueOf(range.get(0)) + "|" + String.valueOf(range.get(1)) + "&per_page=100";
                    break;
            }
                StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                                parseJSONData(response);
                        }
                }, (VolleyError error) -> {
                        //handle error
                        Toast.makeText(context, "Connection error. Please check your connection.", Toast.LENGTH_LONG).show();
                        loadingDialog.hide();
                        shopProgressBar.setVisibility(View.GONE);
                        refreshBtn.setVisibility(View.VISIBLE);
                });
                RequestQueue rQueue = Volley.newRequestQueue(context);
                request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                rQueue.add(request);
        }
        @SuppressLint("SetTextI18n")
        public void parseJSONData(String json) {

                try {
                        JSONObject object = new JSONObject(json);
                        JSONArray array = object.getJSONArray("results");
                        Log.e("JSON", json);

                        productLists = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                String id = jsonObject.getString("ID");
                                String name = jsonObject.getString("name");
                                String title = jsonObject.getString("title");
                                String image = jsonObject.getString("image");
                                String price = jsonObject.getString("price");
                                String regular_price = jsonObject.getString("regular_price");
                                String product_type = jsonObject.getString("product_type");
                                String type = jsonObject.getString("type");
                                String description = jsonObject.getString("description");
//
                                ProductList list = new ProductList();
                                list.setId(id);
                                list.setTitle(title);
                                list.setName(name);
                                list.setImage(image);
                                list.setPrice(price);
                                list.setRegular_price(regular_price);
                                list.setType(type);
                                list.setProduct_type(product_type);
                                list.setDescription(description);
                                productLists.add(list);
                        }
//            Toast.makeText(getActivity(), String.valueOf(cartList.size()), Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                        e.printStackTrace();
                }
                //when fetching is ready
                productAdapter = new ProductGridAdapter(requireActivity(), productLists, cartCounter);
                productGridView.setAdapter(productAdapter);
                loadingDialog.hide();
                shopProgressBar.setVisibility(View.GONE);
                refreshBtn.setVisibility(View.GONE);


        }



}

