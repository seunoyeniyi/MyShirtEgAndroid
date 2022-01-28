package com.myshirt.eg.ui.categories;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.myshirt.eg.MainActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.adapter.CategoriesList;
import com.myshirt.eg.adapter.CategoriesRecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class CategoriesFragment extends Fragment {


    View root;
    ShimmerFrameLayout categoriesShimmer;
    Button refreshBtn;
    List<CategoriesList> categoriesLists;
    RecyclerView categoriesRecycler;

    RequestQueue requestQueue;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        if (getArguments() != null) {
////            mParam1 = getArguments().getString(ARG_PARAM1);
////            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_categories, container, false);

        ((MainActivity) getActivity()).appBarType("title", "Categories");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        requestQueue = Volley.newRequestQueue(requireContext());

        categoriesShimmer = (ShimmerFrameLayout) root.findViewById(R.id.categories_shimmer);
        categoriesShimmer.startShimmer();
        categoriesShimmer.setVisibility(View.VISIBLE);
        refreshBtn = (Button) root.findViewById(R.id.refreshBtn);
        refreshBtn.setVisibility(View.GONE);

        categoriesLists = new ArrayList<>();

        categoriesRecycler = (RecyclerView) root.findViewById(R.id.categoryRecyler);
        CategoriesRecyclerAdapter adapter = new CategoriesRecyclerAdapter(getActivity(), categoriesLists);
        categoriesRecycler.setAdapter(adapter);
//        categoriesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoriesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoriesRecycler.setVisibility(View.GONE);

        fetchCategories();

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchCategories();
            }
        });




        return root;
    }



    public void fetchCategories() {
//        catProgress.setVisibility(View.VISIBLE);
        categoriesRecycler.setVisibility(View.GONE);
        categoriesShimmer.setVisibility(View.VISIBLE);
        categoriesShimmer.startShimmer();
        String url = Site.CATEGORIES + "?hide_empty=1&order_by=menu_order";
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Activity activity = getActivity();
                if(activity == null || !isAdded()){
                    return; //to avoid crash
                }
                parseCatJSONData(response);
            }
        }, (VolleyError error) -> {
            Activity activity = getActivity();
            if(activity == null || !isAdded()){
                return; //to avoid crash
            }
            //handle error
            if (!isAdded()) return;
//            catProgress.setVisibility(View.GONE);
            categoriesRecycler.setVisibility(View.VISIBLE);
            categoriesShimmer.stopShimmer();
            categoriesShimmer.setVisibility(View.GONE);
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
    public void parseCatJSONData(String json) {
        categoriesLists = new ArrayList<>();
        //        add default all
//        categoriesLists.add(new CategoriesList("All", "all", null, null));

        try {
            JSONArray array = new JSONArray(json);
            if (!isAdded()) return;

            for (int i = 0; i < array.length(); i++) {
                JSONObject category = array.getJSONObject(i);
                categoriesLists.add(new CategoriesList(category.getString("name"), category.getString("slug"), category.getString("count"), null, category.getString("image"), category.getString("icon")));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!isAdded()) return;
        CategoriesRecyclerAdapter adapter = new CategoriesRecyclerAdapter(getActivity(), categoriesLists);
        categoriesRecycler.setAdapter(adapter);
        categoriesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoriesRecycler.setVisibility(View.VISIBLE);
        categoriesShimmer.stopShimmer();
        categoriesShimmer.setVisibility(View.GONE);

    }




    @Override
    public void onStop() {
        super.onStop();
        requestQueue.cancelAll(requireContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        requestQueue.cancelAll(requireContext());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        requestQueue.cancelAll(requireContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requestQueue.cancelAll(requireContext());
    }
}


