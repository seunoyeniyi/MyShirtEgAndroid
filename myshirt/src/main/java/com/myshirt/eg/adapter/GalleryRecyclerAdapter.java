package com.myshirt.eg.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GalleryRecyclerAdapter extends RecyclerView.Adapter<GalleryRecyclerAdapter.ViewHolder> {
    // Start with first item selected
    private int focusedItem = 0;
    List<String> galleryRecyclerLists;
    Activity activity;
    public GalleryRecyclerAdapter(Activity activity, List<String> galleryRecyclerLists) {
        this.activity = activity;
        this.galleryRecyclerLists = galleryRecyclerLists;
    }

    @NotNull
    @Override
    public GalleryRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.single_gallery_recycler_layout, parent, false);

        if (galleryRecyclerLists.size() < 2) {
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, activity.getResources().getDisplayMetrics()));
            contactView.setLayoutParams(params);
        } else {
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, activity.getResources().getDisplayMetrics());
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(width, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, activity.getResources().getDisplayMetrics()));
            params.setMargins(0, 0, 10, 0);
            contactView.setLayoutParams(params);
        }


        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(GalleryRecyclerAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Get the data model based on position
        // Set item views based on your views and data model
        holder.itemView.setSelected(focusedItem == position);

        ImageView galleryImage = holder.galleryImage;
        RelativeLayout container = holder.container;


            Glide
                    .with(activity)
                    .load(galleryRecyclerLists.get(position).replace("localhost", Site.DOMAIN))
                    .placeholder(R.drawable.sample_placeholder)
                    .into(galleryImage);




    }

    // Returns the total count of items eg the list
    @Override
    public int getItemCount() {
        return galleryRecyclerLists.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public ImageView galleryImage;
        public RelativeLayout container;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView eg a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            galleryImage = (ImageView) itemView.findViewById(R.id.gallery_image);
            container = (RelativeLayout) itemView.findViewById(R.id.container);
        }
    }
}