package com.myshirt.eg.ui.shop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.myshirt.eg.ui.FilterSheetDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.myshirt.eg.MainActivity;
import com.myshirt.eg.ui.ScrollViewExt;
import com.myshirt.eg.ui.ScrollViewListener;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.adapter.ProductGridAdapter;
import com.myshirt.eg.adapter.ProductList;
import com.myshirt.eg.handler.UpdateCartCount;
import com.myshirt.eg.handler.UserSession;
import com.myshirt.eg.ui.MyGridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShopFragment extends Fragment {
    View root;
    MyGridView productGridView;
    ArrayList<ProductList> productLists;
    ProductGridAdapter productAdapter;
    ShimmerFrameLayout shopProgressShim;
    FilterSheetDialog filterSheetDialog;
    Button refreshBtn;
    TextView cartCounter;
    Spinner sortSpinner;

    String defaultPaged = "1";
    int currentPaged = 1;
    LinearLayout paginationLayout;
    LinearLayout loadMore;
    ScrollViewExt shopScrollView;
    ProgressBar paginationProgress;

    String product_category = "";
    String product_tag = "";
    String product_color = "";

    UserSession userSession;
    String order_by = "title menu_order";

    boolean applyPriceRange = false;
    List<Float> priceRange = null;


    boolean loadingMore = false;

    RequestQueue requestQueue;

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
            ((MainActivity) getActivity()).appBarType("welcome");

            if (root != null) {
                return root;
            }

            root = inflater.inflate(R.layout.fragment_shop, container, false);

            userSession = new UserSession(getActivity().getApplicationContext());




            Bundle bundle = getArguments();
            if (bundle != null) {
                order_by = (bundle.containsKey("order_by")) ?  bundle.getString("order_by") : order_by;
            }


            productLists = new ArrayList<>();

            requestQueue = Volley.newRequestQueue(requireContext());

            filterSheetDialog = new FilterSheetDialog(getActivity());
            filterSheetDialog.setOnCallback(new FilterSheetDialog.FilterSheetListener() {
                @Override
                public void onApplyClicked(String category, String tag, String color, boolean applyPriceRangeX, List<Float> range) {
                    productLists.clear();
                    applyPriceRange = applyPriceRangeX;
                    priceRange = range;

                    product_category = category;
                    product_tag = tag;

                    product_color = color;

                    currentPaged = 1;
                    fetchData(getContext(), order_by, String.valueOf(currentPaged), true);
                }
            });

        ImageButton searchBtn = (ImageButton) root.findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) requireActivity()).changeFragment(R.id.navigation_search);
            }
        });


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
                        fetchData(getContext(), order_by, String.valueOf(currentPaged + 1), false);
                    }
                }
            });

            productGridView = (MyGridView) root.findViewById(R.id.productGridView);
            shopProgressShim = (ShimmerFrameLayout) root.findViewById(R.id.shimmer_view_container);
            shopProgressShim.startShimmer();
            shopProgressShim.setVisibility(View.VISIBLE);
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
            ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_item_shop, sortOptions);
            sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
            sortSpinner.setAdapter(sortAdapter);

            ImageButton filterBtn = (ImageButton) root.findViewById(R.id.filter_btn);
            filterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!filterSheetDialog.isAdded()) { //to avoid crashing with double touch
                        filterSheetDialog.show(getActivity().getSupportFragmentManager(), "Filter");
                    }
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
                            fetchData(getContext(), order_by, String.valueOf(currentPaged + 1), false);
                        }
                    }
                }
            });






            //to refresh
            refreshBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fetchData(getContext(), order_by, String.valueOf(currentPaged), true);
                }
            });

            switch (order_by) {
                case "title menu_order":
                    sortSpinner.setSelection(0);
                    break;
                case "popularity":
                    sortSpinner.setSelection(1);
                    break;
                case "rating":
                    sortSpinner.setSelection(2);
                    break;
                case "date":
                    sortSpinner.setSelection(3);
                    break;
                case "price":
                    sortSpinner.setSelection(4);
                    break;
                case "price-desc":
                    sortSpinner.setSelection(5);
                    break;
                default:
                    break;
            }

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
                        case 0:
                        default:
                            order_by = "title menu_order";
                            break;
                    }
                    currentPaged = 1;
                    fetchData(getContext(), order_by, String.valueOf(currentPaged), true);
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
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
            ImageView iconImage =  (ImageView) cartCount.findViewById(R.id.cartIconMenu);
