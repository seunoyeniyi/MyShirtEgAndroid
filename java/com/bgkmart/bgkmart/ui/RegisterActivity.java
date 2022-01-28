package com.fatima.fabric.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fatima.fabric.CheckoutActivity;
import com.fatima.fabric.MainActivity;
import com.fatima.fabric.R;
import com.fatima.fabric.Site;
import com.fatima.fabric.handler.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    UserSession userSession;
    String activity_from;
    EditText usernameEditText, emailEditText, passwordEditText, tronWallet;
    Button registerBtn, gotoLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().hide();
        getSupportActionBar().setTitle("Login");
        userSession = new UserSession(this.getApplicationContext());

        activity_from = getIntent().getStringExtra("activity_from");

        usernameEditText = (EditText) findViewById(R.id.usernameEditView);
        emailEditText = (EditText) findViewById(R.id.emailEditView);
        tronWallet = (EditText) findViewById(R.id.tronWallet);
        passwordEditText = (EditText) findViewById(R.id.passwordEditView);
        registerBtn = (Button) findViewById(R.id.saveBtn);
        gotoLoginBtn = (Button) findViewById(R.id.gotoLoginBtn);

        gotoLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean formPassed = true;
                if (isEmpty(usernameEditText)) {
                    usernameEditText.setError("Username required!");
                    usernameEditText.requestFocus();
                    formPassed = false;
                }
                if (!isEmail(emailEditText)) {
                    emailEditText.setError("Valid email required!");
                    emailEditText.requestFocus();
                    formPassed = false;
                }
                if (isEmpty(passwordEditText)) {
                    passwordEditText.setError("Password required!");
                    passwordEditText.requestFocus();
                    formPassed = false;
                }
                if (formPassed) {
                    try {
                        ProgressDialog loader = ProgressDialog.show(RegisterActivity.this, "Loading", "Please wait...", true);
                        tryRegistering(loader);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
    public boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }
    boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public void tryRegistering(ProgressDialog loader) throws JSONException {
        String url = Site.REGISTER;
        JSONObject data = new JSONObject();
        data.put("username", usernameEditText.getText().toString());
        data.put("email", emailEditText.getText().toString());
        data.put("tron_wallet", tronWallet.getText().toString());
        data.put("password", passwordEditText.getText().toString());


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
                        try {
                            JSONObject object = new JSONObject(response.toString());
                            if (object.has("code") || object.getString("data").equals("null")) {
                                Toast.makeText(RegisterActivity.this, object.getString("message"), Toast.LENGTH_LONG).show();
                            } else {
                                JSONObject data = new JSONObject(object.getString("data"));
                                UserSession userSession = new UserSession(RegisterActivity.this.getApplicationContext());
                                userSession.createLoginSession(data.getString("ID"), data.getString("user_login"), data.getString("user_email"), true);
                                Toast toast = Toast.makeText(RegisterActivity.this, "Registration Completed!", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                                toast.show();
                                switch (activity_from) {
                                    case "checkout":
                                        startActivity(new Intent(RegisterActivity.this, CheckoutActivity.class));
                                        break;
                                    case "address":
                                        startActivity(new Intent(RegisterActivity.this, AddressActivity.class));
                                        break;
                                    default:
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        break;
                                }
                            }
                            loader.hide();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(RegisterActivity.this, "Registration denied by the server! - Your email or username may already exists.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();
                loader.hide();
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(this.getApplicationContext());
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }
}