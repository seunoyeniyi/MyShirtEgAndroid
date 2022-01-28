package com.fatima.fabric.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fatima.fabric.R;
import com.fatima.fabric.Site;
import com.fatima.fabric.handler.AddToCartWithTotalUpdate;
import com.fatima.fabric.handler.UpdateCartCount;
import com.fatima.fabric.handler.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class CartAdapter extends BaseAdapter {

    Context context;
    ArrayList<CartList> cartLists;
    LayoutInflater layoutInflater;
    UserSession userSession;
    Activity activity;
    TextView cartCounter;
    public CartAdapter(Context context, ArrayList<CartList> cartLists, TextView cartCounter) {
        this.context = context;
        this.cartLists = cartLists;
        this.cartCounter = cartCounter;
        this.activity = (Activity) context;
        userSession = new UserSession(context.getApplicationContext());
    }

    @Override
    public int getCount() {
        return cartLists.size();
    }

    @Override
    public Object getItem(int i) {
        return cartLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n"})
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;

        rowView = layoutInflater.inflate(R.layout.single_cart_layout, null);
        TextView productTitleView =(TextView) rowView.findViewById(R.id.cartProductTitle);
        ImageView productImageView = (ImageView) rowView.findViewById(R.id.cartImage);
        TextView productPriceView =(TextView) rowView.findViewById(R.id.cartProductPrice);
        TextView productQuantity =(TextView) rowView.findViewById(R.id.cartProductQuantity);
        Button minusBtn = (Button) rowView.findViewById(R.id.minusBtn);
        Button plus = (Button) rowView.findViewById(R.id.plusBtn);
        Button removeBtn = (Button) rowView.findViewById(R.id.removeBtn);

        productTitleView.setText(cartLists.get(position).getProductTitle());
        new DownloadImageTask(productImageView)
                .execute(cartLists.get(position).getProductImage().replace("localhost", Site.DOMAIN));
        productPriceView.setText(cartLists.get(position).getPrice() + "Rs");
        productQuantity.setText(cartLists.get(position).getQuantity());

        //disable minus button if quantity = 1
        if (cartLists.get(position).getQuantity().equals("1")) minusBtn.setEnabled(false);

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity = Integer.parseInt(cartLists.get(position).getQuantity());
                int newQuantity = currentQuantity - 1;
                if (newQuantity < 1) {
                    //disable button
                    minusBtn.setEnabled(false);
                } else {
                    if (newQuantity < 2) minusBtn.setEnabled(false);
//                   update cart
                    try {
                        ProgressBar totalProgress = (ProgressBar) activity.findViewById(R.id.totalProgressBar);
                        LinearLayout totalLayout = (LinearLayout) activity.findViewById(R.id.totalLayout);
                        TextView subtotalView = (TextView) activity.findViewById(R.id.subtotalView);
                        TextView totalView = (TextView) activity.findViewById(R.id.totalView);
                        new AddToCartWithTotalUpdate(context, cartLists.get(position).getId(), String.valueOf(newQuantity), true,
                                cartCounter, totalProgress, totalLayout, subtotalView, totalView);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //update quantity textView
                    productQuantity.setText(String.valueOf(newQuantity));
                    //update cartList quantity by position
                    cartLists.get(position).setQuantity(String.valueOf(newQuantity));
                }
            }
        });
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity = Integer.parseInt(cartLists.get(position).getQuantity());
                int newQuantity = currentQuantity + 1;
                if (newQuantity > 1) {
                    minusBtn.setEnabled(true);
                }
                //update cart
                try {
                    ProgressBar totalProgress = (ProgressBar) activity.findViewById(R.id.totalProgressBar);
                    LinearLayout totalLayout = (LinearLayout) activity.findViewById(R.id.totalLayout);
                    TextView subtotalView = (TextView) activity.findViewById(R.id.subtotalView);
                    TextView totalView = (TextView) activity.findViewById(R.id.totalView);
                    new AddToCartWithTotalUpdate(context, cartLists.get(position).getId(), String.valueOf(1), false,
                            cartCounter, totalProgress, totalLayout, subtotalView, totalView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //update quantity textView
                productQuantity.setText(String.valueOf(newQuantity));
                //update cartList quantity by position
                cartLists.get(position).setQuantity(String.valueOf(newQuantity));
            }
        });
        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ProgressBar totalProgress = (ProgressBar) activity.findViewById(R.id.totalProgressBar);
                    LinearLayout totalLayout = (LinearLayout) activity.findViewById(R.id.totalLayout);
                    TextView subtotalView = (TextView) activity.findViewById(R.id.subtotalView);
                    TextView totalView = (TextView) activity.findViewById(R.id.totalView);
                    new AddToCartWithTotalUpdate(context, cartLists.get(position).getId(), String.valueOf(0), false,
                            cartCounter, totalProgress, totalLayout, subtotalView, totalView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cartLists.remove(position);
                        CartAdapter.this.notifyDataSetChanged();
                    }
                }, 1000);
            }
        });

        return rowView;
    }
    public void updateTotals(Context context) {
        ProgressBar totalProgress = (ProgressBar) activity.findViewById(R.id.totalProgressBar);
        totalProgress.setVisibility(View.VISIBLE);
        LinearLayout totalLayout = (LinearLayout) activity.findViewById(R.id.totalLayout);
        totalLayout.setVisibility(View.GONE);


        String url = Site.CART + userSession.userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseJSONDataForTotals(response);
                new UpdateCartCount(context, userSession.userID, cartCounter);
            }
        }, (VolleyError error) -> {
            //handle error
        });
        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void parseJSONDataForTotals(String json) {
        TextView subtotalView = (TextView) activity.findViewById(R.id.subtotalView);
        TextView totalView = (TextView) activity.findViewById(R.id.totalView);
        try {
            JSONObject object = new JSONObject(json);

//            double subtotalDouble = Double.parseDouble(object.getString("subtotal"));
            DecimalFormat formatter = new DecimalFormat("#,###.00");
            subtotalView.setText(object.getString("subtotal") + "Rs");
            totalView.setText(object.getString("subtotal") + "Rs");

            ProgressBar totalProgress = (ProgressBar) activity.findViewById(R.id.totalProgressBar);
            totalProgress.setVisibility(View.GONE);
            LinearLayout totalLayout = (LinearLayout) activity.findViewById(R.id.totalLayout);
            totalLayout.setVisibility(View.VISIBLE);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        @SuppressLint("StaticFieldLeak")
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
