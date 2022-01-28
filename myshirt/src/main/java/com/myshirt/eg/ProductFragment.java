package com.myshirt.eg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
import com.myshirt.eg.adapter.AttributesList;
import com.myshirt.eg.adapter.AttributesListAdapter;
import com.myshirt.eg.adapter.CommentList;
import com.myshirt.eg.adapter.CommentRecyclerAdapter;
import com.myshirt.eg.adapter.GalleryRecyclerAdapter;
import com.myshirt.eg.handler.AddToCartSingle;
import com.myshirt.eg.handler.GetVariationPriceView;
import com.myshirt.eg.handler.PicassoImageGetter;
import com.myshirt.eg.handler.PriceFormatter;
import com.myshirt.eg.handler.UpdateCartCount;
import com.myshirt.eg.handler.UserSession;
import com.myshirt.eg.ui.MyGridView;
import com.myshirt.eg.ui.MyListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import me.himanshusoni.quantityview.QuantityView;

public class ProductFragment extends Fragment implements AttributesListAdapter.AttributeCallback {

    View root;
//    Dialog loadingDialog;
    MyGridView productGridView;
    ProgressBar priceAndCartProgressBar;
    ProgressBar relatedProgressBar;
    LinearLayout priceCartLayout;
    UserSession userSession;
    String parent_id, product_id, product_title, product_image, product_description;
//    ArrayList<ProductList> productLists;
    TextView cartCounter;
    Button addToCartBtn;
    Button refreshBtn;
    MyListView attributesListView;
    TextView variationPriceView;
    ProgressBar priceVariationProgressBar;
    EditText hiddenVariationIdView;
    ArrayList<AttributesList> attributesLists;
    ArrayList<SelectedOptions> selectedOptions = new ArrayList<>();
    JSONArray variations = new JSONArray();
    TextView productCatText;
    Button buyWhatsApp;
    TextView outOfStockText;
    LinearLayout addToCartLayout;
    LinearLayout totalRatingLayout;
    RatingBar totalRatingBar;
    TextView totalCustomerReviews;
    LinearLayout reviewLayout;
    TextView no_rating_text_view;
    RatingBar ratingBar;
    EditText ratingComment;
    Button ratingSubmit;
    RecyclerView ratingRecycler;
    CommentRecyclerAdapter commentRecyclerAdapter;
    ArrayList<CommentList> commentLists = new ArrayList<>();
    TabLayout tabLayout;

    List<String> galleryRecyclerLists;
    RecyclerView galleryRecycler;
    GalleryRecyclerAdapter galleryAdapter;

    TextView descriptionView;

    Context context;

    RelativeLayout cartBtn;

    ImageView wishlistToggle;
    boolean in_wishlist = false;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_product, container, false);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        userSession = new UserSession(requireActivity().getApplicationContext());



        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        ((MainActivity) requireActivity()).appBarType("title", "");


        product_id = getArguments().getString("product_id");
        parent_id = product_id; //first time
        product_title  = getArguments().getString("product_title");
        product_image  = getArguments().getString("product_image");
        product_description  = getArguments().getString("product_description");

