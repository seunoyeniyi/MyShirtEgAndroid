package com.myshirt.eg.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.myshirt.eg.R;

import androidx.annotation.NonNull;

public class UsernameDialog extends Dialog {
//    Context context;
    OnFeedBack callback;
    EditText usernameEdit;
    Button continueBtn;
    public UsernameDialog(@NonNull Context context) {
        super(context);
//        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.username_dialog);
        usernameEdit = (EditText) findViewById(R.id.usernameDialogEdit);
        continueBtn = (Button) findViewById(R.id.continueUserDialog);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onSubmit(usernameEdit.getText().toString());
            }
        });
    }
    public void setFeedBackReceiver(OnFeedBack callback) {
        this.callback = callback;
    }
    public interface OnFeedBack{
        void onSubmit(String username);
    }
}
