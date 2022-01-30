package com.myshirt.eg.ui;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import  com.myshirt.eg.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.myshirt.eg.Site;
import com.myshirt.eg.adapter.AttributeTermList;
import com.myshirt.eg.adapter.BrandList;
import com.myshirt.eg.adapter.BrandsAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.RangeSlider;
import com.myshirt.eg.adapter.CategoriesList;
import com.myshirt.eg.adapter.TagsList;
import com.myshirt.eg.handler.PriceFormatter;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilterSheetDialog extends BottomSheetDialogFragment {
    Context context;
    View root;

    ArrayList<CategoriesList> categories;
    ArrayList<TagsList> tags;
    ArrayList<AttributeTermList> colors;
    ArrayList<AttributeTermList> sizes;

    List<String> categoriesString;
    List<String> tagsString;
    List<String> colorsString;
    List<String> sizesString;

    RangeSlider priceRange;
    Spinner catSpinner, tagSpinner, colorSpinner, sizeSpinner;
    ProgressBar catProgress, tagProgress, colorProgress, sizeProgress;
    TextView priceRangeTextView;
    MyListView brandsListView;
    ArrayList<BrandList> brandLists = new ArrayList<>();
    BrandsAdapter brandsAdapter;

    float initialLower = 1000;
    float initialUpper = 14000;

    String selected_category = "";
    String selected_tag = "";
    String selected_color = "";
    String selected_size = "";

    FilterSheetListener callback;

    public FilterSheetDialog(Context context) {
        this.context = context;
        this.callback = null;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle);
    }
    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = View.inflate(getContext(), R.layout.filter_bottom_sheet_dialog, null);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();


        priceRange = (RangeSlider) root.findViewById(R.id.priceRange);
        catSpinner = (Spinner) root.findViewById(R.id.categoriesSortSpinner);
        tagSpinner = (Spinner) root.findViewById(R.id.tagsSortSpinner);
        colorSpinner = (Spinner) root.findViewById(R.id.colorsSortSpinner);
        sizeSpinner = (Spinner) root.findViewById(R.id.sizesSortSpinner);

        catProgress = (ProgressBar) root.findViewById(R.id.categories_progress);
        tagProgress = (ProgressBar) root.findViewById(R.id.tags_progress);
        colorProgress = (ProgressBar) root.findViewById(R.id.colors_progress);
        sizeProgress = (ProgressBar) root.findViewById(R.id.sizes_progress);

        priceRangeTextView = (TextView) root.findViewById(R.id.price_range_text);

        priceRangeTextView.setText(Site.CURRENCY + PriceFormatter.format(String.valueOf(priceRange.getValues().get(0))) + " - " + Site.CURRENCY + PriceFormatter.format(String.valueOf(priceRange.getValues().get(1))));

        priceRange.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                priceRangeTextView.setText(Site.CURRENCY + PriceFormatter.format(String.valueOf(priceRange.getValues().get(0))) + " - " + Site.CURRENCY + PriceFormatter.format(String.valueOf(priceRange.getValues().get(1))));
            }
        });


        catProgress.setVisibility(View.GONE);
        tagProgress.setVisibility(View.GONE);
        colorProgress.setVisibility(View.GONE);
        sizeProgress.setVisibility(View.GONE);

        //don't fetch again if already fetched
        boolean areNull = false;
        if (categories == null ||  tags == null ||  colors == null ||  sizes == null) {
            areNull = true;
            categories = new ArrayList<>();
            tags = new ArrayList<>();
            colors = new ArrayList<>();
            sizes = new ArrayList<>();

            categoriesString = new ArrayList<String>();
            tagsString = new ArrayList<String>();
            colorsString = new ArrayList<String>();
            sizesString = new ArrayList<String>();

            //for categories spinner
            categoriesString.add("Select a category"); //default
            categories.add(new CategoriesList("Select a category", "", "", "", "", "")); //default

            //for tags spinner
            tagsString.add("Select a tag"); //default
            tags.add(new TagsList("Select a tag", "")); //default

            //for colors spinner
            colorsString.add("Any color"); //default
            colors.add(new AttributeTermList("Any color",  "", "")); //default

            //for sizes spinner
            sizesString.add("Any size"); //default
            sizes.add(new AttributeTermList("Any size",  "", "")); //default
        }

            //cat spinner
            ArrayAdapter<String> catSortAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_item_shop, categoriesString);
            catSortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
            catSpinner.setAdapter(catSortAdapter);

            //tag spinner
            ArrayAdapter<String> tagSortAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_item_shop, tagsString);
            tagSortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
            tagSpinner.setAdapter(tagSortAdapter);

            //color spinner
            ArrayAdapter<String> colorSortAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_item_shop, colorsString);
            colorSortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
            colorSpinner.setAdapter(colorSortAdapter);

            //size spinner
            ArrayAdapter<String> sizeSortAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_item_shop, sizesString);
            sizeSortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
            sizeSpinner.setAdapter(sizeSortAdapter);


            if (areNull) {
                fetchCategories();
                fetchTags();
                fetchColors();
                fetchSizes();
            }






        catSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected_category = categories.get(i).getSlug();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selected_tag = tags.get(i).getSlug();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected_color = colors.get(i).getSlug();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected_size = sizes.get(i).getSlug();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });


        Button applyFilter = (Button) root.findViewById(R.id.applyFilter);
            assert applyFilter != null;
            applyFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<Float> range = priceRange.getValues();
                    boolean applyPriceRange = (range.get(0) != initialLower || range.get(1) != initialUpper); //if initial price has changed

                    if (callback != null) {
                        callback.onApplyClicked(selected_category, selected_tag, selected_color, applyPriceRange, range, selected_size);
                    }
                    dismiss();
                }
            });


            ImageButton cancelBtn = (ImageButton) root.findViewById(R.id.cancel_sheet);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });


        return  root;
    }


    public void setOnCallback(FilterSheetListener callback) {
        this.callback = callback;
    }

    public void fetchCategories() {
        catProgress.setVisibility(View.VISIBLE);

        String url = Site.CATEGORIES + "?hide_empty=1&order_by=menu_order";

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                if(context == null || !isAdded()){
                    return; //to avoid crash
                }
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject category = array.getJSONObject(i);
                        categories.add(new CategoriesList(category.getString("name"), category.getString("slug"), "", "", "", ""));
                        categoriesString.add(category.getString("name"));
                    }

                    ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_item_shop, categoriesString);
                    sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
                    catSpinner.setAdapter(sortAdapter);
                    catProgress.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, (VolleyError error) -> {
            if(context == null || !isAdded()){
                return; //to avoid crash
            }
            //handle error
            Toast.makeText(context, "Can't get categories.", Toast.LENGTH_LONG).show();
            catProgress.setVisibility(View.GONE);
        });
        RequestQueue requestQueue =  Volley.newRequestQueue(requireContext());
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
    public void fetchTags() {
        tagProgress.setVisibility(View.GONE);
//        tagProgress.setVisibility(View.VISIBLE);
//
//        String url = Site.TAGS  + "?hide_empty=1&order_by=menu_order";
//
//        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
//            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//            @Override
//            public void onResponse(String response) {
//                if(context == null || !isAdded()){
//                    return; //to avoid crash
//                }
//                try {
//                    JSONArray array = new JSONArray(response);
//                    for (int i = 0; i < array.length(); i++) {
//                        JSONObject tag = array.getJSONObject(i);
//                        tags.add(new TagsList(tag.getString("name"), tag.getString("slug")));
//                        tagsString.add(tag.getString("name"));
//                    }
//
//                    ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_item_shop, tagsString);
//                    sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
//                    tagSpinner.setAdapter(sortAdapter);
//                    tagProgress.setVisibility(View.GONE);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, (VolleyError error) -> {
//            if(context == null || !isAdded()){
//                return; //to avoid crash
//            }
//            //handle error
//            Toast.makeText(context, "Can't get tags.", Toast.LENGTH_LONG).show();
//            tagProgress.setVisibility(View.GONE);
//        });
//        RequestQueue requestQueue =  Volley.newRequestQueue(requireContext());
//        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        requestQueue.add(request);
    }
    public void fetchColors() {
        colorProgress.setVisibility(View.GONE);
//        colorProgress.setVisibility(View.VISIBLE);
//
//        String url = Site.ATTRIBUTES  + "?name=color";
//
//
//        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
//            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//            @Override
//            public void onResponse(String response) {
//                if(context == null || !isAdded()){
//                    return; //to avoid crash
//                }
//                try {
//                    JSONObject object = new JSONObject(response);
//                    JSONArray array = object.getJSONArray("terms");
//
//                    for (int i = 0; i < array.length(); i++) {
//                        JSONObject term = array.getJSONObject(i);
//                        colors.add(new AttributeTermList(term.getString("name"), term.getString("taxonomy"), term.getString("slug")));
//                        colorsString.add(term.getString("name"));
//                    }
//
//                    ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_item_shop, colorsString);
//                    sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
//                    colorSpinner.setAdapter(sortAdapter);
//                    colorProgress.setVisibility(View.GONE);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, (VolleyError error) -> {
//            if(context == null || !isAdded()){
//                return; //to avoid crash
//            }
//            //handle error
//            Toast.makeText(context, "Can't get colors.", Toast.LENGTH_LONG).show();
//            colorProgress.setVisibility(View.GONE);
//        });
//        RequestQueue requestQueue =  Volley.newRequestQueue(requireContext());
//        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        requestQueue.add(request);
    }
    public void fetchSizes() {

        sizeProgress.setVisibility(View.VISIBLE);

        String url = Site.ATTRIBUTES  + "?name=size";


        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                if(context == null || !isAdded()){
                    return; //to avoid crash
                }
                try {
                    Log.e("RES", response);
                    JSONObject object = new JSONObject(response);
                    JSONArray array = object.getJSONArray("terms");

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject term = array.getJSONObject(i);
                        sizes.add(new AttributeTermList(term.getString("name"), term.getString("taxonomy"), term.getString("slug")));
                        sizesString.add(term.getString("name"));
                    }

                    ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_item_shop, sizesString);
                    sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
                    sizeSpinner.setAdapter(sortAdapter);
                    sizeProgress.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, (VolleyError error) -> {
            if(context == null || !isAdded()){
                return; //to avoid crash
            }
            //handle error
            Toast.makeText(context, "Can't get sizes.", Toast.LENGTH_LONG).show();
            sizeProgress.setVisibility(View.GONE);
        });
        RequestQueue requestQueue =  Volley.newRequestQueue(requireContext());
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);


            }
        });
        return  dialog;
    }
    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public interface FilterSheetListener  {
        public void onApplyClicked(String category, String tag, String selected_color, boolean applyPriceRange, List<Float> range, String selected_size);
    }
}