//        Log.e("Parent", parent_id);

        ImageButton backBtn = (ImageButton) root.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) requireActivity()).onBackPressed();
            }
        });

        progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");

        tabLayout = (TabLayout) root.findViewById(R.id.tab_layout);

        totalRatingLayout = (LinearLayout) root.findViewById(R.id.total_rating_layout);
        totalRatingBar = (RatingBar) root.findViewById(R.id.totalRatingBar);
        totalCustomerReviews = (TextView) root.findViewById(R.id.total_customer_rating);
        reviewLayout = (LinearLayout) root.findViewById(R.id.reviews_layout);
        ratingRecycler = (RecyclerView) root.findViewById(R.id.rating_recycler);
        no_rating_text_view = (TextView) root.findViewById(R.id.no_rating);
        ratingBar = (RatingBar) root.findViewById(R.id.ratingBar);
        ratingComment = (EditText) root.findViewById(R.id.rating_comment);
        ratingSubmit = (Button) root.findViewById(R.id.rating_submit);
        ratingBar.setNumStars(5);

        ratingSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = ratingComment.getText().toString();
                String rating = String.valueOf(Math.round(ratingBar.getRating()));



                if (!userSession.logged()) {
                    Toast.makeText(requireContext(), "Please login to add review.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (comment.length() < 1) {
                    Toast.makeText(requireContext(), "Please write eg comment.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (ratingBar.getRating() < 1) {
                    Toast.makeText(requireContext(), "Please tap any star to rate us.", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    addReview(parent_id, userSession.userID, rating, comment);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


        commentLists = new ArrayList<>();
        commentRecyclerAdapter = new CommentRecyclerAdapter(requireActivity(), commentLists);
        ratingRecycler.setAdapter(commentRecyclerAdapter);
        ratingRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        cartCounter = (TextView) root.findViewById(R.id.actionbar_notifcation_textview);
        //we are changing the cart counter to the one eg our layout
        cartCounter.setText("0");
        cartCounter.setVisibility(View.GONE);
        new UpdateCartCount(requireContext(), userSession.userID, cartCounter);

        cartBtn = (RelativeLayout) root.findViewById(R.id.cart_btn);
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) requireActivity()).changeFragment(R.id.navigation_cart);
            }
        });


        galleryRecyclerLists = new ArrayList<>();
        galleryRecyclerLists.add(getArguments().getString("product_image")); //default
        galleryRecycler = (RecyclerView) root.findViewById(R.id.galleryRecycler);
        galleryAdapter = new GalleryRecyclerAdapter(getActivity(), galleryRecyclerLists);
        galleryRecycler.setAdapter(galleryAdapter);
        galleryRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));


        wishlistToggle = (ImageView) root.findViewById(R.id.wishlist_btn);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("in_wishlist")) {
                in_wishlist = bundle.getString("in_wishlist").equals("true");
            }
        }

        if (in_wishlist) {
            wishlistToggle.setImageResource(R.drawable.icons8_love_2);
        } else {
            wishlistToggle.setImageResource(R.drawable.icons8_heart_9);
        }

        buyWhatsApp = (Button) root.findViewById(R.id.buy_whatsapp);
        outOfStockText = (TextView) root.findViewById(R.id.out_of_stock);
        addToCartLayout = (LinearLayout) root.findViewById(R.id.add_to_cart_layout);
        buyWhatsApp.setVisibility(View.GONE);
        outOfStockText.setVisibility(View.GONE);


        context = requireContext();
        productCatText = (TextView) root.findViewById(R.id.productCatText);
        productGridView = (MyGridView) root.findViewById(R.id.productGridView);
        priceAndCartProgressBar = (ProgressBar) root.findViewById(R.id.priceAndCartProgress);
//        relatedProgressBar = (ProgressBar) root.findViewById(R.id.relatedProgressBar);
        priceCartLayout = (LinearLayout) root.findViewById(R.id.priceCartLayout);
        addToCartBtn = (Button) root.findViewById(R.id.addToCartBtn);
        refreshBtn = (Button) root.findViewById(R.id.refreshDetailBtn);
        attributesListView = (MyListView) root.findViewById(R.id.attributesListView);
        variationPriceView = (TextView) root.findViewById(R.id.variationPriceView);
        priceVariationProgressBar = (ProgressBar) root.findViewById(R.id.priceVariationProgressBar);
        hiddenVariationIdView = (EditText) root.findViewById(R.id.hiddenVariationIdEditView);

        hiddenVariationIdView.setText("0"); //default value
        priceVariationProgressBar.setVisibility(View.GONE);
        priceCartLayout.setVisibility(View.GONE);





        wishlistToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { ;
                if (userSession.logged()) {
                    //toggle the heart
                    if (in_wishlist) {
                        wishlistToggle.setImageResource(R.drawable.icons8_heart_9);
                        removeFromWishList(parent_id, userSession.userID, wishlistToggle);
                    } else {
                        wishlistToggle.setImageResource(R.drawable.icons8_love_2);
                        addToWishList(parent_id, userSession.userID, wishlistToggle);
                    }
                } else {
                    Toast.makeText(context, "Login first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buyWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://api.whatsapp.com/send?phone=111111111&text=Hi, I want to buy *" + product_title + "* ID:" + product_id  + ".";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

//        productCatText.setVisibility(View.GONE);

        TextView titleView = (TextView) root.findViewById(R.id.detailTitleView);
        descriptionView = (TextView) root.findViewById(R.id.detailDescriptionView);

        titleView.setText(product_title);

        if (product_description.isEmpty()) {
            descriptionView.setVisibility(View.GONE);
        } else {
            descriptionView.setVisibility(View.VISIBLE);
            PicassoImageGetter imageGetter = new PicassoImageGetter(requireContext(), descriptionView);
            Spannable html;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                html = (Spannable) Html.fromHtml(product_description, Html.FROM_HTML_MODE_LEGACY, imageGetter, null);
//            descriptionView.setText(Html.fromHtml(product_description, Html.FROM_HTML_MODE_COMPACT));
            } else {
                html = (Spannable) Html.fromHtml(product_description, imageGetter, null);
//            descriptionView.setText(Html.fromHtml(product_description));
            }
            descriptionView.setText(html);
        }


        descriptionView.setVisibility(View.GONE);
        priceCartLayout.setVisibility(View.GONE);
        reviewLayout.setVisibility(View.GONE);
        totalRatingLayout.setVisibility(View.GONE);



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    descriptionView.setVisibility(View.GONE);
                    priceCartLayout.setVisibility(View.VISIBLE);
                    reviewLayout.setVisibility(View.GONE);
                } else if (position == 1) {
                    descriptionView.setVisibility(View.VISIBLE);
                    priceCartLayout.setVisibility(View.GONE);
                    reviewLayout.setVisibility(View.GONE);
                } else if (position == 2) {
                    descriptionView.setVisibility(View.GONE);
                    priceCartLayout.setVisibility(View.GONE);
                    reviewLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



//        productLists = new ArrayList<>();
        attributesLists = new ArrayList<>();

        fetchDetail(getActivity());

        //to refresh
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                priceAndCartProgressBar.setVisibility(View.VISIBLE);
                fetchDetail(getContext());
            }
        });

    return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        MenuItem item = menu.findItem(R.id.cartMenuIcon);