//            iconImage.setColorFilter(this.getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
                if (cartCounter == null || productAdapter == null) {
                    productAdapter = new ProductGridAdapter(getActivity(), productLists, cartCounter);
                    productGridView.setAdapter(productAdapter);
                }

                userSession = new UserSession(getActivity().getApplicationContext());

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

        //hide search
        MenuItem search = menu.findItem(R.id.nav_menu_search);
        if (search!=null)
            search.setVisible(false);


        super.onCreateOptionsMenu(menu, inflater);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
            if (id == R.id.cartMenuIcon) {
                ((MainActivity) getActivity()).changeFragment(R.id.navigation_cart);
            } else if (id == R.id.nav_menu_wishlist) {
                ((MainActivity)getActivity()).changeFragment(R.id.wishlist_fragment);
            }
                return super.onOptionsItemSelected(item);
        }



        public void  fetchData(Context context, String order_by, String paged, boolean showProgressBar) {
            loadingMore = true;
            if (showProgressBar) {
                shopProgressShim.setVisibility(View.VISIBLE);
                shopProgressShim.startShimmer();
//                loadingDialog.show();
            }
//                productLists.clear();
//                Log.e("ORDERBY", order_by);
                String url = Site.SIMPLE_PRODUCTS + "?orderby=" + order_by +  "?per_page=40&hide_description=1&show_variation=1" + "&user_id=" + userSession.userID + "&paged=" + paged;
                if (order_by == "title menu_order") {
                    url = Site.SIMPLE_PRODUCTS + "?orderby=" + order_by + "&order=asc&per_page=40&hide_description=1&show_variation=1" + "&user_id=" + userSession.userID + "&paged=" + paged;
                }
            switch (order_by) {
                case "price":
                    url = Site.SIMPLE_PRODUCTS + "?orderby=meta_value_num&meta_key=_price&order=asc&per_page=40&hide_description=1&show_variation=1" + "&user_id=" + userSession.userID + "&paged=" + paged;
                    break;
                case "price-desc":
                    url = Site.SIMPLE_PRODUCTS + "?orderby=meta_value_num&meta_key=_price&order=desc&per_page=40&hide_description=1&show_variation=1" + "&user_id=" + userSession.userID + "&paged=" + paged;
                    break;
                case "date":
                    url = Site.SIMPLE_PRODUCTS + "?orderby=date&order=DESC&per_page=40&hide_description=1&show_variation=1" + "&user_id=" + userSession.userID + "&paged=" + paged;
                    break;
            }

                if (product_category.length() > 0) {
                    url += "&cat=" + product_category;
                }

                if (product_tag.length() > 0) {
                    url += "&tag=" + product_tag;
                }

                if (product_color.length() > 0) {
                    url += "&color=" + product_color;
                }

                if (applyPriceRange && this.priceRange != null) {
                        url = Site.SIMPLE_PRODUCTS + "?price_range=" + String.valueOf(priceRange.get(0)) + "|" + String.valueOf(priceRange.get(1)) + "&per_page=40&hide_description=1&show_variation=1" + "&user_id=" + userSession.userID + "&paged=" + paged;
                }

//            Log.e("PPPP", url);

                StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onResponse(String response) {
                                parseJSONData(response);
                        }
                }, (VolleyError error) -> {
                        //handle error
                        Toast.makeText(context, "Connection error. Please check your connection.", Toast.LENGTH_LONG).show();
//                        loadingDialog.hide();
                        shopProgressShim.stopShimmer();
                        shopProgressShim.setVisibility(View.GONE);
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

                request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                requestQueue.add(request);
        }
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @SuppressLint("SetTextI18n")
        public void parseJSONData(String json) {

            if (json.isEmpty()) {
                paginationProgress.setVisibility(View.GONE);
                paginationLayout.setVisibility(View.GONE);
            }
//            Toast.makeText(requireContext(), userSession.userID.toString(), Toast.LENGTH_SHORT).show();
                try {
                        JSONObject object = new JSONObject(json);
                    if (object.length() == 0) {
                        paginationLayout.setVisibility(View.GONE);
                    }
                        JSONArray array = object.getJSONArray("results");
//                        Log.e("JSON", json);

//                        productLists = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                String id = jsonObject.getString("ID");
                                String name = jsonObject.getString("name");
//                                String title = jsonObject.getString("title");
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
//                                list.setTitle(title);
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
//                productAdapter = new ProductGridAdapter(requireActivity(), productLists, cartCounter);
//                productGridView.setAdapter(productAdapter);
                productAdapter.notifyDataSetChanged();
//                loadingDialog.hide();
                shopProgressShim.stopShimmer();
                shopProgressShim.setVisibility(View.GONE);
                refreshBtn.setVisibility(View.GONE);
                paginationProgress.setVisibility(View.GONE);

                //release for another loading
                loadingMore = false;


        }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        requestQueue.cancelAll(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        requestQueue.cancelAll(requireContext());
    }




}

