package com.myshirt.eg.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import  com.myshirt.eg.R;

import java.util.List;

public class OptionAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    List<OptionList> optionLists;
    public OptionAdapter(Context applicationContext, List<OptionList> optionLists) {
        this.optionLists = optionLists;
        this.context = applicationContext;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return optionLists.size();
    }

    @Override
    public Object getItem(int i) {
        return optionLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.spinner_dropdown_item, null);
        TextView valueText = (TextView) view.findViewById(android.R.id.text1);

        valueText.setText(optionLists.get(i).getValue());
        return view;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.spinner_dropdown_menu_item, null);
        TextView valueText = (TextView) view.findViewById(android.R.id.text1);

        valueText.setText(optionLists.get(position).getValue());
        return view;
    }

}
