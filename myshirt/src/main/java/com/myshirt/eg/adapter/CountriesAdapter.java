package com.myshirt.eg.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.myshirt.eg.R;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CountriesAdapter extends ArrayAdapter<CountryLists> {

    private List<CountryLists> dataList;
    // --Commented out by Inspection (6/28/2021 5:38 PM):Context mContext;
    int itemLayout;

    ListFilter listFilter = new ListFilter();
    List<CountryLists> dataListAllItems;



    public CountriesAdapter(Context context, int resource, List<CountryLists> storeDataLst) {
        super(context, resource, storeDataLst);
        dataList = storeDataLst;
//        mContext = context;
        itemLayout = resource;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public CountryLists getItem(int position) {
//        Log.d("CustomListAdapter",
//                dataList.get(position));
        return dataList.get(position);
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        if (view == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(itemLayout, parent, false);
        }

        TextView strName = (TextView) view.findViewById(R.id.lbl_name);
        strName.setText(Html.fromHtml(getItem(position).getName()));
        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    public class ListFilter extends Filter {
        Object lock = new Object();

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String str = ((CountryLists) resultValue).getName();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            if (dataListAllItems == null) {
                synchronized (lock) {
                    dataListAllItems = new ArrayList<CountryLists>(dataList);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    results.values = dataListAllItems;
                    results.count = dataListAllItems.size();
                }
            } else {
                final String searchStrLowerCase = prefix.toString().toLowerCase();

                ArrayList<CountryLists> matchValues = new ArrayList<CountryLists>();

                for (CountryLists dataItem : dataListAllItems) {
                    if (dataItem.getName().toLowerCase().startsWith(searchStrLowerCase)) {
                        matchValues.add(dataItem);
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                dataList = (ArrayList<CountryLists>) results.values;
            } else {
                dataList = null;
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}