package com.myshirt.eg;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.myshirt.eg.handler.DataPart;
import com.myshirt.eg.handler.VolleyMultipartRequest;
import com.myshirt.eg.ui.BrowserActivity;
import com.myshirt.eg.ui.LoginActivity;
import com.myshirt.eg.handler.UpdateCartCount;
import com.myshirt.eg.handler.UserSession;
import com.myshirt.eg.ui.ProfileAddressActivity;
import com.myshirt.eg.ui.home.HomeFragment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {
    // --Commented out by Inspection (6/28/2021 5:38 PM):Menu globalMenu;
    TextView cartCounter;
//    BottomNavigationView navView;
    UserSession userSession;
    String selectedTab;
    int currentTab = 0;
    public Toolbar toolbar;
//    public Menu globalMenu;

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle drawerToggle;


    private static final int REQUEST_PERMISSIONS = 100;
    private static final int PICK_IMAGE_REQUEST =1 ;
    private Bitmap profileBitmap;
    private String profileFilePath;
    CircleImageView profileImageView;
    ProgressDialog loader;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"ResourceType", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        userSession = new UserSession(getApplicationContext());


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icons8_align_left);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setDrawerToggle();



        if (userSession.logged())
            getToken();



        loader = new ProgressDialog(this);
        loader.setTitle("Loading");
        loader.setMessage("Please wait...");
        loader.setCancelable(false);

//        navView = (BottomNavigationView) findViewById(R.id.bottom_nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home)
                .build(); //these fragments won't need arrow back when navigated to
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController);
//        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);




        selectedTab = (getIntent().hasExtra("selected_tab")) ? getIntent().getStringExtra("selected_tab") : "home";

        Intent intent = null;
        Bundle bundle = null;
        switch (selectedTab) {
            case "shop":
                changeFragment(R.id.shop_fragment);
                break;
            case "archive":
                changeFragment(R.id.archive_fragment);
                break;
            case "search":
                changeFragment(R.id.navigation_search);
                break;
            case "cart":
                changeFragment(R.id.navigation_cart);
                break;
            case "profile":
            case "account":
                startActivity(new Intent(MainActivity.this, ProfileAddressActivity.class));
                break;
            case "orders":
                bundle = new Bundle();
                bundle.putString("order_status", "all");
                changeFragment(R.id.orders_fragment, bundle);
                break;
            case "orders_pending":
                bundle = new Bundle();
                bundle.putString("order_status", "pending");
                changeFragment(R.id.orders_fragment, bundle);
                break;
            default:
                break;
        }



        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation); // initiate a Navigation View
        //set item background
//        navView.setItemBackground(getResources().getDrawable(R.drawable.background));
        // implement setNavigationSelectedListener event
        navigationView.setNavigationItemSelectedListener(drawerNavListener);
        View headerLayout = navigationView.getHeaderView(0);
        TextView headerUsername = headerLayout.findViewById(R.id.displayNameView);
        TextView headerViewProfile = headerLayout.findViewById(R.id.viewProfile);
        profileImageView = (CircleImageView) headerLayout.findViewById(R.id.profile_pic);
        profileImageView.setOnClickListener(profilePicClicked);
        updateProfilePic();
        //buttons eg drawer on activity_main
        ImageButton drawerCloseBtn = findViewById(R.id.closeBtn);
        Menu nav_Menu = navigationView.getMenu();
        if (userSession.logged()) {
            headerUsername.setText(userSession.username);
            headerViewProfile.setText("Account Settings");
        } else {
            headerUsername.setText("Hi!");
            headerViewProfile.setText("Log In");
        }
        headerViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userSession.logged()) {
                    startActivity(new Intent(MainActivity.this, ProfileAddressActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });
        drawerCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawers();
            }
        });

    }

    public void setDrawerToggle() {

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(false);
        drawerToggle.setHomeAsUpIndicator(R.drawable.ic_icons8_align_left);
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);

                if (fragment instanceof HomeFragment) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                } else {
                    onBackPressed();
                }

            }
        });

        // initiate a DrawerLayout
        drawerLayout.closeDrawers();

        //resize hamburger icon
//        for (int i = 0; i < toolbar.getChildCount(); i++) {
//            if(toolbar.getChildAt(i) instanceof ImageButton){
//                toolbar.getChildAt(i).setScaleX(0.5f);
//                toolbar.getChildAt(i).setScaleY(0.5f);
//            }
//        }
    }


    NavigationView.OnNavigationItemSelectedListener drawerNavListener = new NavigationView.OnNavigationItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {

                case R.id.nav_home:
                    changeFragment(R.id.navigation_home);
                    break;
                case R.id.nav_shop:
                    changeFragment(R.id.shop_fragment);
                    break;
                case R.id.nav_category:
                    changeFragment(R.id.navigation_categories);
                    break;
                case R.id.nav_wishlist:
                    if (userSession.logged()) {
                        changeFragment(R.id.wishlist_fragment);
                    } else {
                        Toast.makeText(MainActivity.this, "Please login first!", Toast.LENGTH_LONG).show();
                    }

                break;
                case R.id.nav_orders: {
                    if (userSession.logged()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("order_status", "all");
                        changeFragment(R.id.orders_fragment, bundle);
                    } else {
                        Toast.makeText(MainActivity.this, "Please login first!", Toast.LENGTH_LONG).show();

                    }
                }
                break;
                case R.id.nav_blog: {
                    String url = Site.ADDRESS + "blog?in_sk_app=1";
                    Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("title", "Blog");
                    startActivity(intent);
                }
                break;
                case R.id.nav_support: {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"customer@myshirt-eg.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Hi, MyShirt-Eg Support Team");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                break;
                case R.id.nav_faq: {
                    String url = Site.ADDRESS + "faqs?in_sk_app=1";
                    Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("title", "FAQs");
                    startActivity(intent);
                }
                break;
                case R.id.nav_contact: {
                    String url = Site.ADDRESS + "contact-us?in_sk_app=1";
                    Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("title", "Contact");
                    startActivity(intent);
                }
                break;
                case R.id.nav_about: {
                    String url = Site.ADDRESS + "about-us?in_sk_app=1";
                    Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("title", "About");
                    startActivity(intent);
                }
                break;
                case R.id.nav_logut:
                    logout();
                break;
                default:
                    break;
            }

            drawerLayout.closeDrawers();

            return false;
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();

    }

