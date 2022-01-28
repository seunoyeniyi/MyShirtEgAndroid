package com.fatima.fabric.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fatima.fabric.BrowserActivity;
import com.fatima.fabric.MainActivity;
import com.fatima.fabric.R;
import com.fatima.fabric.handler.UserSession;
import com.fatima.fabric.ui.LoginActivity;
import com.fatima.fabric.ui.OrdersActivity;
import com.fatima.fabric.ui.ProfileAddressActivity;

public class ProfileFragment extends Fragment {

    UserSession userSession;
    Button logoutBtn;
    TextView displayNameView;
    LinearLayout myOrders, pendingDelivery, pendingPayments, finishedOrders, shippingAddress;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        userSession = new UserSession(getActivity().getApplicationContext());

        if (!userSession.logged()) {
            startActivity(new Intent(getActivity(), LoginActivity.class).putExtra("activity_from", "profile"));
            getActivity().finish();
        } else {

        }

        displayNameView = (TextView) root.findViewById(R.id.displayNameView);
        displayNameView.setText(userSession.getUserDetails().get("username"));
        logoutBtn = (Button) root.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        myOrders = (LinearLayout) root.findViewById(R.id.myOrdersLayout);
        pendingDelivery = (LinearLayout) root.findViewById(R.id.pendingDeliveryLayout);
        pendingPayments = (LinearLayout) root.findViewById(R.id.pendingPaymentLayout);
        finishedOrders = (LinearLayout) root.findViewById(R.id.finishedOrdersLayout);
        shippingAddress = (LinearLayout) root.findViewById(R.id.shippingAddressLayout);

        myOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), OrdersActivity.class).putExtra("order_status", "all"));
            }
        });
        finishedOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), OrdersActivity.class).putExtra("order_status", "complete"));
            }
        });
        pendingDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), OrdersActivity.class).putExtra("order_status", "processing"));
            }
        });
        pendingPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), OrdersActivity.class).putExtra("order_status", "pending"));
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
            String url = "https://givephuck.com/";
            startActivity(new Intent(getActivity(), BrowserActivity.class).putExtra("url", url));
        }
    };

    public void logout() {
        userSession.logout();
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }
}