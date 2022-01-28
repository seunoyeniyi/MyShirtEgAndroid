package com.fatima.fabric.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fatima.fabric.ProductActivity;
import com.fatima.fabric.R;
import com.fatima.fabric.Site;
import com.fatima.fabric.handler.AddToCart;
import com.fatima.fabric.handler.UserSession;

import org.json.JSONException;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ProductGridAdapterWithNoCartCounter extends BaseAdapter {

    Context context;
    ArrayList<ProductList> productLists;
    Activity activity;
    LayoutInflater layoutInflater;
    UserSession userSession;
    public ProductGridAdapterWithNoCartCounter(Context context, ArrayList<ProductList> productLists) {
        this.context = context;
        this.productLists = productLists;
        this.activity = (Activity) context;
        userSession = new UserSession(context.getApplicationContext());
    }

    @Override
    public int getCount() {
        return productLists.size();
    }

    @Override
    public Object getItem(int i) {
        return productLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams", "SetTextI18n", "DefaultLocale"})
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;

        rowView = layoutInflater.inflate(R.layout.single_product_card, null);
        TextView titleView =(TextView) rowView.findViewById(R.id.titleView);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageview);
        TextView priceView = (TextView) rowView.findViewById(R.id.priceView);
        TextView regularPriceView = (TextView) rowView.findViewById(R.id.regularPriceView);

        titleView.setText(productLists.get(position).getName());
        regularPriceView.setPaintFlags(regularPriceView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        new DownloadImageTask(imageView)
                .execute(productLists.get(position).getImage().replace("localhost", Site.DOMAIN));
//        double doublePrice = Double.parseDouble(productLists.get(position).getPrice());
        DecimalFormat formatter = new DecimalFormat("#,###");

        if (productLists.get(position).getProduct_type().equals("variable") || productLists.get(position).getType().equals("variable")) {
            priceView.setText("From " + productLists.get(position).getPrice() + "Rs"); //variable product
            regularPriceView.setVisibility(View.GONE);
        } else { // FOR SIMPLE PRODUCT
            priceView.setText(productLists.get(position).getPrice() + "Rs"); //simple product
            regularPriceView.setText(productLists.get(position).getRegular_price() + "Rs");
        }

        LinearLayout contentLayout = (LinearLayout) rowView.findViewById(R.id.contentLayout); //layer to click for new activity
        LinearLayout cartLayout = (LinearLayout) rowView.findViewById(R.id.cartLayoutBtn); //layout for cart btn
        TextView cartTextView = (TextView) rowView.findViewById(R.id.cartTextView);

        if (productLists.get(position).getProduct_type().equals("variable") || productLists.get(position).getType().equals("variable")) {
            //setup as variable product
            cartTextView.setText("Options");
            cartLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openProductActivity(productLists.get(position).getId(),
                            productLists.get(position).getImage(),
                            productLists.get(position).getName(),
                            productLists.get(position).getDescription());
                }
            });
        } else {
            //setup as simple product
            cartLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    add to cart
                    try {
                        new AddToCart(context, productLists.get(position).getId(), "1", false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openProductActivity(productLists.get(position).getId(),
                        productLists.get(position).getImage(),
                        productLists.get(position).getName(),
                        productLists.get(position).getDescription());
            }
        });

        return rowView;
    }
    public void openProductActivity(String productID, String image, String title, String description) {
        Intent intent = new Intent(context, ProductActivity.class);
        intent.putExtra("product_id", productID);
        intent.putExtra("product_image", image);
        intent.putExtra("product_title", title);
        intent.putExtra("product_description", description);

        if (context.getClass().getSimpleName().equals("ProductActivity")) {
            activity.finish();
        }
        activity.startActivity(intent);
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
