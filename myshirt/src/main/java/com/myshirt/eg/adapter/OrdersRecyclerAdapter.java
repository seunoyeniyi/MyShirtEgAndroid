package com.myshirt.eg.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.handler.PriceFormatter;
import com.myshirt.eg.ui.OrderActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OrdersRecyclerAdapter extends RecyclerView.Adapter<OrdersRecyclerAdapter.ViewHolder> {
    // Start with first item selected
    List<OrdersRecyclerClass> ordersRecyclerLists;
    Activity activity;
    public OrdersRecyclerAdapter(Activity activity, List<OrdersRecyclerClass> ordersRecyclerLists) {
        this.activity = activity;
        this.ordersRecyclerLists = ordersRecyclerLists;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.single_order_list_layout, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        // Set item views based on your views and data model

        TextView orderCode = holder.orderCode;
        TextView dateView = holder.dateView;
        TextView paymentMethodView = holder.paymentMethodView;
        TextView amountView = holder.amountView;
        ImageView statusIcon = holder.statusIcon;
        RelativeLayout orderLayout = holder.orderLayout;

        orderCode.setText("#" + ordersRecyclerLists.get(position).getId());
        //format date
        String order_date = ordersRecyclerLists.get(position).getDate();

        dateView.setText(order_date);
        paymentMethodView.setText(Site.payment_method_title(ordersRecyclerLists.get(position).getPayment_method()));
        amountView.setText(Site.CURRENCY + PriceFormatter.format(ordersRecyclerLists.get(position).getAmount()));

        if (ordersRecyclerLists.get(position).getStatus().equals("processing") || ordersRecyclerLists.get(position).getStatus().equals("completed")) {
            statusIcon.setImageResource(R.drawable.icons8_checked);
        } else {
            statusIcon.setImageResource(R.drawable.icons8_data_pending_2);
        }
        if (!ordersRecyclerLists.get(position).getStatus().equals("completed") && ordersRecyclerLists.get(position).getPayment_method().equals("cod")) {
            statusIcon.setImageResource(R.drawable.icons8_data_pending_2);
        }
        if (ordersRecyclerLists.get(position).getPayment_method().equals("paypal") && ordersRecyclerLists.get(position).getStatus().equals("on-hold")) {
            statusIcon.setImageResource(R.drawable.icons8_cancel_1);
        }


        orderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(activity, OrderActivity.class).putExtra("order_id", ordersRecyclerLists.get(position).getId()));
            }
        });


    }

    // Returns the total count of items eg the list
    @Override
    public int getItemCount() {
        return ordersRecyclerLists.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView orderCode;
        public TextView dateView;
        public TextView paymentMethodView;
        public ImageView statusIcon;
        public TextView amountView;
        public RelativeLayout orderLayout;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView eg a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            orderCode = (TextView) itemView.findViewById(R.id.orderCode);
            dateView = (TextView) itemView.findViewById(R.id.orderDate);
            paymentMethodView = (TextView) itemView.findViewById(R.id.paymentMethod);
            amountView = (TextView) itemView.findViewById(R.id.orderAmount);
            statusIcon = (ImageView) itemView.findViewById(R.id.paymentStatusIcon);
            orderLayout = (RelativeLayout) itemView.findViewById(R.id.orderLayout);
        }
    }

}