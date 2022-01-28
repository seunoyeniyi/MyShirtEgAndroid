package com.myshirt.eg.adapter;


import android.annotation.SuppressLint;

import com.myshirt.eg.R;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AttributesListAdapter extends BaseAdapter {

    Context context;
    ArrayList<AttributesList> attributesList;
    LayoutInflater layoutInflater;
    Button addToCartBtn;
    ProgressBar priceVariationProgressBar;
    TextView variationPriceView;
    boolean firstTimeSpinner = true;
    private AttributeCallback callback;
    public AttributesListAdapter(Context context, ArrayList<AttributesList> attributesLists, Button addToCartBtn, ProgressBar priceVariationProgressBar, TextView variationPriceView) {
        this.context = context;
        this.attributesList = attributesLists;
        this.addToCartBtn = addToCartBtn;
        this.priceVariationProgressBar = priceVariationProgressBar;
        this.variationPriceView = variationPriceView;

    }

    @Override
    public int getCount() {
        return attributesList.size();
    }

    @Override
    public Object getItem(int i) {
        return attributesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;

        rowView = layoutInflater.inflate(R.layout.single_attribute_layout, null);
        TextView labelView =(TextView) rowView.findViewById(R.id.attributeLabel);
        Spinner spinner = rowView.findViewById(R.id.attributeSpinner);
        RecyclerView swatch = rowView.findViewById(R.id.attributeSwatch);

        if (attributesList.get(position).getLabel().toLowerCase().equals("size")) {
            swatch.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);
        } else {
            swatch.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
        }


        List<OptionList> options = new ArrayList<OptionList>();
        List<OptionList> swatchOptions = new ArrayList<OptionList>();
        options.add(new OptionList("null", "Choose an option"));

        try {
            JSONArray array = attributesList.get(position).getOptions();
            //reverse loop
            for (int i = array.length() - 1; i >= 0; i--) {
                JSONObject option = new JSONObject((array.getString(i)));
                Log.e("Attr Name", option.getString("value"));
                options.add(new OptionList(option.getString("name"), option.getString("value")));
                swatchOptions.add(new OptionList(option.getString("name"), option.getString("value")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



        String label = attributesList.get(position).getLabel();
        labelView.setText(Html.fromHtml(label.substring(0, 1).toUpperCase() + label.substring(1)));


        if (attributesList.get(position).getLabel().toLowerCase().equals("size")) {
            SwatchesAdapter adapter = new SwatchesAdapter(swatchOptions);
            swatch.setAdapter(adapter);
            swatch.setLayoutManager(new GridLayoutManager(context, 5));
            adapter.setOnWatchSelected(new SwatchesAdapter.SwatchListener() {
                @Override
                public void swatchSelected(String name, String value, int i) {
                    callback.addToOptions(attributesList.get(position).getName(), swatchOptions.get(i).getName());
                }
            });

            //select first
            adapter.notifyItemChanged(0);
            callback.addToOptions(attributesList.get(0).getName(), swatchOptions.get(0).getName());

        } else {
            OptionAdapter adapter = new OptionAdapter(context, options);
//        adapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (spinner.getSelectedItemPosition() != 0) {
                        String value = spinner.getSelectedItem().toString();
                        if (callback != null) {
//                        Log.e("name:", attributesList.get(position).getName());
//                        Log.e("value:", options.get(i).getName());
                            callback.addToOptions(attributesList.get(position).getName(), options.get(i).getName());
                        }
                    } else {
                        callback.addToOptions(attributesList.get(position).getName(), "");
                        if (firstTimeSpinner) {
                            firstTimeSpinner = false;
                        } else {
                            addToCartBtn.setEnabled(false);
                        }

                        variationPriceView.setVisibility(View.GONE);
                        priceVariationProgressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }



        return rowView;
    }



    public void setCallback(AttributeCallback callback) {
        this.callback = callback;
    }

    public interface AttributeCallback {
        public void addToOptions(String name, String value);
    }
}
