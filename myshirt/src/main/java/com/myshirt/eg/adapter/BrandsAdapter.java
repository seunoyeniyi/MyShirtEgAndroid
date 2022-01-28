package com.myshirt.eg.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.myshirt.eg.R;

import java.util.ArrayList;

public class BrandsAdapter extends BaseAdapter {

    Context context;
    ArrayList<BrandList> brandLists;
    public BrandsAdapter(Context context, ArrayList<BrandList> brandLists) {
        this.context = context;
        this.brandLists = brandLists;
    }

    @Override
    public int getCount() {
        return brandLists.size();
    }

    @Override
    public Object getItem(int i) {
        return brandLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("SetTextI18n")
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @SuppressLint({"ViewHolder", "InflateParams"})
        View rowView = layoutInflater.inflate(R.layout.multiple_list_selection, null);
        CheckedTextView checkedTextView = (CheckedTextView) rowView.findViewById(R.id.checkedText);
        checkedTextView.setText(Html.fromHtml(brandLists.get(position).getName() + " (" + brandLists.get(position).getCount() + ")"));
        return rowView;
    }
}
