package com.myshirt.eg.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.myshirt.eg.CheckoutActivity;
import com.myshirt.eg.MainActivity;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.handler.UserSession;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    UserSession userSession;
    String activity_from;
    EditText usernameEditText, emailEditText, passwordEditText;
    Button registerBtn, gotoLoginBtn;
    LoginButton fbLoginBtn;
    CallbackManager callbackManager;
    SignInButton googleSignInButton;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 222;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().hide();
        getSupportActionBar().setTitle("Login");
        userSession = new UserSession(this.getApplicationContext());

        if (Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(getResources().getString(R.color.white)));
        }

        if (userSession.logged()){
            finish();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        activity_from = (getIntent().hasExtra("activity_from")) ? getIntent().getStringExtra("activity_from") : "";

        usernameEditText = (EditText) findViewById(R.id.usernameEditView);
        emailEditText = (EditText) findViewById(R.id.emailEditView);
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

        //facebook
        callbackManager = CallbackManager.Factory.create();
        fbLoginBtn = (LoginButton) findViewById(R.id.login_button);
        fbLoginBtn.setPermissions(Arrays.asList("email", "public_profile"));
        //google
        googleSignInButton = findViewById(R.id.sign_in_button);

        // Callback registration
        fbLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response)
                    {

                        if (object != null) {
                            try {
//                                String name = object.getString("name");
                                String email = object.getString("email");
                                String fbUserID = object.getString("id");
                                //use the above details for login or registration for your app
//                                        Toast.makeText(LoginActivity.this, "name: " + name + ",\n email: " + email + ", \n ID: " + fbUserID, Toast.LENGTH_LONG).show();
                                UsernameDialog usernameDialog = new UsernameDialog(RegisterActivity.this);
                                usernameDialog.setFeedBackReceiver(new UsernameDialog.OnFeedBack() {
                                    @Override
                                    public void onSubmit(String username) {
                                        ProgressDialog loader = ProgressDialog.show(RegisterActivity.this, "Loading", "Please wait...", true);
                                        try {
                                            tryRegisteringByType(fbUserID, "facebook", username, email, loader);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        usernameDialog.hide();
                                    }
                                });
                                usernameDialog.show();
                                disconnectFacebook();
                            }
                            catch (JSONException | NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                Bundle parameters = new Bundle(); parameters.putString("fields", "id, name, email, gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(RegisterActivity.this, "Login canceled!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
//                Log.e("FACEBOOK", exception.getMessage());
//                Toast.makeText(LoginActivity.this, "Login error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                Toast.makeText(RegisterActivity.this, "Login error", Toast.LENGTH_LONG).show();
            }
        });
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
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
        data.put("password", passwordEditText.getText().toString());
        data.put("replace_cart_user", userSession.userID);


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
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class).putExtra("from_login", "true"));
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
        postRequest.setShouldCache(false);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }
    public void tryRegisteringByType(String register_id, String register_type, String username, String email, ProgressDialog loader) throws JSONException {
        String url = Site.REGISTER;
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("email", email);
        data.put("register_id", register_id);
        data.put("register_type", register_type);

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
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class).putExtra("from_login", "true"));
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
        postRequest.setShouldCache(false);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    private void googleHandleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed eg successfully, show authenticated UI.
            updateUI(account);
            //sign out we don't want google continuous login
            mGoogleSignInClient.signOut();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
//            Log.w("GOOGLE ERROR:", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if(account != null) {
//            String name = account.getDisplayName();
            String email = account.getEmail();
            String personId = account.getId();

            UsernameDialog usernameDialog = new UsernameDialog(RegisterActivity.this);
            usernameDialog.setFeedBackReceiver(new UsernameDialog.OnFeedBack() {
                @Override
                public void onSubmit(String username) {
                    //            Toast.makeText(LoginActivity.this, "name: " + name + ",\n email: " + email + ", \n ID: " + personId, Toast.LENGTH_LONG).show();
                    ProgressDialog loader = ProgressDialog.show(RegisterActivity.this, "Loading", "Please wait...", true);
                    try {
                        tryRegisteringByType(personId, "google", username, email, loader);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    usernameDialog.hide();
                }
            });
            usernameDialog.show();

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userSession.logged()) {
            finish();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            googleHandleSignInResult(task);
        }
    }
    public void disconnectFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null,
                HttpMethod.DELETE, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse)
            {
                LoginManager.getInstance().logOut();
            }
        }).executeAsync();
    }


}