package com.myshirt.eg.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.myshirt.eg.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SwatchesAdapter extends RecyclerView.Adapter<SwatchesAdapter.ViewHolder> {
    // Start with first item selected
    private int focusedItem = 0;
    List<OptionList> swatchesList;
    private SwatchListener listener;

    public SwatchesAdapter(List<OptionList> swatchesList) {
        this.swatchesList = swatchesList;
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
    public SwatchesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.single_swathes_layout, parent, false);

        // Return a new holder instance
        SwatchesAdapter.ViewHolder viewHolder = new SwatchesAdapter.ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SwatchesAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Get the data model based on position
        // Set item views based on your views and data model
        holder.itemView.setSelected(focusedItem == position);


        TextView swatchName = holder.swatchName;
        ConstraintLayout swatchLayout = holder.swatchLayout;

        swatchName.setText(Html.fromHtml(getFirstLetterIdentity(swatchesList.get(position).getValue())));

        if (focusedItem == position) {
            swatchName.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            swatchName.setTextColor(Color.parseColor("#000000"));
        }

        swatchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.swatchSelected(swatchesList.get(position).getName(), swatchesList.get(position).getValue(), position);
                    // Redraw the old selection and the new
                    notifyItemChanged(focusedItem);
                    focusedItem = position;
                    notifyItemChanged(focusedItem);
                }
            }
        });


    }

    // Returns the total count of items eg the list
    @Override
    public int getItemCount() {
        return swatchesList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView swatchName;
        public ConstraintLayout swatchLayout;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView eg a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            swatchName = (TextView) itemView.findViewById(R.id.swatch_name);
            swatchLayout = (ConstraintLayout) itemView.findViewById(R.id.swatch_layout);

        }
    }

    public String getFirstLetterIdentity(String name) {
        switch (name.toLowerCase()) {
            case "small":
                return "S";
            case "medium":
                return "M";
            case "large":
                return "L";
            case "extra large":
                return "XL";
            default:
                return name;
        }
    }


    public void setOnWatchSelected(SwatchListener listener) {
        this.listener = listener;
    }

    public interface SwatchListener {
        public void swatchSelected(String name, String value, int i);
    }


}