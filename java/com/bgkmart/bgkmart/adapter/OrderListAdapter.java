package com.fatima.fabric.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fatima.fabric.R;
import com.fatima.fabric.handler.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrderListAdapter extends BaseAdapter {
    ArrayList<OrderList> orderLists;
    UserSession userSession;
    Context context;
    public OrderListAdapter(Context context, ArrayList<OrderList> orderLists) {
        this.context = context;
        this.orderLists = orderLists;
        userSession = new UserSession(context.getApplicationContext());
    }
    @Override
    public int getCount() {
        return orderLists.size();
    }

    @Override
    public Object getItem(int i) {
        return orderLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = layoutInflater.inflate(R.layout.order_list_layout, null);
        TextView orderIdView = rowView.findViewById(R.id.orderIdTextView);
        TextView orderDateView = rowView.findViewById(R.id.orderDateView);
        TextView orderStatusView = rowView.findViewById(R.id.orderStatusView);
        TextView priceAndCountView = rowView.findViewById(R.id.priceAndCountView);

        //set color for pending view
        switch ( orderLists.get(position).getStatus()) {
            case "complete":
                orderStatusView.setTextColor(Color.parseColor("#000000"));
                break;
            case "processing":
                orderStatusView.setTextColor(Color.parseColor("#CCCC00"));
                break;
            case "pending":
                orderStatusView.setTextColor(Color.parseColor("#FF0000"));
                break;
            default:
                break;
        }

        orderIdView.setText("#" + orderLists.get(position).getId());
        try {
            JSONObject dateObject = new JSONObject(orderLists.get(position).getDate());
            @SuppressLint("SimpleDateFormat") Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dateObject.getString("date"));
            orderDateView.setText(date.toString());
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        orderStatusView.setText(Html.fromHtml("&#9679;"));
//        double price = Double.parseDouble(orderLists.get(position).getPrice());
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        priceAndCountView.setText(orderLists.get(position).getPrice() + "Rs" + " for " + orderLists.get(position).getItem_count() + "items");

        return rowView;
    }
}
