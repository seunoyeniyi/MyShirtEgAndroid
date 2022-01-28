package com.myshirt.eg.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myshirt.eg.MainActivity;
import com.bumptech.glide.Glide;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CategoriesRecyclerAdapter extends RecyclerView.Adapter<CategoriesRecyclerAdapter.ViewHolder> {
    // Start with first item selected
    private int focusedItem = 0;
    List<CategoriesList> categoriesLists;
    Activity activity;
    public CategoriesRecyclerAdapter(Activity activity, List<CategoriesList> categoriesLists) {
        this.activity = activity;
        this.categoriesLists = categoriesLists;
    }
    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // Handle key up and key down and attempt to move selection
        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

                // Return false if scrolled to the bounds and allow focus to move off the list
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return tryMoveSelection(lm, -1);
                    }
                }

                return false;
            }
        });
    }
    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int tryFocusItem = focusedItem + direction;

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
            notifyItemChanged(focusedItem);
            focusedItem = tryFocusItem;
            notifyItemChanged(focusedItem);
            lm.scrollToPosition(focusedItem);
            return true;
        }

        return false;
    }

    @NotNull
    @Override
    public CategoriesRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.single_cateogry_recycler_layout, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(CategoriesRecyclerAdapter.ViewHolder holder, int position) {
        // Get the data model based on position
        // Set item views based on your views and data model
//        holder.itemView.setSelected(focusedItem == position);

        TextView catTextView = holder.catTextView;
        ImageView catImage = holder.catImage;
        LinearLayout catLayout = holder.catLayout;
        catTextView.setText(Html.fromHtml(categoriesLists.get(position).getName()));

        if (!categoriesLists.get(position).getImage().equals("false")) {
            Glide
                    .with(activity)
                    .load(categoriesLists.get(position).getImage().replace("localhost", Site.DOMAIN))
                    .placeholder(R.drawable.sample_placeholder)
                    .into(catImage);
        } else {
            catImage.setImageResource(R.drawable.icons8_star_1);
        }

        catLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open new categories
                if (categoriesLists.get(position).getSlug().equals("all"))
                    return;

//                activity.startActivity(new Intent(activity, ArchiveFragment.class)
//                        .putExtra("category_name", categoriesLists.get(position).getSlug())
//                        .putExtra("cat_title", categoriesLists.get(position).getName()));
                Bundle bundle = new Bundle();
                bundle.putString("cat_title", categoriesLists.get(position).getName());
                bundle.putString("category_name", categoriesLists.get(position).getSlug());
                ((MainActivity) activity).changeFragment(R.id.archive_fragment, bundle);
            }
        });

    }

    // Returns the total count of items eg the list
    @Override
    public int getItemCount() {
        return categoriesLists.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView catTextView;
        public LinearLayout catLayout;
        public ImageView catImage;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView eg a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            catTextView = (TextView) itemView.findViewById(R.id.recyclerTitle);
            catLayout = (LinearLayout) itemView.findViewById(R.id.catLayout);
            catImage = (ImageView) itemView.findViewById(R.id.recyclerImage);
        }
    }
}