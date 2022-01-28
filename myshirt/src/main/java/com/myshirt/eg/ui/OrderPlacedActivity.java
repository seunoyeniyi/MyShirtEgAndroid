package com.myshirt.eg.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.myshirt.eg.MainActivity;
import com.myshirt.eg.R;

public class OrderPlacedActivity extends AppCompatActivity {
    TextView infoText;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_placed);
        getSupportActionBar().hide();

        infoText = (TextView) findViewById(R.id.orderInfo);
        if (getIntent().hasExtra("created")) {
            if (getIntent().getStringExtra("created").equals("unsure")) {
                infoText.setText("Please confirm your order eg Profile, if we accept your order.");
            }
        }


        Button continueBtn = (Button) findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                go to orders page
                startActivity(new Intent(OrderPlacedActivity.this, MainActivity.class).putExtra("selected_tab", "orders")
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(OrderPlacedActivity.this, MainActivity.class).putExtra("selected_tab", "orders"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(OrderPlacedActivity.this, MainActivity.class).putExtra("selected_tab", "orders"));
        finish();
        return true;
    }
}