package com.fatima.fabric.ui.home;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fatima.fabric.Site;
import com.fatima.fabric.adapter.ProductGridAdapter;
import com.fatima.fabric.adapter.ProductList;
import com.fatima.fabric.handler.The_Slide_Items_Model_Class;
import com.fatima.fabric.handler.The_Slide_items_Pager_Adapter;
import com.fatima.fabric.handler.UpdateCartCount;
import com.fatima.fabric.handler.UserSession;
import com.fatima.fabric.ui.MyGridView;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.fatima.fabric.ArchiveActivity;
import com.fatima.fabric.BrowserActivity;
import com.fatima.fabric.R;
import com.google.android.material.tabs.TabLayout;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private List<The_Slide_Items_Model_Class> listItems;
    private ViewPager page;
    private TabLayout tabLayout;
    TextView cartCounter;
    UserSession userSession;
    Dialog loadingDialog;

    MyGridView bestSellingGrid;
    MyGridView newestGrid;
    MyGridView menGrid;
    MyGridView womenGrid;
    MyGridView foodGrid;
    ArrayList<ProductList> bestSellingLists;
    ArrayList<ProductList> newestLists;
    ArrayList<ProductList> menLists;
    ArrayList<ProductList> womenLists;
    ArrayList<ProductList> foodLists;
    ProductGridAdapter bestSellingAdapter;
    ProductGridAdapter newestAdapter;
    ProductGridAdapter menAdapter;
    ProductGridAdapter womenAdapter;
    ProductGridAdapter foodAdapter;
    Button bestSellingRefresh;
    Button newestRefresh;
    Button menRefresh;
    Button womenRefresh;
    Button foodRefresh;
    ProgressBar bestSellingProgress;
    ProgressBar newestProgress;
    ProgressBar menProgress;
    ProgressBar womenProgress;
    ProgressBar foodProgress;

    //because of cartCounter
    public void initOnCreateViews() {
        loadingDialog = new Dialog(getActivity(), android.R.style.Theme_Light);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        bestSellingProgress = (ProgressBar) requireActivity().findViewById(R.id.bestSellingProgress);
        newestProgress = (ProgressBar) requireActivity().findViewById(R.id.newestProgress);
        menProgress = (ProgressBar) requireActivity().findViewById(R.id.menProgress);
        womenProgress = (ProgressBar) requireActivity().findViewById(R.id.womenProgress);
        foodProgress = (ProgressBar) requireActivity().findViewById(R.id.foodProgress);

        bestSellingRefresh = (Button) requireActivity().findViewById(R.id.bestSellingRefresh);
        newestRefresh = (Button) requireActivity().findViewById(R.id.newestRefresh);
        menRefresh = (Button) requireActivity().findViewById(R.id.menRefresh);
        womenRefresh = (Button) requireActivity().findViewById(R.id.womenRefresh);
        foodRefresh = (Button) requireActivity().findViewById(R.id.foodRefresh);
        bestSellingRefresh.setVisibility(View.GONE);
        newestRefresh.setVisibility(View.GONE);
        menRefresh.setVisibility(View.GONE);
        womenRefresh.setVisibility(View.GONE);
        foodRefresh.setVisibility(View.GONE);

        bestSellingGrid = (MyGridView) requireActivity().findViewById(R.id.bestSellingGrid);
        newestGrid = (MyGridView) requireActivity().findViewById(R.id.newestGrid);
        menGrid = (MyGridView) requireActivity().findViewById(R.id.menGrid);
        womenGrid = (MyGridView) requireActivity().findViewById(R.id.womenGrid);
        foodGrid = (MyGridView) requireActivity().findViewById(R.id.foodGrid);

        bestSellingLists = new ArrayList<>();
        newestLists = new ArrayList<>();
        menLists = new ArrayList<>();
        womenLists = new ArrayList<>();
        foodLists = new ArrayList<>();
        bestSellingAdapter = new ProductGridAdapter(requireActivity(), bestSellingLists, cartCounter);
        newestAdapter = new ProductGridAdapter(requireActivity(), newestLists, cartCounter);
        menAdapter = new ProductGridAdapter(requireActivity(), menLists, cartCounter);
        womenAdapter = new ProductGridAdapter(requireActivity(), womenLists, cartCounter);
        foodAdapter = new ProductGridAdapter(requireActivity(), foodLists, cartCounter);
        bestSellingGrid.setAdapter(bestSellingAdapter);
        newestGrid.setAdapter(newestAdapter);
        menGrid.setAdapter(menAdapter);
        womenGrid.setAdapter(womenAdapter);
        foodGrid.setAdapter(foodAdapter);

        fetchProduct(bestSellingGrid, bestSellingAdapter, bestSellingLists, loadingDialog, bestSellingProgress, bestSellingRefresh, "best_selling");
        fetchProduct(newestGrid, newestAdapter, newestLists, loadingDialog, newestProgress, newestRefresh, "newest");
        fetchProduct(menGrid, menAdapter, menLists, loadingDialog, menProgress, menRefresh, "mens-fashion");
        fetchProduct(womenGrid, womenAdapter, womenLists, loadingDialog, womenProgress, womenRefresh, "womens-fashion");
        fetchProduct(foodGrid, foodAdapter, foodLists, loadingDialog, foodProgress, foodRefresh, "fastfood");

        bestSellingRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchProduct(bestSellingGrid, bestSellingAdapter, bestSellingLists, loadingDialog, bestSellingProgress, bestSellingRefresh, "best_selling");
            }
        });
        newestRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchProduct(newestGrid, newestAdapter, newestLists, loadingDialog, newestProgress, newestRefresh, "newest");
            }
        });
        menRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchProduct(menGrid, menAdapter, menLists, loadingDialog, menProgress, menRefresh, "mens-fashion");
            }
        });
        womenRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchProduct(womenGrid, womenAdapter, womenLists, loadingDialog, womenProgress, womenRefresh, "womens-fashion");
            }
        });
        foodRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchProduct(foodGrid, foodAdapter, foodLists, loadingDialog, foodProgress, foodRefresh, "fastfood");
            }
        });
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.cartMenuIcon);
        item.setActionView(R.layout.cart_icon_update_layout);
        RelativeLayout cartCount = (RelativeLayout)   item.getActionView();
        cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
        cartCounter.setText("0");
        cartCounter.setVisibility(View.GONE);
        initOnCreateViews();
        userSession = new UserSession(requireActivity().getApplicationContext());
        new UpdateCartCount(getActivity(), userSession.userID, cartCounter);
        cartCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(item);
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.cartMenuIcon:{
                BottomNavigationView bottomNav = (BottomNavigationView) requireActivity().findViewById(R.id.nav_view);
                bottomNav.setSelectedItemId(R.id.cartBottomBtn);
            }
            break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        page = root.findViewById(R.id.my_pager) ;
        tabLayout = root.findViewById(R.id.my_tablayout);
        listItems = new ArrayList<>() ;
        listItems.add(new The_Slide_Items_Model_Class(R.drawable.slider_1_premium,"premium"));
        listItems.add(new The_Slide_Items_Model_Class(R.drawable.discount,"discount"));
        listItems.add(new The_Slide_Items_Model_Class(R.drawable.standard,"standard"));
        The_Slide_items_Pager_Adapter itemsPager_adapter = new The_Slide_items_Pager_Adapter(getActivity(), listItems);
        page.setAdapter(itemsPager_adapter);
        tabLayout.setupWithViewPager(page,true);


        new CountDownTimer(60000 * 30, 5000) {
            @Override
            public void onTick(long l) {
                if (page.getCurrentItem() < listItems.size()-1) {
                    page.setCurrentItem(page.getCurrentItem()+1);
                }
                else
                    page.setCurrentItem(0);
            }

            @Override
            public void onFinish() {

            }
        }.start();
        itemsPager_adapter.setOnShareClickedListener(new The_Slide_items_Pager_Adapter.OnShareClickedListener() {
            @Override
            public void ShareClicked(int position) {
//                Toast.makeText(getActivity(), "Selected slide is " + String.valueOf(position), Toast.LENGTH_LONG).show();
                BottomNavigationView bottomNav = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
                bottomNav.setSelectedItemId(R.id.shopBottomBtn);
            }
        });
        ImageView shopNowImage = (ImageView) root.findViewById(R.id.shopNowImage);
        shopNowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomNavigationView bottomNav = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
                bottomNav.setSelectedItemId(R.id.shopBottomBtn);
            }
        });

        return root;
    }

    public void fetchProduct(MyGridView gridView, ProductGridAdapter adapter, ArrayList<ProductList> theList, Dialog loader, ProgressBar progressBar, Button refreshBtn, String category) {
        if (category.equals("best_selling")) { // for first load
            loader.show();
        } else { //for others load
            progressBar.setVisibility(View.VISIBLE);
        }
        String url = Site.PRODUCTS + "?cat=" + category + "&per_page=4";
        if (category.equals("best_selling")) {
            url = Site.PRODUCTS + "?meta_key=total_sales&orderby=meta_value_num&per_page=4";
        } else if (category.equals("newest")) {
            url = Site.PRODUCTS + "?orderby=date&order=DESC&per_page=4";
        }
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONData(response, adapter, theList, loader, progressBar, refreshBtn);
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(getContext(), "Connection error. Please check your connection.", Toast.LENGTH_LONG).show();
            loader.hide();
            progressBar.setVisibility(View.GONE);
            refreshBtn.setVisibility(View.VISIBLE);
        });
        RequestQueue rQueue = Volley.newRequestQueue(requireActivity());
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }

    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json, ProductGridAdapter adapter, ArrayList<ProductList> theList, Dialog loader, ProgressBar progressBar, Button refreshBtn) {
        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("results");


            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String id = jsonObject.getString("ID");
                String name = jsonObject.getString("name");
                String title = jsonObject.getString("title");
                String image = jsonObject.getString("image");
                String price = jsonObject.getString("price");
                String product_type = jsonObject.getString("product_type");
                String regular_price = jsonObject.getString("regular_price");
                String type = jsonObject.getString("type");
                String description = jsonObject.getString("description");
//
                ProductList list = new ProductList();
                list.setId(id);
                list.setTitle(title);
                list.setName(name);
                list.setImage(image);
                list.setPrice(price);
                list.setRegular_price(regular_price);
                list.setType(type);
                list.setProduct_type(product_type);
                list.setDescription(description);
                theList.add(list);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //when fetching is ready
        adapter.notifyDataSetChanged();
        loader.hide();
        progressBar.setVisibility(View.GONE);
        refreshBtn.setVisibility(View.GONE);
    }


}