//        item.setActionView(R.layout.cart_icon_update_layout);
//        RelativeLayout cartCount = (RelativeLayout)   item.getActionView();
//        cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
        //we are changing the cart counter to the one eg our layout -- uncomment above if ready to use
//        cartCounter.setText("0");
//        cartCounter.setVisibility(View.GONE);
//        new UpdateCartCount(requireContext(), userSession.userID, cartCounter);
//        cartCount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onOptionsItemSelected(item);
//            }
//        });

        MenuItem wishItem = menu.findItem(R.id.nav_menu_wishlist);
        wishItem.setActionView(R.layout.wislist_icon_update_layout);
        RelativeLayout wishLayout = (RelativeLayout) wishItem.getActionView();
        TextView wishNotification = (TextView) wishLayout.findViewById(R.id.actionbar_notifcation_textview);
        if (userSession.has_wishlist()) {
            wishNotification.setVisibility(View.VISIBLE);
        } else {
            wishNotification.setVisibility(View.GONE);
        }
        wishLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(wishItem);
            }
        });

        //hide search menu
        MenuItem search = menu.findItem(R.id.nav_menu_search);
        if (search!=null)
            search.setVisible(false);


        super.onCreateOptionsMenu(menu, inflater);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        switch (item.getItemId()) {
            case R.id.cartMenuIcon:{
                ((MainActivity) requireActivity()).changeFragment(R.id.navigation_cart);
            }
            break;
            case R.id.nav_menu_wishlist: {
                ((MainActivity)getActivity()).changeFragment(R.id.wishlist_fragment);
            }
            break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void  fetchDetail(Context context) {
//        loadingDialog.show();
        String url = Site.PRODUCT + product_id + "?user_id=" + userSession.userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Activity activity = getActivity();
                if(activity == null || !isAdded()){
                    return; //to avoid crash
                }
                parseDetailData(response);
            }
        }, (VolleyError error) -> {
            Activity activity = getActivity();
            if(activity == null || !isAdded()){
                return; //to avoid crash
            }
            //handle error
            Toast.makeText(context, "Unable to get product.", Toast.LENGTH_LONG).show();
//            loadingDialog.hide();
            priceAndCartProgressBar.setVisibility(View.GONE);
            refreshBtn.setVisibility(View.VISIBLE);
        });
        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables", "NotifyDataSetChanged"})
    public void parseDetailData(String json) {
        try {
            JSONObject object = new JSONObject(json);

            TextView priceView = (TextView) root.findViewById(R.id.detailPriceView);
            QuantityView quantityView = (QuantityView) root.findViewById(R.id.quantityView_default);
            Button minusBtn = (Button) root.findViewById(R.id.minusBtn);
            Button plus = (Button) root.findViewById(R.id.plusBtn);

            //galleries
            galleryRecyclerLists.clear();
            galleryRecyclerLists = new ArrayList<>();
            galleryRecyclerLists.add(product_image); //default

            JSONArray galleries = object.getJSONArray("gallery_images");
            for (int i = 0; i < galleries.length(); i++) {
                galleryRecyclerLists.add(galleries.getString(i));
            }

            galleryAdapter = new GalleryRecyclerAdapter(getActivity(), galleryRecyclerLists);
            galleryRecycler.setAdapter(galleryAdapter);
            galleryRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));


            String description = object.getString("description");
            if (description.isEmpty()) {
                descriptionView.setVisibility(View.GONE);
            } else {
                PicassoImageGetter imageGetter = new PicassoImageGetter(requireContext(), descriptionView);
                Spannable html;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    html = (Spannable) Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY, imageGetter, null);
