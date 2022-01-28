package com.myshirt.eg.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.recyclerview.widget.RecyclerView;

import com.myshirt.eg.MainActivity;
import com.myshirt.eg.R;
import com.myshirt.eg.ui.BrowserActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VideoBannerRecyclerAdapter extends RecyclerView.Adapter<VideoBannerRecyclerAdapter.ViewHolder> {
    // Start with first item selected
    private int focusedItem = 0;
    List<BannerRecyclerClass> bannerRecyclerLists;
    Activity activity;
    int layout;
    public VideoBannerRecyclerAdapter(Activity activity, int layout, List<BannerRecyclerClass> bannerRecyclerLists) {
        this.activity = activity;
        this.layout = layout;
        this.bannerRecyclerLists = bannerRecyclerLists;
    }

    @NotNull
    @Override
    public VideoBannerRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(layout, parent, false);

        // Return a new holder instance
        VideoBannerRecyclerAdapter.ViewHolder viewHolder = new VideoBannerRecyclerAdapter.ViewHolder(contactView);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(VideoBannerRecyclerAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Get the data model based on position
        // Set item views based on your views and data model
        holder.itemView.setSelected(focusedItem == position);

        VideoView videoView = holder.videoView;
        TextView bannerTitle = holder.bannerTitle;
        TextView bannerDescription = holder.bannerDescription;
        RelativeLayout parentLayout = holder.parentLayout;

        bannerTitle.setText(Html.fromHtml(bannerRecyclerLists.get(position).getTitle().replace("\\", "")));
        bannerDescription.setText(Html.fromHtml(bannerRecyclerLists.get(position).getDescription().replace("\\", "")));

        if (bannerRecyclerLists.get(position).isResource()) {
//            bannerImage.setImageResource(bannerRecyclerLists.get(position).getFeatured_image_resource());
        } else {
            //from web
            Log.e("Video", bannerRecyclerLists.get(position).getFeatured_image_string());
            Uri uri = Uri.parse(bannerRecyclerLists.get(position).getFeatured_image_string());
            videoView.setVideoURI(uri);
            MediaController mediaController = new MediaController(activity);

            // sets the anchor view
            // anchor view for the videoView
            mediaController.setAnchorView(videoView);

            // sets the media player to the videoView
            mediaController.setMediaPlayer(videoView);

            // sets the media controller to the videoView
            videoView.setMediaController(mediaController);

            videoView.seekTo( 1 );
            // starts the video
//            videoView.start();
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
        public VideoView videoView;
        public RelativeLayout parentLayout;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView eg a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            bannerTitle = (TextView) itemView.findViewById(R.id.bannerTitle);
            bannerDescription = (TextView) itemView.findViewById(R.id.bannerDescription);
            videoView = (VideoView) itemView.findViewById(R.id.videoView);
            parentLayout = (RelativeLayout) itemView.findViewById(R.id.parentLayout);
        }
    }
}