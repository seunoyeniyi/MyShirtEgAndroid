package com.myshirt.eg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.myshirt.eg.handler.SiteInfo;
import com.myshirt.eg.handler.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class SplashScreen extends AppCompatActivity {


//    Animation rotateAnim;
//    ImageView logoImage;
    ProgressBar progressBar;

    UserSession userSession;
    SiteInfo siteInfo;

    private static final String CHANNEL_ID = "101";

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSession = new UserSession(this.getApplicationContext());
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor(getResources().getString(R.color.white))));
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();

       getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        if (Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        }

        siteInfo = new SiteInfo(this.getApplicationContext());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getResources().getString(R.color.white)));
        }

        createNotificationChannel();
        if (userSession.logged()) {
            getToken();
        }


//        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);

//        logoImage = (ImageView) findViewById(R.id.logo);

//        logoImage.setAnimation(rotateAnim);

        progressBar = findViewById(R.id.progressBar);


        if (!need_checkup()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    proceed();
                }
            }, 3000); //3000 as production time
        }

    }

    public void proceed() {
        Intent main = new Intent(SplashScreen.this, MainActivity.class);
        main.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(main);
        finish();
    }

    private void getToken() {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                //If task is failed then
                if (task.isSuccessful()) {
                    //Token
                    String token = task.getResult();
                    Log.e("Token", token);

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

                    RequestQueue rQueue = Volley.newRequestQueue(SplashScreen.this);
                    postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    rQueue.add(postRequest);
                }


            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "PushNotificationChannel";
            String description = "Receive Offer notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public boolean need_checkup() {
        Calendar calendar = Calendar.getInstance();
        long last_check = siteInfo.get_last_check(); //last check time
        long current_time = calendar.getTimeInMillis(); //today present time
        long time_difference = current_time - last_check; //difference btw the timestamp

        Calendar calendar_difference = Calendar.getInstance();
        calendar_difference.setTimeInMillis(time_difference); //set the timestamp difference

        if (last_check == 0) {
            checkupCheck();
            return true;
        } else if (calendar_difference.get(Calendar.HOUR) > 24) {
            checkupCheck();
            return true;
        }

        checkupCheck();//still check up anyway... but can cut-shut without fulfilment

        return false;
    }

    public void checkupCheck() {

        progressBar.setVisibility(View.VISIBLE);
        String url = Site.INFO;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject site = new JSONObject(response);

                    siteInfo.set("name", site.getString("name"));
                    siteInfo.set_banner_enable_value("slide", site.getString("enable_slide_banners").equals("1"));
                    siteInfo.set_banner_enable_value("big", site.getString("enable_big_banners").equals("1"));
                    siteInfo.set_banner_enable_value("carousel", site.getString("enable_carousel_banners").equals("1"));
                    siteInfo.set_banner_enable_value("thin", site.getString("enable_thin_banners").equals("1"));
//                    siteInfo.set_banner_enable_value("sale", site.getString("enable_sale_banners").equals("1"));
                    siteInfo.set_banner_enable_value("grid", site.getString("enable_grid_banners").equals("1"));
                    siteInfo.set_banner_enable_value("video", site.getString("enable_video_banners").equals("1"));

                    Calendar calendar = Calendar.getInstance();
                    siteInfo.set_last_check(calendar.getTimeInMillis());

                    progressBar.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            proceed();
                        }
                    }, 3000);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, (VolleyError error) -> {
            //error
            progressBar.setVisibility(View.GONE);
            proceed();
        });
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
    }

}