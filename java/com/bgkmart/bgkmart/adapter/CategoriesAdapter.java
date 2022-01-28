package com.fatima.fabric.adapter;

import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.fatima.fabric.R;

public class CategoriesAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<CategoriesList> categoryParents;
    private HashMap<CategoriesList, List<CategoriesList>> categoriesDetails;

    public CategoriesAdapter(Context context, List<CategoriesList> categoryParents,
                             HashMap<CategoriesList, List<CategoriesList>> categoriesDetails) {
        this.context = context;
        this.categoryParents = categoryParents;
        this.categoriesDetails = categoriesDetails;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.categoriesDetails.get(this.categoryParents.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final CategoriesList expandedListText = (CategoriesList) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }
        TextView expandedListTextView = (TextView) convertView
                .findViewById(R.id.expandedListItem);
        expandedListTextView.setText(expandedListText.getName() + "(" + expandedListText.getCount() + ")");
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.categoriesDetails.get(this.categoryParents.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.categoryParents.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.categoryParents.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    public View getGroupView(int listPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        CategoriesList list = (CategoriesList) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.listTitle);
        ImageView groupIcon = (ImageView) convertView.findViewById(R.id.groupIcon);
        ImageView groupChevron = (ImageView) convertView.findViewById(R.id.groupChevron);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(list.getName() + "(" + list.getCount() + ")");

        //remove chevron if category has no sub_cats
        if (list.getSub_cats().equals("0")) {
            groupChevron.setVisibility(View.INVISIBLE);
        } else {
            groupChevron.setVisibility(View.VISIBLE);
        }
        //handle chevron based on expansion state
        if (isExpanded) {
            groupChevron.setImageResource(R.drawable.button_up);
        } else {
            groupChevron.setImageResource(R.drawable.icons8_down_button);
        }
        //change group icon based on category name
        switch (list.getSlug()) {
            case "women":
            case "womens-fashion":
                groupIcon.setImageResource(R.drawable.women_dress);
                break;
            case "accessories":
                groupIcon.setImageResource(R.drawable.vintage_glasses);
                break;
            case "grocery":
                groupIcon.setImageResource(R.drawable.icons8_flour_3);
                break;
            case "lifestyle":
                groupIcon.setImageResource(R.drawable.icons8_dumbbell);
                break;
            case "kids":
                groupIcon.setImageResource(R.drawable.icons8_children);
                break;
            case "fashion":
            case "clothes":
                groupIcon.setImageResource(R.drawable.icons8_t_shirt);
                break;
            case "electronics":
                groupIcon.setImageResource(R.drawable.icons8_electronics);
                break;
            case "fastfood":
                groupIcon.setImageResource(R.drawable.icons8_bread_and_rolling_pin);
                break;
            case "mens-fashion":
                groupIcon.setImageResource(R.drawable.icons8_man_in_a_tuxedo);
                break;
            default:
                groupIcon.setImageResource(R.drawable.icons8_filled_circle);
                break;
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}