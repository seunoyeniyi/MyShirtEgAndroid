package com.fatima.fabric.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fatima.fabric.R;
import com.fatima.fabric.handler.UserSession;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderDetailsAdapter extends BaseAdapter {
    ArrayList<OrderDetailsList> orderDetailsLists;
    UserSession userSession;
    Context context;
    public OrderDetailsAdapter(Context context, ArrayList<OrderDetailsList> orderDetailsLists) {
        this.context = context;
        this.orderDetailsLists = orderDetailsLists;
        userSession = new UserSession(context.getApplicationContext());
    }
    @Override
    public int getCount() {
        return orderDetailsLists.size();
    }

    @Override
    public Object getItem(int i) {
        return orderDetailsLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = layoutInflater.inflate(R.layout.single_order_details_layout, null);
        TextView productTitleView = rowView.findViewById(R.id.productTitleView);
        TextView productPriceView = rowView.findViewById(R.id.productPriceView);

        productTitleView.setText(Html.fromHtml(orderDetailsLists.get(position).getName() + "<font color='black'><b> x " + orderDetailsLists.get(position).getCount() + "</b></font>"));
//        double price = Double.parseDouble(orderDetailsLists.get(position).getPrice());
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        productPriceView.setText(orderDetailsLists.get(position).getPrice() + "Rs");

        return rowView;
    }
}
