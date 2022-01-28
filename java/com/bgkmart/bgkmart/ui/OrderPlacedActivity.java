package com.fatima.fabric.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.fatima.fabric.MainActivity;
import com.fatima.fabric.R;

public class OrderPlacedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_placed);
        getSupportActionBar().hide();
        Button continueBtn = (Button) findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                go to orders page
                startActivity(new Intent(OrderPlacedActivity.this, OrdersActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(OrderPlacedActivity.this, MainActivity.class).putExtra("selected_tab", "shop"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(OrderPlacedActivity.this, MainActivity.class).putExtra("selected_tab", "shop"));
        finish();
        return true;
    }
}