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
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;


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
import com.facebook.appevents.AppEventsLogger;
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

public class LoginActivity extends AppCompatActivity {
    String activity_from;
    EditText usernameEmailView;
    EditText passwordView;
    Button loginBtn;
    Button goRegisterBtn;
    UserSession userSession;
    LoginButton fbLoginBtn;
    CallbackManager callbackManager;
    SignInButton googleSignInButton;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 222;

//    public void Get_hash_key() {
//        PackageInfo info;
//        try {
//            info = getPackageManager().getPackageInfo("com.dlink.pakstores", PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md;
//                md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                String something = new String(Base64.encode(md.digest(), 0));
//                //String something = new String(Base64.encodeBytes(md.digest()));
//                Log.e("hash key", something);
//            }
//        } catch (PackageManager.NameNotFoundException e1) {
//            Log.e("name not found", e1.toString());
//        } catch (NoSuchAlgorithmException e) {
//            Log.e("no such an algorithm", e.toString());
//        } catch (Exception e) {
//            Log.e("exception", e.toString());
//        }
//    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

//        Get_hash_key();

        AppEventsLogger.activateApp(getApplication());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Check for existing Google Sign In account, if the user is already signed eg
//        the GoogleSignInAccount will be non-null.
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        updateUI(account);

        activity_from = (getIntent().hasExtra("activity_from")) ? getIntent().getStringExtra("activity_from") : "";

        usernameEmailView = (EditText) findViewById(R.id.usernameEditView);
        passwordView = (EditText) findViewById(R.id.passwordEditView);
        loginBtn = (Button) findViewById(R.id.saveBtn);
        goRegisterBtn = (Button) findViewById(R.id.gotoLoginBtn);

        //facebook
        callbackManager = CallbackManager.Factory.create();
        fbLoginBtn = (LoginButton) findViewById(R.id.login_button);
        fbLoginBtn.setPermissions(Arrays.asList("email", "public_profile"));
        //google
        googleSignInButton = findViewById(R.id.sign_in_button);

        // If you are using eg a fragment, call loginButton.setFragment(this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean formPassed = true;
                if (isEmpty(usernameEmailView)) {
                    usernameEmailView.setError("Username or Email required!");
                    usernameEmailView.requestFocus();
                    formPassed = false;
                }
                if (isEmpty(passwordView)) {
                    passwordView.setError("Password required!");
                    passwordView.requestFocus();
                    formPassed = false;
                }
                if (formPassed) {
                    ProgressDialog loader = ProgressDialog.show(LoginActivity.this, "Loading", "Please wait...", true);
                    try {
                        fetchLogin(usernameEmailView.getText().toString(), passwordView.getText().toString(), loader);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Button resetPassword = (Button) findViewById(R.id.reset_password_btn);
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = Site.ADDRESS + "my-account/lost-password/?in_sk_app=1";
                Intent intent = new Intent(LoginActivity.this, BrowserActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("title", "Reset Password");
                startActivity(intent);
            }
        });

        goRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)
                .putExtra("activity_from", activity_from));
            }
        });

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
//                                        String name = object.getString("name");
//                                        String email = object.getString("email");
                                        String fbUserID = object.getString("id");
                                        //use the above details for login or registration for your app
//                                        Toast.makeText(LoginActivity.this, "name: " + name + ",\n email: " + email + ", \n ID: " + fbUserID, Toast.LENGTH_LONG).show();
                                        ProgressDialog loader = ProgressDialog.show(LoginActivity.this, "Loading", "Please wait...", true);
                                        try {
                                            fetchLoginByType(fbUserID, "facebook", loader);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

//
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
                Toast.makeText(LoginActivity.this, "Login canceled!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
//                Log.e("FACEBOOK", exception.getMessage());
//                Toast.makeText(LoginActivity.this, "Login error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                Toast.makeText(LoginActivity.this, "Login error", Toast.LENGTH_LONG).show();
            }
        });
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        switch (item.getItemId()) {
            case android.R.id.home:
                finish(); // close this activity and return to preview activity (if there is any)
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    public void fetchLogin(String usernameEmail, String password, ProgressDialog loader) throws JSONException {
        String url = Site.LOGIN;
        JSONObject data = new JSONObject();
        data.put("username", usernameEmail);
        data.put("password", password);
        data.put("replace_cart_user", userSession.userID);
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
                        parseJSONData(response.toString(), loader);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(LoginActivity.this, "Authentication denied!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();
                loader.hide();
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(this.getApplicationContext());
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }
    public void fetchLoginByType(String login_id, String login_type, ProgressDialog loader) throws JSONException {
//        Log.e("LOGIN_ID", login_id);
        String url = Site.LOGIN;
        JSONObject data = new JSONObject();
        data.put("login_id", login_id);
        data.put("login_type", login_type);
        data.put("replace_cart_user", userSession.userID);
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
                        parseJSONData(response.toString(), loader);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(LoginActivity.this, "Authentication denied!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();
                loader.hide();
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(this.getApplicationContext());
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }
    public void parseJSONData(String json, ProgressDialog loader) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.has("code") || object.getString("data").equals("null")) {
                Toast toast = Toast.makeText(LoginActivity.this, "Incorrect login details!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();
                loader.hide();
            } else {
                JSONObject data = new JSONObject(object.getString("data"));
                UserSession userSession = new UserSession(LoginActivity.this.getApplicationContext());
                userSession.createLoginSession(data.getString("ID"), data.getString("user_login"), data.getString("user_email"), true);
                if (userSession.logged()) loader.hide();
                switch (activity_from) {
                    case "checkout":
                        startActivity(new Intent(LoginActivity.this, CheckoutActivity.class));
                        break;
                    case "address":
                        startActivity(new Intent(LoginActivity.this, AddressActivity.class));
                        break;
                    default:
                        Toast toast = Toast.makeText(LoginActivity.this, "Successfully!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                        toast.show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class).putExtra("from_login", "true"));
                        loader.hide();
                        break;
                }
            }
//
//            UserSession userSession = new UserSession(context.getApplicationContext());
//            if (userSession.userID.equals("0") && object.getString("user_cart_exists").equals("false")) {
////                save the generated user id to session
//                userSession.createLoginSession(object.getString("user"),"", "", false);
//            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        loader.hide();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        super.onBackPressed();
    }

    public boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
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
            Log.w("GOOGLE ERROR:", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if(account != null) {
//            String name = account.getDisplayName();
//            String email = account.getEmail();
            String personId = account.getId();

//            Log.e("GOOGLE ID", personId);
//            Toast.makeText(LoginActivity.this, "name: " + name + ",\n email: " + email + ", \n ID: " + personId, Toast.LENGTH_LONG).show();
            ProgressDialog loader = ProgressDialog.show(LoginActivity.this, "Loading", "Please wait...", true);
            try {
                fetchLoginByType(personId, "google", loader);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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