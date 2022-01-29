package com.myshirt.eg.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.myshirt.eg.ProductFragment;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.adapter.AttributesList;
import com.myshirt.eg.adapter.AttributesListAdapter;
import com.myshirt.eg.adapter.ProductList;
import com.myshirt.eg.handler.AddToCartWithUpdateCartCount;
import com.myshirt.eg.handler.GetVariationPriceView;
import com.myshirt.eg.handler.PriceFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class AttributeBottomSheet extends BottomSheetDialogFragment implements AttributesListAdapter.AttributeCallback {
    Context context;
    View root;

    TextView cartCounter;
    ProductList productList;
    TextView attributePrice;
    MyListView attributeListView;
    Button addToCartBtn;
    ProgressBar progressBar;
    EditText hiddenVariationIdView;

    String cartProductID = "0";
    JSONArray variations;
    ArrayList<AttributesList> attributesLists;
    ArrayList<SelectedOptions> selectedOptions = new ArrayList<>();

    public AttributeBottomSheet(Context context, TextView cartCounter, ProductList productList) {
        this.context = context;
        this.cartCounter = cartCounter;
        this.productList = productList;
        this.variations = productList.getVariations();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = View.inflate(getContext(), R.layout.attribute_bottom_sheet_dialog, null);
        //Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).show();

        attributeListView = (MyListView) root.findViewById(R.id.attributesListView);
        attributePrice = (TextView) root.findViewById(R.id.attribute_price);
        addToCartBtn = (Button) root.findViewById(R.id.add_to_cart_btn);
        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);
        hiddenVariationIdView = (EditText) root.findViewById(R.id.hidden_variation_id);

        attributesLists = new ArrayList<>();

        JSONArray attributes = productList.getAttributes();

        try {
            for (int i = 0; i < attributes.length(); i++) {
                JSONObject attribute = attributes.getJSONObject(i);
                AttributesList list = new AttributesList();
                list.setName(attribute.getString("name"));
                list.setOptions(attribute.getJSONArray("options"));
                list.setIs_variation();
                list.setIs_taxonomy();
                list.setValue();
                list.setLabel(attribute.getString("label"));
                attributesLists.add(list);
            }

            AttributesListAdapter attributesListAdapter = new AttributesListAdapter(requireContext(), attributesLists, addToCartBtn, progressBar, attributePrice);
            attributesListAdapter.setCallback(this);
            attributeListView.setAdapter(attributesListAdapter);
            attributeListView.setVisibility(View.VISIBLE);


        } catch (JSONException e) {
            e.printStackTrace();
        }


        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartProductID.equals("0") || cartProductID.isEmpty()) return;

                try {
                    new AddToCartWithUpdateCartCount(context, cartProductID, "1", false, cartCounter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dismiss();
            }
        });


        return  root;
    }
    public void handleAddToCartOfVariation() {
        StringBuilder params = new StringBuilder();
        boolean noneIsEmpty = true;
        for (SelectedOptions option : selectedOptions) {
            params.append(option.getAttrName()).append("=").append(option.getOptionName()).append("&");
            if (option.getAttrName().isEmpty() || option.getOptionName().isEmpty())
                noneIsEmpty = false;
        }
//        Log.e("params: ", params.toString());
        if (noneIsEmpty) {
            new GetVariationPriceView(requireContext(), productList.getId(), params.toString(), attributePrice, progressBar, addToCartBtn, hiddenVariationIdView);
        }

    }
    public boolean attrNameExists(String attr) {
        boolean optionExists = false;
        for (SelectedOptions option : selectedOptions) {
            if (option.getAttrName().equals(attr)) {
                optionExists = true;
                break;
            }
        }
        return  optionExists;
    }
    public int getAttrNamePosition(String attr) {
        int position = -1;
        for (int i = 0; i < selectedOptions.size(); i++) {
            if (selectedOptions.get(i).getAttrName().equals(attr)) {
                position = i;
                break;  // uncomment to get the first instance
            }
        }
        return position;
    }

    @Override
    public void addToOptions(String attr_name, String option_name) {
        if (attrNameExists(attr_name)) {
            //update key name
            selectedOptions.get(getAttrNamePosition(attr_name)).setOptionName(option_name);
        } else {
            //add new key name
            selectedOptions.add(new SelectedOptions(attr_name, option_name));
        }

        handleAddToCartOfVariation();
        getMatchedAttributesOfVariation();
    }

    @SuppressLint("SetTextI18n")
    public void getMatchedAttributesOfVariation() {
        boolean noneIsEmpty = true;
        //first - let's convert our selected options (both key & value) to array list, to be able to compare
        ArrayList<String> selectedAttributes = new ArrayList<>();
        for (SelectedOptions option : selectedOptions) {
            selectedAttributes.add(option.getAttrName().toLowerCase());
            selectedAttributes.add(option.getOptionName().toLowerCase());
            if (option.getAttrName().isEmpty() || option.getOptionName().isEmpty())
                noneIsEmpty = false;
        }

        if (noneIsEmpty) {


            try {
                //for each product variations, compare our selected attributes list
                boolean matchFound = false;
                for (int i = 0; i < variations.length(); i++) {
                    JSONObject variation = variations.getJSONObject(i);
                    ArrayList<String> attributesLists = new ArrayList<>();
                    JSONArray attributes = variation.getJSONArray("attributes");
                    //let convert the attributes (both key & value) to array list, to be able to compare with selected attributes
                    for (int j = 0; j < attributes.length(); j++) {
                        JSONObject attribute = attributes.getJSONObject(j);
                        Iterator keys = attribute.keys();
                        while (keys.hasNext()) {
                            String currentKey = (String) keys.next();
                            attributesLists.add(currentKey.toLowerCase());
                            attributesLists.add(attribute.getString(currentKey).toLowerCase());
                        }
                    }

                    //now let's compare if selected attributes is equal current variation loop
                    if (attributesLists.containsAll(selectedAttributes)) {
                        matchFound = true;
                        attributePrice.setVisibility(View.VISIBLE);
                        attributePrice.setText(Site.CURRENCY + PriceFormatter.format(variation.getString("price")));
                        hiddenVariationIdView.setText(variation.getString("ID"));
                        cartProductID = variation.getString("ID");
                        progressBar.setVisibility(View.GONE);
                        addToCartBtn.setEnabled(true);
                    }
                }

                if (!matchFound) {
                    attributePrice.setVisibility(View.GONE);
                    addToCartBtn.setEnabled(false);
                    hiddenVariationIdView.setText("0");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } //end of noneIsEmpty


    }

    private static class SelectedOptions {
        String attr_name;
        String option_name;

        public SelectedOptions(String attr_name, String option_name) {
            this.attr_name = attr_name;
            this.option_name = option_name;
        }

        public String getAttrName() {
            return attr_name;
        }

//        public void setName(String name) {
//            this.name = name;
//        }

        public String getOptionName() {
            return option_name;
        }

        public void setOptionName(String option_name) {
            this.option_name = option_name;
        }
    }

}
