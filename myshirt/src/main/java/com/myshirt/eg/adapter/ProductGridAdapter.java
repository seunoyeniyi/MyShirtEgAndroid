package com.myshirt.eg.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myshirt.eg.MainActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.handler.AddToCartWithUpdateCartCount;
import com.myshirt.eg.handler.PriceFormatter;
import com.bumptech.glide.Glide;
import com.myshirt.eg.handler.UserSession;
import com.myshirt.eg.ui.AttributeBottomSheet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProductGridAdapter extends BaseAdapter {

    Context context;
    ArrayList<ProductList> productLists;
    Activity activity;
    LayoutInflater layoutInflater;
    UserSession userSession;
    TextView cartCounter;
    int cardResource = R.layout.single_product_card;

    AttributeBottomSheet attributeBottomSheet;

    public ProductGridAdapter(Context context, ArrayList<ProductList> productLists, TextView cartCounter) {
        this.context = context;
        this.productLists = productLists;
        this.activity = (Activity) context;
        this.cartCounter = cartCounter;
        userSession = new UserSession(context.getApplicationContext());
    }
    public ProductGridAdapter(Context context, ArrayList<ProductList> productLists, TextView cartCounter, int cardResource) {
        this.context = context;
        this.productLists = productLists;
        this.activity = (Activity) context;
        this.cartCounter = cartCounter;
        this.cardResource = cardResource;
        userSession = new UserSession(context.getApplicationContext());
    }

    @Override
    public int getCount() {
        return productLists.size();
    }

    @Override
    public Object getItem(int i) {
        return productLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n", "DefaultLocale", "ResourceAsColor"})
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;

        rowView = layoutInflater.inflate(cardResource, null);
        TextView titleView =(TextView) rowView.findViewById(R.id.titleView);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageview);
        TextView priceView = (TextView) rowView.findViewById(R.id.priceView);
        TextView regularPriceView = (TextView) rowView.findViewById(R.id.regularPriceView);
//        ProgressBar addToCartProgressBar = (ProgressBar) rowView.findViewById(R.id.addToCartProgress);
        ImageView wishlistToggle = (ImageView) rowView.findViewById(R.id.wishListToggle);
        TextView catText = (TextView) rowView.findViewById(R.id.catText);

        titleView.setText(/*productLists.get(position).getId() + */ Html.fromHtml(productLists.get(position).getName()));
        regularPriceView.setPaintFlags(regularPriceView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        Glide
                .with(context)
                .load(productLists.get(position).getImage().replace("localhost", Site.DOMAIN))
                .placeholder(R.drawable.sample_placeholder)
                .into(imageView);

        if (productLists.get(position).getIn_wish_list().equals("true")) {
            wishlistToggle.setImageResource(R.drawable.icons8_love_2);
//            wishlistToggle.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        } else {
            wishlistToggle.setImageResource(R.drawable.icons8_heart_9);
//            wishlistToggle.setBackgroundResource(R.color.white);
        }

        //parse json of the categories
        try {
            JSONArray categoriesArray = new JSONArray(productLists.get(position).getCategories());
            if (categoriesArray.length() > 0) {
                JSONObject catObject = categoriesArray.getJSONObject(0);
                catText.setText(Html.fromHtml(catObject.getString("name")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button addToCartBtn = (Button) rowView.findViewById(R.id.add_to_cart_btn);
        TextView outOfStockText = (TextView) rowView.findViewById(R.id.out_of_stock);
        TextView saveDiscountText = (TextView) rowView.findViewById(R.id.save_discount);





        if (productLists.get(position).getProduct_type().equals("variable") || productLists.get(position).getType().equals("variable")) {
            if (!productLists.get(position).getLowest_price().isEmpty() && !productLists.get(position).getLowest_price().equals("false")) {
                priceView.setText("From " + Site.CURRENCY + PriceFormatter.format(productLists.get(position).getLowest_price())); //variable product
            } else {
                priceView.setText("From " + Site.CURRENCY + PriceFormatter.format(productLists.get(position).getPrice())); //variable product
            }
            regularPriceView.setVisibility(View.GONE);

        } else { // FOR SIMPLE PRODUCT
            priceView.setText(Site.CURRENCY + PriceFormatter.format(productLists.get(position).getPrice())); //simple product
            if (!productLists.get(position).getPrice().equals(productLists.get(position).getRegular_price())) {
                regularPriceView.setText(Site.CURRENCY + productLists.get(position).getRegular_price());
                //save discount
                Double diff = Double.parseDouble(productLists.get(position).getRegular_price()) - Double.parseDouble(productLists.get(position).getPrice());
                saveDiscountText.setText("Save " + Site.CURRENCY + PriceFormatter.format(diff));
//                saveDiscountText.setVisibility(View.VISIBLE);
            } else {
                regularPriceView.setVisibility(View.GONE);
            }

        }

        //stock status
        if (productLists.get(position).getStock_status().equals("outofstock")) {
            addToCartBtn.setVisibility(View.INVISIBLE);
//            outOfStockText.setVisibility(View.VISIBLE);
            priceView.setText("Out of Stock");
            priceView.setGravity(Gravity.CENTER);
        }


        LinearLayout contentLayout = (LinearLayout) rowView.findViewById(R.id.contentLayout); //layer to click for new activity
//        LinearLayout cartLayout = (LinearLayout) rowView.findViewById(R.id.cartLayoutBtn); //layout for cart btn
//        TextView cartTextView = (TextView) rowView.findViewById(R.id.cartTextView);
//        addToCartProgressBar.setVisibility(View.GONE)



        if (productLists.get(position).getProduct_type().equals("variable") || productLists.get(position).getType().equals("variable")) {
            //setup as variable product
            addToCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attributeBottomSheet = new AttributeBottomSheet(context, cartCounter, productLists.get(position));
                    if (!attributeBottomSheet.isAdded()) { //to avoid crashing with double touch
                        attributeBottomSheet.show(((MainActivity) activity).getSupportFragmentManager(), "Select");
                    }

//                    openProductActivity(productLists.get(position).getId(),
//                            productLists.get(position).getImage(),
//                            productLists.get(position).getName(),
//                            productLists.get(position).getDescription(),
//                            productLists.get(position).getIn_wish_list());
                }
            });
        } else {
            //setup as simple product
            addToCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    add to cart
                    try {
                        new AddToCartWithUpdateCartCount(context, productLists.get(position).getId(), "1", false, cartCounter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openProductActivity(productLists.get(position).getId(),
                        productLists.get(position).getImage(),
                        productLists.get(position).getName(),
                        productLists.get(position).getDescription(),
                        productLists.get(position).getIn_wish_list());
            }
        });




        wishlistToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context, userSession.userID.toString(), Toast.LENGTH_SHORT).show();
                if (userSession.logged()) {
                    //toggle the heart
                    if (productLists.get(position).getIn_wish_list().equals("true")) {
                        wishlistToggle.setImageResource(R.drawable.icons8_heart_9);
//                        wishlistToggle.setBackgroundResource(R.color.white);
                        removeFromWishList(productLists.get(position).getId(), userSession.userID, wishlistToggle, position);
                    } else {
                        wishlistToggle.setImageResource(R.drawable.icons8_love_2);
//                        wishlistToggle.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                        addToWishList(productLists.get(position).getId(), userSession.userID, wishlistToggle, position);
                    }
                } else {
                    Toast.makeText(context, "Login first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rowView;
    }
    public void openProductActivity(String productID, String image, String title, String description, String in_wishlist) {
//        Intent intent = new Intent(context, ProductFragment.class);
//        intent.putExtra("product_id", productID);
//        intent.putExtra("product_image", image);
//        intent.putExtra("product_title", title);
//        intent.putExtra("product_description", description);

        //uncomment this if Product page changed to Activity type
//        if (context.getClass().getSimpleName().equals("ProductFragment")) {
//            activity.finish();
//        }
//        activity.startActivity(intent);

        Bundle bundle = new Bundle();
        bundle.putString("product_id", productID);
        bundle.putString("product_image", image);
        bundle.putString("product_title", title);
        bundle.putString("product_description", description);
        bundle.putString("in_wishlist", in_wishlist);
        ((MainActivity) activity).changeFragment(R.id.product_fragment, bundle);
    }
    public void addToWishList(String productID, String userID, ImageView wishListToggle, int position) {
        String url = Site.ADD_TO_WISH_LIST + userID + "/" + productID;

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
//
                        try {
                            JSONObject object = new JSONObject(response.toString());
                            JSONArray array = object.getJSONArray("results");
                            if (array.length() > 0) {
                                wishListToggle.setImageResource(R.drawable.icons8_love_2);
//                                wishListToggle.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                                productLists.get(position).setIn_wish_list("true");
                                userSession.has_wishlist(true);
                            } else {
                                wishListToggle.setImageResource(R.drawable.icons8_heart_9);
//                                wishListToggle.setBackgroundResource(R.color.white);
                                productLists.get(position).setIn_wish_list("false");
                                userSession.has_wishlist(false);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(context, "Error updating wishlist!.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(context);
        postRequest.setShouldCache(false);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }
    public void removeFromWishList(String productID, String userID, ImageView wishListToggle, int position) {
        String url = Site.REMOVE_FROM_WISH_LIST + userID + "/" + productID;

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
                        wishListToggle.setImageResource(R.drawable.icons8_heart_9);
//                        wishListToggle.setBackgroundResource(R.color.white);
                        productLists.get(position).setIn_wish_list("false");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(context, "Error updating wishlist!.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(context);
        postRequest.setShouldCache(false);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }
}
