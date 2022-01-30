package com.myshirt.eg.ui.search;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.myshirt.eg.MainActivity;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.adapter.ProductGridAdapter;
import com.myshirt.eg.adapter.ProductList;
import com.myshirt.eg.handler.UpdateCartCount;
import com.myshirt.eg.handler.UserSession;
import com.myshirt.eg.ui.FilterSheetDialog;
import com.myshirt.eg.ui.MyGridView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {

    private View root;
    TextView cartCounter;
    UserSession userSession;


    FilterSheetDialog filterSheetDialog;
    MyGridView productGridView;
    ArrayList<ProductList> productLists;
    ProductGridAdapter productAdapter;
    ShimmerFrameLayout shopProgressBar;
    Button refreshBtn;
    String searchQuery = "";
    TextView searchStatus;

    AutoCompleteTextView appSearch;
    ProgressBar suggestiveProgress;

    ImageButton filterBtn;

    String product_tag = "";
    String category_name = "";
    String product_color = "";
    String product_size = "";

    boolean applyPriceRange = false;
    List<Float> priceRange = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ((MainActivity) getActivity()).appBarType("welcome");


        if (root != null) {
            return root;
        }


        root = inflater.inflate(R.layout.fragment_search, container, false);


        userSession = new UserSession(requireContext().getApplicationContext());

        searchStatus = (TextView) root.findViewById(R.id.search_status);

        filterSheetDialog = new FilterSheetDialog(getActivity());
        filterBtn = (ImageButton) root.findViewById(R.id.filter_btn);


        appSearch = (AutoCompleteTextView) root.findViewById(R.id.appSearch);
        suggestiveProgress = (ProgressBar) root.findViewById(R.id.suggestive_progress);

        suggestiveProgress.setVisibility(View.GONE);

        //for the  search
        appSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String query = textView.getText().toString();
                if (!query.isEmpty()) {
                    productLists.clear();
                    searchQuery = query;
                    fetchData(requireContext());
                }
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(appSearch.getWindowToken(), 0);
                return true;
            }
        });

        appSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (((AutoCompleteTextView) appSearch).isPerformingCompletion()) {
                    return;
                }
                if (charSequence.length() < 2) {
                    return;
                }

                suggestSearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        appSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                searchQuery = (String) adapterView.getAdapter().getItem(i);
                fetchData(requireContext());

                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(appSearch.getWindowToken(), 0);
            }
        });


        productGridView = (MyGridView) root.findViewById(R.id.productGridView);
        shopProgressBar = (ShimmerFrameLayout) root.findViewById(R.id.shimmer_view_container);
        refreshBtn = (Button) root.findViewById(R.id.refreshBtn);


        productLists = new ArrayList<>();

        //to refresh
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shopProgressBar.setVisibility(View.VISIBLE);
                shopProgressBar.startShimmer();
                fetchData(requireContext());
            }
        });

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!filterSheetDialog.isAdded()) { //to avoid crashing with double touch
                    filterSheetDialog.show(getActivity().getSupportFragmentManager(), "Filter");
                }
            }
        });

        filterSheetDialog.setOnCallback(new FilterSheetDialog.FilterSheetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onApplyClicked(String category, String tag, String color, boolean applyPriceRangeX, List<Float> range, String size) {
                productLists.clear();
                product_tag = tag;

                category_name = category;

                product_color = color;
                product_size = size;

                applyPriceRange = applyPriceRangeX;
                priceRange = range;

//                currentPaged = 1;
//                fetchData(requireContext(), String.valueOf(currentPaged), true);
                fetchData(requireContext());
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.cartMenuIcon);
        item.setActionView(R.layout.cart_icon_update_layout);
        RelativeLayout cartCount = (RelativeLayout)   item.getActionView();
        cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
        cartCounter.setText("0");
        cartCounter.setVisibility(View.GONE);

        ImageView iconImage =  (ImageView) cartCount.findViewById(R.id.cartIconMenu);
//        iconImage.setColorFilter(this.getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);

        userSession = new UserSession(requireActivity().getApplicationContext());
        new UpdateCartCount(getActivity(), userSession.userID, cartCounter);
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
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.cartMenuIcon:{
                ((MainActivity)getActivity()).changeFragment(R.id.navigation_cart);
            }
            break;
            case R.id.nav_menu_wishlist: {
                ((MainActivity)getActivity()).changeFragment(R.id.wishlist_fragment);
            }
            break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void  fetchData(Context context) {
        searchStatus.setVisibility(View.GONE);
        shopProgressBar.setVisibility(View.VISIBLE);
        shopProgressBar.startShimmer();
        productGridView.setVisibility(View.GONE);

        String url = Site.SIMPLE_PRODUCTS + "?search=" + searchQuery + "&per_page=40&hide_description=1&show_variation=1&user_id=" + userSession.userID;

        if (category_name.length() > 0) {
            url += "&cat=" + category_name;
        }

        if (product_tag.length() > 0) {
            url += "&tag=" + product_tag;
        }

        if (product_color.length() > 0) {
            url += "&color=" + product_color;
        }

//        if (product_size.length() > 0) {
//            url += "&size=" + product_size;
//        }

        if (applyPriceRange && priceRange != null) {
            url += "&price_range=" + String.valueOf(priceRange.get(0)) + "|" + String.valueOf(priceRange.get(1));
        }

        @SuppressLint("SetTextI18n") StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Activity activity = getActivity();
                if(activity == null || !isAdded()){
                    return; //to avoid crash
                }

                parseJSONData(response);
            }
        }, (VolleyError error) -> {
            Activity activity = getActivity();
            if(activity == null || !isAdded()){
                return; //to avoid crash
            }
            //handle error
            Toast.makeText(context, "Unable to get products.", Toast.LENGTH_LONG).show();
            shopProgressBar.stopShimmer();
            shopProgressBar.setVisibility(View.GONE);
            productGridView.setVisibility(View.GONE);
            searchStatus.setVisibility(View.VISIBLE);
            searchStatus.setText("Try Again!");
            refreshBtn.setVisibility(View.VISIBLE);
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 3 * 60 * 1000; // eg 3 minutes cache will be hit, but also refreshed on background
                    final long cacheExpired = 24 * 60 * 60 * 1000; // eg 24 hours this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(jsonString, cacheEntry);
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            protected void deliverResponse(String response) {
                super.deliverResponse(response);
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }
        };
        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("results");

            if (array.length() < 1) {
                searchStatus.setVisibility(View.VISIBLE);
                searchStatus.setText("Nothing found!");
            } else {
                searchStatus.setVisibility(View.GONE);
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
        productAdapter = new ProductGridAdapter(requireContext(), productLists, cartCounter);
        productGridView.setAdapter(productAdapter);
        productGridView.setVisibility(View.VISIBLE);
        shopProgressBar.stopShimmer();
        shopProgressBar.setVisibility(View.GONE);
        refreshBtn.setVisibility(View.GONE);


    }


    public void suggestSearch(String query) {
        suggestiveProgress.setVisibility(View.VISIBLE);

        String url = Site.SEARCH + "?s=" + query;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Activity activity = getActivity();
                if(activity == null || !isAdded()){
                    return; //to avoid crash
                }
                try {
                    JSONArray products = new JSONArray(response);
                    ArrayList<String> names = new ArrayList<>();

                    for (int i = 0; i < products.length(); i++) {
                        JSONObject product = products.getJSONObject(i);
                        names.add(product.getString("name"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), R.layout.single_search_dropdown, names);

                    appSearch.setAdapter(adapter);

                    appSearch.showDropDown();

                    suggestiveProgress.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, (VolleyError error) -> {
            //error
            Activity activity = getActivity();
            if(activity == null || !isAdded()){
                return; //to avoid crash
            }
            suggestiveProgress.setVisibility(View.GONE);
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 3 * 60 * 1000; // eg 3 minutes cache will be hit, but also refreshed on background
                    final long cacheExpired = 24 * 60 * 60 * 1000; // eg 24 hours this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(jsonString, cacheEntry);
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            protected void deliverResponse(String response) {
                super.deliverResponse(response);
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(requireActivity()).add(request);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(appSearch.getWindowToken(), 0);
    }

    @Override
    public void onStop() {
        super.onStop();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(appSearch.getWindowToken(), 0);
    }

}