//            descriptionView.setText(Html.fromHtml(product_description, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    html = (Spannable) Html.fromHtml(description, imageGetter, null);
//            descriptionView.setText(Html.fromHtml(product_description));
                }
                descriptionView.setText(html);
            }


            //parse json of the categories
            try {
                JSONArray categoriesArray = new JSONArray(object.getString("categories"));
                if (categoriesArray.length() > 0) {
                    JSONObject catObject = categoriesArray.getJSONObject(0);
                    productCatText.setText(Html.fromHtml(catObject.getString("name")));
                    productCatText.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


//            if (object.getString("stock_status").equals("outofstock")) {
//                outOfStockText.setVisibility(View.VISIBLE);
//                buyWhatsApp.setVisibility(View.VISIBLE);
//                addToCartBtn.setEnabled(false);
//                addToCartLayout.setVisibility(View.GONE);
//            }

//            FOR VARIABLE PRODUCT
            if (object.getString("type").equals("variable") || object.getString("product_type").equals("variable")) {
                if (!object.getString("lowest_variation_price").isEmpty()) {
                    priceView.setText("From " + Site.CURRENCY + object.getString("lowest_variation_price"));
                } else {
                    priceView.setText("From " + Site.CURRENCY + object.getString("price"));
                }

//                variations
                variations = object.getJSONArray("variations");
                JSONArray attributes = object.getJSONArray("attributes");


                for (int i = 0; i < attributes.length(); i++) {
                    JSONObject attribute = attributes.getJSONObject(i);
                    AttributesList list = new AttributesList();
//                    list.setId(attribute.getString("id"));
                    list.setName(attribute.getString("name"));
                    list.setOptions(attribute.getJSONArray("options"));
//                    list.setPosition(attribute.getString("position"));
//                    list.setVisible(attribute.getString("visible"));
//                    list.setVariation(attribute.getString("variation"));
                    list.setIs_variation();
                    list.setIs_taxonomy();
                    list.setValue();
                    list.setLabel(attribute.getString("label"));
                    attributesLists.add(list);
                }
                AttributesListAdapter attributesListAdapter = new AttributesListAdapter(requireContext(), attributesLists, addToCartBtn, priceVariationProgressBar, variationPriceView);
                attributesListAdapter.setCallback(this);
                attributesListView.setAdapter(attributesListAdapter);
                attributesListView.setVisibility(View.VISIBLE);
//                addToCartBtn.setEnabled(false);

                addToCartBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            String quantity = String.valueOf(quantityView.getQuantity());
                            if (!hiddenVariationIdView.getText().toString().equals("0")) {
                                new AddToCartSingle(requireActivity(),
                                        hiddenVariationIdView.getText().toString(), quantity, false, cartCounter);
                            } else {
                                Toast.makeText(requireActivity(), "Please select options.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Toast toast = Toast.makeText(ProductFragment.this, "Added to Cart", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.TOP|Gravity.CENTER, 0,0);
//                        toast.show();
                    }
                });



            } else { // FOR SIMPLE PRODUCT
                priceView.setText(Site.CURRENCY + object.getString("price"));
                attributesListView.setVisibility(View.GONE);
                addToCartBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            String quantity = String.valueOf(quantityView.getQuantity());
                            new AddToCartSingle(requireActivity(),
                                    object.getString("ID"), quantity, false, cartCounter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Toast toast = Toast.makeText(ProductFragment.this, "Added to Cart", Toast.LENGTH_LONG);
//                        toast.setGravity(Gravity.TOP|Gravity.CENTER, 0,0);
//                        toast.show();
                    }
                });
            }

            //REVIEWS
            JSONArray comments = object.getJSONArray("comments");

            if (comments.length() > 0) {
                no_rating_text_view.setVisibility(View.GONE);
                totalRatingLayout.setVisibility(View.VISIBLE);
                //calculate total rating review
                float average_rating = Float.parseFloat(object.getString("average_rating"));
                totalRatingBar.setRating(average_rating);
                totalCustomerReviews.setText("(" + object.getString("review_count") + " Customer Reviews)");
            }
            for (int i = 0; i < comments.length(); i++) {
                JSONObject comment = comments.getJSONObject(i);
                commentLists.add(new CommentList(comment.getString("comment_author"), comment.getString("comment_content"), comment.getString("rating"), comment.getString("user_image")));
            }

            commentRecyclerAdapter.notifyDataSetChanged();

            Objects.requireNonNull(tabLayout.getTabAt(2)).setText("REVIEWS(" + commentLists.size() + ")");

