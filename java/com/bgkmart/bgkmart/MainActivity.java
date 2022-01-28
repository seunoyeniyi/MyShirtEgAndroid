package com.fatima.fabric;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.fatima.fabric.handler.UpdateCartCount;
import com.fatima.fabric.handler.UserSession;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    Menu globalMenu;
    TextView cartCounter;
    BottomNavigationView navView;
    UserSession userSession;
    String selectedTab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar);

        userSession = new UserSession(getApplicationContext());

        selectedTab = (getIntent().hasExtra("selected_tab")) ? getIntent().getStringExtra("selected_tab") : "home";

        navView = (BottomNavigationView) findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.categoriesBottomBtn, R.id.shopBottomBtn, R.id.cartBottomBtn, R.id.profileBottomBtn)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
//        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        switch (selectedTab) {
            case "search":
                navView.setSelectedItemId(R.id.categoriesBottomBtn);
                break;
            case "shop":
                navView.setSelectedItemId(R.id.shopBottomBtn);
                break;
            case "cart":
                navView.setSelectedItemId(R.id.cartBottomBtn);
                break;
            case "profile":
                navView.setSelectedItemId(R.id.profileBottomBtn);
                break;
            default:
                break;
        }

    }



    @SuppressLint("SetTextI18n")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        globalMenu = menu; //to access to cart menu globally
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem item = menu.findItem(R.id.cartMenuIcon);
        item.setActionView(R.layout.cart_icon_update_layout);
        RelativeLayout cartCount = (RelativeLayout)   item.getActionView();
        cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
        cartCounter.setText("0");
        cartCounter.setVisibility(View.GONE);
        new UpdateCartCount(this, userSession.userID, cartCounter);
        cartCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(item);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.cartMenuIcon:{
                navView.setSelectedItemId(R.id.cartBottomBtn);
            }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}

