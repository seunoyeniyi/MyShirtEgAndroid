package com.myshirt.eg.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.myshirt.eg.MainActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.adapter.OrdersRecyclerAdapter;
import com.myshirt.eg.adapter.OrdersRecyclerClass;
import com.myshirt.eg.handler.UserSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {
    View root;
    String order_status;
//    MyListView orderListView;
    RecyclerView ordersRecyclerView;
//    ArrayList<OrderList> orderLists;
    List<OrdersRecyclerClass> ordersRecyclerLists;
//    OrderListAdapter orderListAdapter;
    OrdersRecyclerAdapter ordersAdapter;
    ShimmerFrameLayout progressBar;
    Button refreshBtn;
    UserSession userSession;
    TextView noQuery;
    String activityTitle = "Orders";
    Spinner sortSpinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_orders, container, false);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        userSession = new UserSession(requireActivity().getApplicationContext());


        ((MainActivity) getActivity()).appBarType("title", "Orders");


        Bundle bundle = getArguments();
        assert bundle != null;
        order_status = (bundle.containsKey("order_status")) ? bundle.getString("order_status") : "all";


        noQuery = (TextView) root.findViewById(R.id.noQuery);
        noQuery.setVisibility(View.GONE);
        ordersRecyclerView = (RecyclerView) root.findViewById(R.id.orderRecyclerView);
        progressBar = (ShimmerFrameLayout) root.findViewById(R.id.shimmer_view_container);
//        progressBar.setVisibility(View.VISIBLE);
//        progressBar.startShimmer();
        refreshBtn = (Button) root.findViewById(R.id.refreshOrdersBtn);

        ordersRecyclerLists = new ArrayList<>();
        ordersAdapter = new OrdersRecyclerAdapter(requireActivity(), ordersRecyclerLists);
        ordersRecyclerView.setAdapter(ordersAdapter);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, false));








        sortSpinner = (Spinner) root.findViewById(R.id.sortSpinner);
        List<String> sortOptions = new ArrayList<String>();
        sortOptions.add("All"); //0 -
        sortOptions.add("Processing"); //1 -
        sortOptions.add("Pending Payment"); //2 -
        sortOptions.add("Completed"); //3 -
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_item_shop, sortOptions);
        sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_menu_item);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ordersRecyclerLists.clear();
                switch (i) {
                    case 1:
                        order_status = "processing";
                        break;
                    case 2:
                        order_status = "pending";
                        break;
                    case 3:
                        order_status = "completed";
                        break;
                    case 0:
                    default:
                        order_status = "all";
                        break;
                }
                fetchOrders();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });


        //change
        switch (order_status) {
            case "processing":
                activityTitle = "Processing Orders";
                sortSpinner.setSelection(1);
                break;
            case "pending":
                activityTitle = "Pending Orders";
                sortSpinner.setSelection(2);
                break;
            case "complete":
            case "completed":
                activityTitle = "Completed Orders";
                sortSpinner.setSelection(3);
                break;
            default:
                sortSpinner.setSelection(0);
                break;
        }

        //        getSupportActionBar().setTitle(activityTitle);
        ((MainActivity) getActivity()).appBarType("title", activityTitle);

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                fetchOrders();
            }
        });

        return root;
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem search = menu.findItem(R.id.nav_menu_search);
        MenuItem cart = menu.findItem(R.id.cartMenuIcon);

        if(search!=null)
            search.setVisible(false);
        if(cart!=null)
            cart.setVisible(false);
    }


    public void fetchOrders() {
        noQuery.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.startShimmer();
        String url = Site.ORDERS + userSession.userID;
        switch (order_status) {
            case "complete":
            case "completed":
            case "wc-completed":
                url = Site.ORDERS + userSession.userID + "?status=wc-completed";
                break;
            case "processing":
            case "wc-processing":
                url = Site.ORDERS + userSession.userID + "?status=wc-processing";
                break;
            case "pending":
            case "wc-pending":
                url = Site.ORDERS + userSession.userID + "?status=wc-pending";
                break;
            case "wc-on-hold":
            case "on-hold":
                url = Site.ORDERS + userSession.userID + "?status=wc-on-hold";
                break;
            default:
                break;
        }
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(requireContext(), "Unable to get orders.", Toast.LENGTH_LONG).show();
            progressBar.stopShimmer();
            progressBar.setVisibility(View.GONE);
            refreshBtn.setVisibility(View.VISIBLE);
        });
        RequestQueue rQueue = Volley.newRequestQueue(requireContext());
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @SuppressLint("SetTextI18n")
    public void parseJSONData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            JSONArray array = object.getJSONArray("orders");


            if (array.length() < 1) {
                noQuery.setVisibility(View.VISIBLE);
                noQuery.setText("0 " + activityTitle.replace("Orders", "Order") + " found!");
            } else {
                noQuery.setVisibility(View.GONE);
            }

            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String id = jsonObject.getString("ID");
                String date = jsonObject.getString("date_modified_date");
                String status = jsonObject.getString("status");
                String price = jsonObject.getString("total");
//                String item_count = jsonObject.getString("item_count");
                String payment_method = jsonObject.getString("payment_method");
//

                ordersRecyclerLists.add(new OrdersRecyclerClass(id, date, status, payment_method, price));
            }
//            Toast.makeText(getActivity(), String.valueOf(cartList.size()), Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //when fetching is ready
        ordersAdapter.notifyDataSetChanged();
        progressBar.stopShimmer();
        progressBar.setVisibility(View.GONE);
        refreshBtn.setVisibility(View.GONE);


    }

    @Override
    public void onStop() {
        ((MainActivity) requireActivity()).changeFragment(R.id.navigation_home); // to avoid empty home when navigate back from payment webpage
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}