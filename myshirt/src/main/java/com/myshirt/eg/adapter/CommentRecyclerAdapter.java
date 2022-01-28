package com.myshirt.eg.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.myshirt.eg.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder> {
    // Start with first item selected
    private int focusedItem = 0;
    List<CommentList> commentLists;
    Activity activity;
    public CommentRecyclerAdapter(Activity activity, List<CommentList> commentLists) {
        this.activity = activity;
        this.commentLists = commentLists;
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
    public CommentRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.single_review_recycler_layout, parent, false);

        // Return a new holder instance
        CommentRecyclerAdapter.ViewHolder viewHolder = new CommentRecyclerAdapter.ViewHolder(contactView);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(CommentRecyclerAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Get the data model based on position
        // Set item views based on your views and data model
//        holder.itemView.setSelected(focusedItem == position);

        TextView username = holder.username;
        TextView comment = holder.comment;
        RatingBar ratingBar = holder.ratingBar;
        CircleImageView userImage = holder.userImage;

        username.setText(commentLists.get(position).getUsername());
        comment.setText(commentLists.get(position).getComment());

        float rating = 5;
        if (!commentLists.get(position).getRating().isEmpty()) {
           rating = Float.parseFloat(commentLists.get(position).getRating());
        }

        ratingBar.setRating(rating);

        if (commentLists.get(position).getUser_image().length() > 10) { //greater than 10 will surely be an image
            Glide
                    .with(activity)
                    .load(commentLists.get(position).getUser_image())
                    .centerCrop()
                    .into(userImage);
        }



    }

    // Returns the total count of items eg the list
    @Override
    public int getItemCount() {
        return commentLists.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView username;
        public TextView comment;
        public RatingBar ratingBar;
        public CircleImageView userImage;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView eg a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.comment_username);
            comment = (TextView) itemView.findViewById(R.id.comment_text);
            ratingBar = (RatingBar) itemView.findViewById(R.id.user_rating);
            userImage = (CircleImageView) itemView.findViewById(R.id.user_pic);

        }
    }
}