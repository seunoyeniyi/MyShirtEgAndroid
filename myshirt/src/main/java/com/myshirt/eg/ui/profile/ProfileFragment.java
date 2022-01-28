package com.myshirt.eg.ui.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.myshirt.eg.MainActivity;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.handler.UserSession;
import com.myshirt.eg.ui.LoginActivity;
import com.myshirt.eg.ui.ProfileAddressActivity;

public class ProfileFragment extends Fragment {

    UserSession userSession;
    Button logoutBtn;
    TextView displayNameView;
    LinearLayout wishList, myOrders, pendingDelivery, pendingPayments, finishedOrders, shippingAddress;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        ((MainActivity) getActivity()).appBarType("title", "Account");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        userSession = new UserSession(getActivity().getApplicationContext());

        if (!userSession.logged()) {
            startActivity(new Intent(getActivity(), LoginActivity.class).putExtra("activity_from", "profile"));
            getActivity().finish();
        }

        displayNameView = (TextView) root.findViewById(R.id.displayNameView);
        displayNameView.setText("Welcome " + userSession.getUserDetails().get("username") + "!");
        logoutBtn = (Button) root.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        wishList = (LinearLayout) root.findViewById(R.id.wishListLayout);
        myOrders = (LinearLayout) root.findViewById(R.id.myOrdersLayout);
        pendingDelivery = (LinearLayout) root.findViewById(R.id.pendingDeliveryLayout);
        pendingPayments = (LinearLayout) root.findViewById(R.id.pendingPaymentLayout);
        finishedOrders = (LinearLayout) root.findViewById(R.id.finishedOrdersLayout);
        shippingAddress = (LinearLayout) root.findViewById(R.id.shippingAddressLayout);

        wishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getActivity(), WishListFragment.class));
                ((MainActivity) requireActivity()).changeFragment(R.id.wishlist_fragment);
            }
        });
        myOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getActivity(), OrdersFragment.class).putExtra("order_status", "all"));
                Bundle bundle = new Bundle();
                bundle.putString("order_status", "all");
                ((MainActivity) requireActivity()).changeFragment(R.id.orders_fragment, bundle);
            }
        });
        finishedOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getActivity(), OrdersFragment.class).putExtra("order_status", "complete"));
                Bundle bundle = new Bundle();
                bundle.putString("order_status", "complete");
                ((MainActivity) requireActivity()).changeFragment(R.id.orders_fragment, bundle);
            }
        });
        pendingDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getActivity(), OrdersFragment.class).putExtra("order_status", "processing"));
                Bundle bundle = new Bundle();
                bundle.putString("order_status", "processing");
                ((MainActivity) requireActivity()).changeFragment(R.id.orders_fragment, bundle);
            }
        });
        pendingPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getActivity(), OrdersFragment.class).putExtra("order_status", "pending"));
                Bundle bundle = new Bundle();
                bundle.putString("order_status", "pending");
                ((MainActivity) requireActivity()).changeFragment(R.id.orders_fragment, bundle);
            }
        });
        shippingAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ProfileAddressActivity.class));
            }
        });

        LinearLayout aboutUsLayout = (LinearLayout) root.findViewById(R.id.aboutUsLayout);
        aboutUsLayout.setOnClickListener(aboutUs);

        return root;
    }
    public View.OnClickListener aboutUs = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String url = Site.ADDRESS + "about-us";
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    };

    public void logout() {
        userSession.logout();
        clearCookies(requireContext());
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }
    public static void clearCookies(Context context)
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//            Log.d(C.TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
//            Log.d(C.TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
}