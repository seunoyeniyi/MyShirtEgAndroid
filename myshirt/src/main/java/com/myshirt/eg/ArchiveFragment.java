package com.myshirt.eg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.myshirt.eg.ui.FilterSheetDialog;
import com.myshirt.eg.ui.ScrollViewExt;
import com.myshirt.eg.ui.ScrollViewListener;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.myshirt.eg.adapter.ProductGridAdapter;
import com.myshirt.eg.adapter.ProductList;
import com.myshirt.eg.handler.UpdateCartCount;
import com.myshirt.eg.handler.UserSession;
import com.myshirt.eg.ui.MyGridView;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArchiveFragment extends Fragment {
    View root;
    TextView cartCounter;
    MyGridView productGridView;
    ArrayList<ProductList> productLists;
    ProductGridAdapter productAdapter;
    ShimmerFrameLayout shopProgressBar;
    Button refreshBtn;
    String category_name = "0";
    UserSession userSession;
    String cat_title;
    String cat_description = "";
    String product_tag = "";
    FilterSheetDialog filterSheetDialog;
    LinearLayout paginationLayout;
    LinearLayout loadMore;
    ProgressBar paginationProgress;
    ScrollViewExt shopScrollView;

    TextView categoryTitleView;
    TextView categoryDescriptionView;

    ImageButton searchBtn;
    ImageButton filterBtn;

    String defaultPaged = "1";
    int currentPaged = 1;

    String product_color = "";
    String product_size = "";

    boolean applyPriceRange = false;
    List<Float> priceRange = null;

    boolean loadingMore = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (productLists == null) {
            productLists = new ArrayList<>();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        userSession = new UserSession(requireActivity().getApplicationContext());
        ((MainActivity) getActivity()).appBarType("welcome");

        if (root != null) {
            return root;
        }

        root = inflater.inflate(R.layout.fragment_archive, container, false);



        Bundle bundle = getArguments();
        if (bundle != null) {
            cat_title = (bundle.containsKey("cat_title")) ?  bundle.getString("cat_title").replace("\\", "") : "Category";
            category_name = bundle.getString("category_name");
            cat_description = (bundle.containsKey("category_description")) ?  bundle.getString("category_description").replace("\\", "") : "";
        } else {
            cat_title = "Category";
        }




        categoryTitleView = (TextView) root.findViewById(R.id.category_title);
        categoryDescriptionView = (TextView) root.findViewById(R.id.category_description);

        categoryTitleView.setText(cat_title);
        if (cat_description.length() > 0) {
            categoryDescriptionView.setText(cat_description);
            categoryDescriptionView.setVisibility(View.VISIBLE);
        } else {
            categoryDescriptionView.setVisibility(View.GONE);
        }

        filterSheetDialog = new FilterSheetDialog(getActivity());

        searchBtn = (ImageButton) root.findViewById(R.id.search_btn);
        filterBtn = (ImageButton) root.findViewById(R.id.filter_btn);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) requireActivity()).changeFragment(R.id.navigation_search);
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

        productGridView = (MyGridView) root.findViewById(R.id.productGridView);
        shopProgressBar = (ShimmerFrameLayout) root.findViewById(R.id.shimmer_view_container);
        shopProgressBar.startShimmer();
        shopProgressBar.setVisibility(View.VISIBLE);
        refreshBtn = (Button) root.findViewById(R.id.refreshBtn);

        shopScrollView = (ScrollViewExt) root.findViewById(R.id.shopScrollView);
        paginationLayout = (LinearLayout) root.findViewById(R.id.paginatinoLayout);
        paginationProgress = (ProgressBar) root.findViewById(R.id.paginationProgress);
        paginationLayout.setVisibility(View.GONE);
        paginationProgress.setVisibility(View.GONE);
        loadMore = (LinearLayout) root.findViewById(R.id.loadmore_layout);


        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!loadingMore) {
                    paginationProgress.setVisibility(View.VISIBLE);
                    fetchData(getContext(), String.valueOf(currentPaged + 1), false);
                }
            }
        });



        productLists = new ArrayList<>();
        fetchData(getContext(), defaultPaged, true);

        //to refresh
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shopProgressBar.setVisibility(View.VISIBLE);
                shopProgressBar.startShimmer();
                fetchData(getContext(), String.valueOf(currentPaged), true);
            }
        });

        shopScrollView.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
                // We take the last son eg the scrollview
                View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
                int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

                // if diff is zero, then the bottom has been reached
                //I use 1600... to load more before reaching end
                if (diff < 1600) {
                    if (!loadingMore) {
                        paginationProgress.setVisibility(View.VISIBLE);
                        fetchData(getContext(), String.valueOf(currentPaged + 1), false);
                    }
                }
            }
        });

        filterSheetDialog.setOnCallback(new FilterSheetDialog.FilterSheetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onApplyClicked(String category, String tag, String color, boolean applyPriceRangeX, List<Float> range, String size) {
                productLists.clear();
                product_tag = tag;
                if (category.length() > 0) { //for archive page only
                    category_name = category;
                    String cat_title = category_name.replaceAll("-", " ");
                    categoryTitleView.setText(cat_title.substring(0, 1).toUpperCase() + cat_title.substring(1));
                    categoryDescriptionView.setVisibility(View.GONE);
                }

                applyPriceRange = applyPriceRangeX;
                priceRange = range;

                product_color = color;
                product_size = size;

                currentPaged = 1;
                fetchData(requireContext(), String.valueOf(currentPaged), true);
            }
        });


        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.cartMenuIcon);
        item.setActionView(R.layout.cart_icon_update_layout);
        RelativeLayout cartCount = (RelativeLayout)   item.getActionView();
        cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
        cartCounter.setText("0");
        cartCounter.setVisibility(View.GONE);

        if (cartCounter == null || productAdapter == null) {
            productLists = new ArrayList<>();
            productAdapter = new ProductGridAdapter(requireContext(), productLists, cartCounter);
            productGridView.setAdapter(productAdapter);
        }

        new UpdateCartCount(getContext(), userSession.userID, cartCounter);
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
            case R.id.nav_menu_wishlist: {
                ((MainActivity)getActivity()).changeFragment(R.id.wishlist_fragment);
            }
            break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void  fetchData(Context context, String paged, boolean showProgressBar) {
        loadingMore = true;
        if (showProgressBar) {
            shopProgressBar.setVisibility(View.VISIBLE);
            shopProgressBar.startShimmer();
//            loadingDialog.show();
        }
        String url = Site.SIMPLE_PRODUCTS + "?orderby=menu_order&cat=" + category_name + "&show_variation=1&per_page=20" + "&paged=" + paged + "&user_id=" + userSession.userID + "&hide_description=1";

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

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                parseJSONData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(context, "Unable to get products.", Toast.LENGTH_LONG).show();
            shopProgressBar.stopShimmer();
            shopProgressBar.setVisibility(View.GONE);
//            loadingDialog.hide();
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
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json) {
        if (json.isEmpty()) {
            paginationProgress.setVisibility(View.GONE);
            paginationLayout.setVisibility(View.GONE);
        }

        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("results");

            if (object.length() == 0) {
                paginationLayout.setVisibility(View.GONE);
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

            if (object.get("pagination") != null) {
                currentPaged = Integer.parseInt(object.getString("paged"));
                paginationLayout.setVisibility(View.VISIBLE);
            } else {
                paginationLayout.setVisibility(View.GONE);
            }
            if (object.has("pagination")) {
                if (object.getString("pagination").equals("null"))
                    paginationLayout.setVisibility(View.GONE);
            } else {
                paginationLayout.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            if (Objects.equals(e.getLocalizedMessage(), "End of input at character 0 of")) {
                paginationProgress.setVisibility(View.GONE);
            }
            e.printStackTrace();
        }
        //when fetching is ready
        productAdapter.notifyDataSetChanged();
//        loadingDialog.hide();
        shopProgressBar.stopShimmer();
        shopProgressBar.setVisibility(View.GONE);
        refreshBtn.setVisibility(View.GONE);
        paginationProgress.setVisibility(View.GONE);

        //release for another loading
        loadingMore = false;


    }
}