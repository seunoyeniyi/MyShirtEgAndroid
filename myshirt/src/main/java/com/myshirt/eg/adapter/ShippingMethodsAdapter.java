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
import com.myshirt.eg.Site;
import com.myshirt.eg.handler.PriceFormatter;

import java.util.ArrayList;

public class ShippingMethodsAdapter extends BaseAdapter {

    Context context;
    ArrayList<ShippingMethodList> shippingMethodLists;
    public ShippingMethodsAdapter(Context context, ArrayList<ShippingMethodList> shippingMethodLists) {
        this.context = context;
        this.shippingMethodLists = shippingMethodLists;
    }

    @Override
    public int getCount() {
        return shippingMethodLists.size();
    }

    @Override
    public Object getItem(int i) {
        return shippingMethodLists.get(i);
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
        checkedTextView.setText(Html.fromHtml(shippingMethodLists.get(position).getTitle() + " (<b>" + Site.CURRENCY + PriceFormatter.format(shippingMethodLists.get(position).getCost()) + "</b>)"));
        return rowView;
    }
}