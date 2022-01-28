package com.fatima.fabric.ui.categories;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fatima.fabric.ArchiveActivity;
import com.fatima.fabric.R;
import com.fatima.fabric.Site;
import com.fatima.fabric.adapter.CategoriesAdapter;
import com.fatima.fabric.adapter.CategoriesList;
import com.fatima.fabric.adapter.ProductList;
import com.fatima.fabric.handler.UserSession;
import com.fatima.fabric.ui.MyExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoriesFragment extends Fragment {


    View root;
    UserSession userSession;
    Dialog loadingDialog;

    ProgressBar progressBar;
    Button refreshBtn;
    MyExpandableListView categoriesListView;
    ExpandableListAdapter categoriesAdapter;
    List<CategoriesList> categoryParents;
    HashMap<CategoriesList, List<CategoriesList>> categoriesDetails;

    public CategoriesFragment() {
        // Required empty public constructor
    }


    public static CategoriesFragment newInstance(String param1, String param2) {
        CategoriesFragment fragment = new CategoriesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_categories, container, false);
        userSession = new UserSession(requireActivity().getApplicationContext());
        loadingDialog = new Dialog(getActivity(), android.R.style.Theme_Light);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
//        loadingDialog.show();

        progressBar = (ProgressBar) root.findViewById(R.id.categoriesProgress);
        refreshBtn = (Button) root.findViewById(R.id.refreshBtn);
        refreshBtn.setVisibility(View.GONE);
        categoriesListView = (MyExpandableListView) root.findViewById(R.id.categoriesListView);
        categoriesDetails = new HashMap<CategoriesList, List<CategoriesList>>();
        categoryParents = new ArrayList<CategoriesList>(categoriesDetails.keySet());
        categoriesAdapter = new CategoriesAdapter(requireActivity(), categoryParents, categoriesDetails);
        categoriesListView.setAdapter(categoriesAdapter);

        categoriesListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                String slug = categoryParents.get(i).getSlug();
                String title = categoryParents.get(i).getName();
                String sub_cat = categoryParents.get(i).getSub_cats();
                if (sub_cat.equals("0")) { //open category activity with it's slug
                    startActivity(new Intent(requireContext(), ArchiveActivity.class)
                            .putExtra("category_name", slug)
                            .putExtra("cat_title", title));
                }
                return false;
            }
        });
//        categoriesListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//
//            @Override
//            public void onGroupExpand(int groupPosition) {
//                String slug = categoryParents.get(groupPosition).getSlug();
//                String title = categoryParents.get(groupPosition).getName();
//                String sub_cat = categoryParents.get(groupPosition).getSub_cats();
//                if (sub_cat.equals("0")) { //open category activity with it's slug
//                    startActivity(new Intent(requireContext(), ArchiveActivity.class)
//                            .putExtra("category_name", slug)
//                    .putExtra("cat_title", title));
//                }
//            }
//        });
        categoriesListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                String slug = categoryParents.get(groupPosition).getSlug();
                String title = categoryParents.get(groupPosition).getName();
                String sub_cat = categoryParents.get(groupPosition).getSub_cats();
                if (!sub_cat.equals("0")) { //open category activity with it's slug
                    startActivity(new Intent(requireContext(), ArchiveActivity.class)
                            .putExtra("category_name", slug)
                            .putExtra("cat_title", title));
                }

            }
        });
        categoriesListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String slug = categoriesDetails.get(categoryParents.get(groupPosition)).get(childPosition).getSlug();
                String title = categoriesDetails.get(categoryParents.get(groupPosition)).get(childPosition).getName();
                startActivity(new Intent(requireContext(), ArchiveActivity.class)
                        .putExtra("category_name", slug)
                        .putExtra("cat_title", title));
                return false;
            }
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchCategories(getActivity());
            }
        });

        fetchCategories(getContext());

        return root;
    }

    public void  fetchCategories(Context context) {
        loadingDialog.show();
        progressBar.setVisibility(View.VISIBLE);
        refreshBtn.setVisibility(View.GONE);
        String url = Site.CATEGORIES;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONData(response, context);
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(context, "Connection error. Please check your connection.", Toast.LENGTH_LONG).show();
            loadingDialog.hide();
            progressBar.setVisibility(View.GONE);
            refreshBtn.setVisibility(View.VISIBLE);
        });
        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json, Context context) {
        categoriesDetails.clear();
        categoryParents.clear();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject category = array.getJSONObject(i);

                CategoriesList parentList = new CategoriesList();
                parentList.setId(category.getString("ID"));
                parentList.setName(category.getString("name"));
                parentList.setCount(category.getString("count"));
                parentList.setLink(category.getString("link"));
                parentList.setSlug(category.getString("slug"));
                parentList.setSub_cats((category.has("sub_cats")) ? category.getString("sub_cats") : "0");

                List<CategoriesList>  children = new ArrayList<>();
                if (category.has("sub_cats")) {
                    JSONArray subArray = category.getJSONArray("sub_cats");
                    for (int j = 0; j < subArray.length(); j++) {
                        JSONObject subCat = subArray.getJSONObject(j);

                        CategoriesList childrenList = new CategoriesList();
                        childrenList.setId(subCat.getString("ID"));
                        childrenList.setName(subCat.getString("name"));
                        childrenList.setSlug(subCat.getString("slug"));
                        childrenList.setLink(subCat.getString("link"));
                        childrenList.setCount(subCat.getString("count"));

                        children.add(childrenList);
                    }
                }

                categoriesDetails.put(parentList, children);
            }
            categoryParents = new ArrayList<CategoriesList>(categoriesDetails.keySet());
            categoriesAdapter = new CategoriesAdapter(requireActivity(), categoryParents, categoriesDetails);
            categoriesListView.setAdapter(categoriesAdapter);
            for (int i = 0; i < categoriesAdapter.getGroupCount(); i++) {
                categoriesListView.expandGroup(i);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        loadingDialog.hide();
        progressBar.setVisibility(View.GONE);
        refreshBtn.setVisibility(View.GONE);


    }

}