//    public void changeTab(int tab) {
//        navView.setSelectedItemId(tab);
//    }

    public void changeFragment(int fragment_id) {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        navController.navigate(fragment_id);
    }
    public void changeFragment(int fragment_id, Bundle bundle) {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        navController.navigate(fragment_id, bundle);
    }



    public void appBarType(String type) {
        LinearLayout titleLayout = toolbar.findViewById(R.id.title_layout);
        LinearLayout userLayout = toolbar.findViewById(R.id.userLayout);
        titleLayout.setVisibility(View.GONE);
        userLayout.setVisibility(View.GONE);
        if (type.equals("welcome")) {
            userLayout.setVisibility(View.VISIBLE);
        } else {
            titleLayout.setVisibility(View.VISIBLE);
        }
    }
    public void appBarType(String type, String title) {
        TextView titleText = toolbar.findViewById(R.id.toolbar_title);
        titleText.setText(Html.fromHtml(title));
        appBarType(type);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
//        globalMenu = menu; //to access to cart menu globally
        MenuItem item = menu.findItem(R.id.cartMenuIcon);
        item.setActionView(R.layout.cart_icon_update_layout);
        RelativeLayout cartCount = (RelativeLayout)   item.getActionView();
        cartCounter = (TextView) cartCount.findViewById(R.id.actionbar_notifcation_textview);
        cartCounter.setText("0");
        cartCounter.setVisibility(View.GONE);
        ImageView iconImage =  (ImageView) cartCount.findViewById(R.id.cartIconMenu);
//        iconImage.setColorFilter(this.getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
        new UpdateCartCount(this, userSession.userID, cartCounter);
        cartCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(item);
            }
        });

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

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.cartMenuIcon:{
                changeFragment(R.id.navigation_cart);
            }
            break;
            case R.id.nav_menu_search:{
                changeFragment(R.id.navigation_search);
            }
            break;
            case R.id.nav_menu_wishlist: {
                changeFragment(R.id.wishlist_fragment);
            }
            break;
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        userSession.logout();
        clearCookies(this);
        finish();
        startActivity(getIntent());
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


    private void getToken() {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                //If task is failed then
                if (task.isSuccessful()) {
                    //Token
                    String token = task.getResult();
                    String url = Site.SAVE_DEVICE + userSession.userID;
                    JSONObject data = new JSONObject();
                    try {
                        data.put("device", token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //nothing to do... either save or not
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //nothing to do even if there is error
                        }
                    });

                    RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
                    postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    rQueue.add(postRequest);
                }


            }
        });
    }



    //PROFILE PICTURE UPLOAD
    View.OnClickListener profilePicClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE))) {

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_PERMISSIONS);
                }
            } else {
                showFileChooser();
            }
        }
        private void showFileChooser() {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }
    };


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri picUri = data.getData();
            profileFilePath = getPath(picUri);
            if (profileFilePath != null) {
                try {

                    profileBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), picUri);
                    uploadBitmap(profileBitmap);
                    profileImageView.setImageBitmap(profileBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(
                        MainActivity.this,"no image selected",
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadBitmap(final Bitmap bitmap) {
        loader.show();

        String url = Site.UPDATE_USER + userSession.userID;
//        Log.e("URL", url);
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            if (obj.getString("image").length() > 10) { //if image is greater than 10 surely is a long image url
                                userSession.set_profile_image(obj.getString("image"));
                                Glide.with(MainActivity.this)
                                        .load(obj.getString("image"))
                                        .into(profileImageView);
                                Toast.makeText(getApplicationContext(), "Profile image updated.", Toast.LENGTH_SHORT).show();
                            }
//
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        loader.hide();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //error
                        Toast.makeText(getApplicationContext(), "Unable to update profile picture.", Toast.LENGTH_SHORT).show();
                        loader.hide();
                    }
                }) {


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UseCompatLoadingForDrawables")
    public void updateProfilePic() {
        if (getIntent().hasExtra("from_login")) {

            String url = Site.USER + userSession.userID;
            StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject user = new JSONObject(response);
                        if (user.getString("image").length() > 10) { //greater than 10 is surely image url
                            userSession.set_profile_image(user.getString("image"));
                            Glide.with(MainActivity.this)
                                    .load(user.getString("image"))
                                    .placeholder(R.drawable.boy_man_avatar)
                                    .into(profileImageView);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, (VolleyError error) -> {
              //error
            });
            request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Volley.newRequestQueue(this).add(request);


        } else if (userSession.get_profile_image().length() > 10) { //is an image url
            Glide.with(MainActivity.this)
                    .load(userSession.get_profile_image())
                    .placeholder(R.drawable.boy_man_avatar)
                    .into(profileImageView);
        }

    }
}

