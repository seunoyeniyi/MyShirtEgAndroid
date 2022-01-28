package com.fatima.fabric.adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import com.fatima.fabric.R;
import com.fatima.fabric.handler.UserSession;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AttributesListAdapter extends BaseAdapter {

    Context context;
    ArrayList<AttributesList> attributesList;
    LayoutInflater layoutInflater;
    UserSession userSession;
    Activity activity;
    Button addToCartBtn;
    ProgressBar priceVariationProgressBar;
    TextView variationPriceView;
    private AttributeCallback callback;
    public AttributesListAdapter(Context context, ArrayList<AttributesList> attributesLists, Button addToCartBtn, ProgressBar priceVariationProgressBar, TextView variationPriceView) {
        this.context = context;
        this.attributesList = attributesLists;
        this.activity = (Activity) context;
        this.addToCartBtn = addToCartBtn;
        this.priceVariationProgressBar = priceVariationProgressBar;
        this.variationPriceView = variationPriceView;
        userSession = new UserSession(context.getApplicationContext());

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
        List<String> options = new ArrayList<String>();
        options.add("Choose an option");

        try {
            JSONArray array = new JSONArray(attributesList.get(position).getOptions());
            for (int i = 0; i < array.length(); i++) {
                JSONObject option = new JSONObject((array.getString(i)));
                options.add(option.getString("value"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_dropdown_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);

        labelView.setText(attributesList.get(position).getLabel());
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinner.getSelectedItemPosition() != 0) {
                    String value = spinner.getSelectedItem().toString();
                    if (callback != null) {
//                        Log.e("name:", attributesList.get(position).getName());
//                        Log.e("value:", value);
                        callback.addToOptions(attributesList.get(position).getName(), value);
                    }
                } else {
                    addToCartBtn.setEnabled(false);
                    variationPriceView.setVisibility(View.INVISIBLE);
                    priceVariationProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return rowView;
    }

    public void setCallback(AttributeCallback callback) {
        this.callback = callback;
    }

    public interface AttributeCallback {
        public void addToOptions(String name, String value);
    }
}