//            parseRelatedData(object.getJSONArray("related_products"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        loadingDialog.hide();
        priceAndCartProgressBar.setVisibility(View.GONE);
        priceCartLayout.setVisibility(View.VISIBLE);
        refreshBtn.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
//    public void parseRelatedData(JSONArray array) {
//        try {
//            for (int i = 0; i < array.length(); i++) {
//                JSONObject jsonObject = array.getJSONObject(i);
//                String id = jsonObject.getString("ID");
//                String name = jsonObject.getString("name");
//                String image = jsonObject.getString("image");
//                String price = jsonObject.getString("price");
//                String regular_price = jsonObject.getString("regular_price");
//                String product_type = jsonObject.getString("product_type");
//                String type = jsonObject.getString("type");
//                String description = jsonObject.getString("description");
//                String in_wishlist = jsonObject.getString("in_wishlist");
//                String categories = jsonObject.getString("categories");
////
//                ProductList list = new ProductList();
//                list.setId(id);
//                list.setName(name);
//                list.setImage(image);
//                list.setPrice(price);
//                list.setType(type);
//                list.setRegular_price(regular_price);
//                list.setProduct_type(product_type);
//                list.setDescription(description);
//                list.setIn_wish_list(in_wishlist);
//                list.setCategories(categories);
//                productLists.add(list);
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
////        //when fetching is ready
//        ProductGridAdapter productAdapter = new ProductGridAdapter(context, productLists, cartCounter);
//        productGridView.setAdapter(productAdapter);
//        relatedProgressBar.setVisibility(View.GONE);
//
//
//
//    }


    @Override
    public void onDestroy() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        super.onDestroy();
    }
    @Override
    public void onStop() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        super.onStop();
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//    }

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
            new GetVariationPriceView(requireContext(), product_id, params.toString(), variationPriceView, priceVariationProgressBar, addToCartBtn, hiddenVariationIdView);
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
                        variationPriceView.setVisibility(View.VISIBLE);
                        variationPriceView.setText(Site.CURRENCY + PriceFormatter.format(variation.getString("price")));
                        hiddenVariationIdView.setText(variation.getString("ID"));
                        priceVariationProgressBar.setVisibility(View.GONE);
                        addToCartBtn.setEnabled(true);
                    }
                }

                if (!matchFound) {
                        variationPriceView.setVisibility(View.GONE);
                        addToCartBtn.setEnabled(false);
                        hiddenVariationIdView.setText("0");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } //end of noneIsEmpty


    }



    public void addToWishList(String productID, String userID, ImageView wishListToggle) {
        String url = Site.ADD_TO_WISH_LIST + userID + "/" + parent_id;

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
                                in_wishlist = true;
                                userSession.has_wishlist(true);
                            } else {
                                wishListToggle.setImageResource(R.drawable.icons8_heart_9);
                                in_wishlist = false;
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
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }
    public void removeFromWishList(String productID, String userID, ImageView wishListToggle) {
        String url = Site.REMOVE_FROM_WISH_LIST + userID + "/" + productID;

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
                        wishListToggle.setImageResource(R.drawable.icons8_heart_9);
                        in_wishlist = false;
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
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }


    ProgressDialog progressDialog;

    public void addReview(String product, String user_id, String rating, String comment) throws JSONException {
        progressDialog.setMessage("Adding review...");
        progressDialog.show();

        String url = Site.ADD_REVIEW + product + "/" + user_id;
        JSONObject data = new JSONObject();
        data.put("rating", rating);
        data.put("comment", comment);

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!isAdded()) return;
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(requireContext(), "Review Submitted", Toast.LENGTH_LONG).show();
                                commentLists.add(new CommentList(userSession.username, comment, rating, userSession.get_profile_image()));
                                commentRecyclerAdapter.notifyDataSetChanged();
                                Objects.requireNonNull(tabLayout.getTabAt(2)).setText("REVIEWS(" + commentLists.size() + ")");
                            } else {
                                Toast.makeText(requireContext(), "Unable to add review... Try again.", Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        progressDialog.hide();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Unable to add review... Try again.", Toast.LENGTH_LONG).show();
                progressDialog.hide();
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(requireActivity().getApplicationContext());
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }

}