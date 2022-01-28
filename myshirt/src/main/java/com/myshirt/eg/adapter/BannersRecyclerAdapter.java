package com.myshirt.eg.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.myshirt.eg.MainActivity;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.ui.BrowserActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BannersRecyclerAdapter extends RecyclerView.Adapter<BannersRecyclerAdapter.ViewHolder> {
    // Start with first item selected
    private int focusedItem = 0;
    List<BannerRecyclerClass> bannerRecyclerLists;
    Activity activity;
    int layout;
    public BannersRecyclerAdapter(Activity activity, int layout, List<BannerRecyclerClass> bannerRecyclerLists) {
        this.activity = activity;
        this.layout = layout;
        this.bannerRecyclerLists = bannerRecyclerLists;
    }

    @NotNull
    @Override
    public BannersRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(layout, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(BannersRecyclerAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Get the data model based on position
        // Set item views based on your views and data model
        holder.itemView.setSelected(focusedItem == position);

        ImageView bannerImage = holder.bannerImage;
        TextView bannerTitle = holder.bannerTitle;
        TextView bannerDescription = holder.bannerDescription;
        RelativeLayout parentLayout = holder.parentLayout;

        bannerTitle.setText(Html.fromHtml(bannerRecyclerLists.get(position).getTitle().replace("\\", "")));
        bannerDescription.setText(Html.fromHtml(bannerRecyclerLists.get(position).getDescription().replace("\\", "")));

        if (bannerRecyclerLists.get(position).isResource()) {
            bannerImage.setImageResource(bannerRecyclerLists.get(position).getFeatured_image_resource());
        } else {
            Glide
                    .with(activity)
                    .load(bannerRecyclerLists.get(position).getFeatured_image_string().replace("localhost", Site.DOMAIN))
                    .placeholder(R.drawable.sample_placeholder)
                    .into(bannerImage);
        }

        parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bannerRecyclerLists.get(position).getOn_click_to().equals("category")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("cat_title", bannerRecyclerLists.get(position).getTitle());
                    bundle.putString("category_name", bannerRecyclerLists.get(position).getCategory());
                    bundle.putString("category_description", bannerRecyclerLists.get(position).getDescription());
                    ((MainActivity) activity).changeFragment(R.id.archive_fragment, bundle);
                } else if (bannerRecyclerLists.get(position).getOn_click_to().equals("url")) {
                    String url = bannerRecyclerLists.get(position).getUrl();
                    Intent intent = new Intent(((MainActivity) activity), BrowserActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("title", bannerRecyclerLists.get(position).getTitle());
                    ((MainActivity) activity).startActivity(intent);
                } else if (bannerRecyclerLists.get(position).getOn_click_to().equals("shop")) {
                    ((MainActivity) activity).changeFragment(R.id.shop_fragment);
                }
            }
        });


    }

    // Returns the total count of items eg the list
    @Override
    public int getItemCount() {
        return bannerRecyclerLists.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView bannerTitle;
        public TextView bannerDescription;
        public ImageView bannerImage;
        public RelativeLayout parentLayout;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView eg a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            bannerTitle = (TextView) itemView.findViewById(R.id.bannerTitle);
            bannerDescription = (TextView) itemView.findViewById(R.id.bannerDescription);
            bannerImage = (ImageView) itemView.findViewById(R.id.bannerImage);
            parentLayout = (RelativeLayout) itemView.findViewById(R.id.parentLayout);
        }
    }
}