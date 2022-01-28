package com.myshirt.eg.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.handler.AddToCartWithTotalUpdate;
import com.myshirt.eg.handler.PriceFormatter;
import com.bumptech.glide.Glide;

import org.json.JSONException;

import java.util.ArrayList;

public class CartAdapter extends BaseAdapter {

    Context context;
    ArrayList<CartList> cartLists;
    LayoutInflater layoutInflater;
    // --Commented out by Inspection (6/28/2021 5:38 PM):UserSession userSession;
    Activity activity;
    TextView cartCounter;
    public CartAdapter(Context context, ArrayList<CartList> cartLists, TextView cartCounter) {
        this.context = context;
        this.cartLists = cartLists;
        this.cartCounter = cartCounter;
        this.activity = (Activity) context;
//        userSession = new UserSession(context.getApplicationContext());
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
        TextView productTotalPriceView =(TextView) rowView.findViewById(R.id.total_price);
        TextView productQuantity =(TextView) rowView.findViewById(R.id.cartProductQuantity);
        Button minusBtn = (Button) rowView.findViewById(R.id.minusBtn);
        Button plus = (Button) rowView.findViewById(R.id.plusBtn);
        Button removeBtn = (Button) rowView.findViewById(R.id.removeBtn);

        productTitleView.setText(cartLists.get(position).getProductTitle());
//        new DownloadImageTask(productImageView)
//                .execute(cartLists.get(position).getProductImage().replace("localhost", Site.DOMAIN));
        Glide
                .with(context)
                .load(cartLists.get(position).getProductImage().replace("localhost", Site.DOMAIN))
                .centerCrop()
                .placeholder(R.drawable.sample_placeholder)
                .into(productImageView);
        productPriceView.setText(Site.CURRENCY + cartLists.get(position).getPrice());
        productQuantity.setText(cartLists.get(position).getQuantity());
        if (Double.parseDouble(cartLists.get(position).getQuantity()) > 1) {
            Double total = Double.parseDouble(cartLists.get(position).getPrice()) * Double.parseDouble(cartLists.get(position).getQuantity());
            productTotalPriceView.setText(Html.fromHtml("<small><font color='gray'>x" + cartLists.get(position).getQuantity() + "=</font></small> <b>" + Site.CURRENCY + PriceFormatter.format(total) + "</b>"));
        } else {
            productTotalPriceView.setVisibility(View.GONE);
        }
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
//                        TextView pointsTextView = (TextView) activity.findViewById(R.id.pointsTextView);
//                        TextView applyRewardTextView = (TextView) activity.findViewById(R.id.rewardApplyText);
                        TextView couponView = (TextView) activity.findViewById(R.id.discountView);
                        TextView freeShippingAlert = (TextView) activity.findViewById(R.id.free_shipping_alert);

                        if (newQuantity > 1) {
                            Double total = Double.parseDouble(cartLists.get(position).getPrice()) * newQuantity;
                            productTotalPriceView.setText(Html.fromHtml("<small><font color='gray'>x" + String.valueOf(newQuantity) + "=</font></small> <b>" + Site.CURRENCY + PriceFormatter.format(total) + "</b>"));
                            productTotalPriceView.setVisibility(View.VISIBLE);
                        } else {
                            productTotalPriceView.setVisibility(View.GONE);
                        }

                        new AddToCartWithTotalUpdate(context, cartLists.get(position).getId(), String.valueOf(newQuantity), true,
                                cartCounter, totalProgress, totalLayout, subtotalView, totalView, couponView, freeShippingAlert /*pointsTextView, applyRewardTextView*/);
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
//                    TextView pointsTextView = (TextView) activity.findViewById(R.id.pointsTextView);
//                    TextView applyRewardTextView = (TextView) activity.findViewById(R.id.rewardApplyText);
                    TextView couponView = (TextView) activity.findViewById(R.id.discountView);
                    TextView freeShippingAlert = (TextView) activity.findViewById(R.id.free_shipping_alert);

                    if (newQuantity > 1) {
                        Double total = Double.parseDouble(cartLists.get(position).getPrice()) * newQuantity;
                        productTotalPriceView.setText(Html.fromHtml("<small><font color='gray'>x" + String.valueOf(newQuantity) + "=</font></small> <b>" + Site.CURRENCY + PriceFormatter.format(total) + "</b>"));
                        productTotalPriceView.setVisibility(View.VISIBLE);
                    } else {
                        productTotalPriceView.setVisibility(View.GONE);
                    }

                    new AddToCartWithTotalUpdate(context, cartLists.get(position).getId(), String.valueOf(1), false,
                            cartCounter, totalProgress, totalLayout, subtotalView, totalView, couponView, freeShippingAlert /*pointsTextView, applyRewardTextView*/);
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
//                    TextView pointsTextView = (TextView) activity.findViewById(R.id.pointsTextView);
//                    TextView applyRewardTextView = (TextView) activity.findViewById(R.id.rewardApplyText);
                    TextView couponView = (TextView) activity.findViewById(R.id.discountView);
                    TextView freeShippingAlert = (TextView) activity.findViewById(R.id.free_shipping_alert);
                    new AddToCartWithTotalUpdate(context, cartLists.get(position).getId(), String.valueOf(0), false,
                            cartCounter, totalProgress, totalLayout, subtotalView, totalView, couponView, freeShippingAlert /*pointsTextView, applyRewardTextView*/);
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
    //    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//        @SuppressLint("StaticFieldLeak")
//        ImageView bmImage;
//
//        public DownloadImageTask(ImageView bmImage) {
//            this.bmImage = bmImage;
//        }
//
//        protected Bitmap doInBackground(String... urls) {
//            String urldisplay = urls[0];
//            Bitmap mIcon11 = null;
//            try {
//                InputStream eg = new java.net.URL(urldisplay).openStream();
//                mIcon11 = BitmapFactory.decodeStream(eg);
//            } catch (Exception e) {
//                Log.e("Error", Objects.requireNonNull(e.getMessage()));
//                e.printStackTrace();
//            }
//            return mIcon11;
//        }
//
//        protected void onPostExecute(Bitmap result) {
//            bmImage.setImageBitmap(result);
//        }
//    }
}
