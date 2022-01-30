package com.myshirt.eg.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

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
import com.myshirt.eg.MainActivity;
import com.myshirt.eg.MapsActivity;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.adapter.CountriesAdapter;
import com.myshirt.eg.adapter.CountryLists;
import com.myshirt.eg.adapter.CountryStates;
import com.myshirt.eg.adapter.StateLists;
import com.myshirt.eg.adapter.StatesAdapter;
import com.myshirt.eg.handler.UserSession;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class AddressActivity extends AppCompatActivity {
    UserSession userSession;
    ProgressDialog loader;
    EditText fname, lname, company, address_1, address_2, city, postcode, phone, email;
    AutoCompleteTextView country, state;
    Button saveBtn;
    List<CountryLists> countryLists;
    List<StateLists> stateLists;
    CountriesAdapter countriesAdapter;
    StatesAdapter statesAdapter;
    Button selectMap;
    private static final int MY_MAP_ADDRESS_REQUEST_CODE = 109;
//    String selectedCountry = "", selectedState = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userSession = new UserSession(this.getApplicationContext());

        fname = (EditText) findViewById(R.id.fname);
        lname = (EditText) findViewById(R.id.lname);
        company = (EditText) findViewById(R.id.company);
        country = (AutoCompleteTextView) findViewById(R.id.country);
        address_1 = (EditText) findViewById(R.id.address_1);
        address_2 = (EditText) findViewById(R.id.address_2);
        state = (AutoCompleteTextView) findViewById(R.id.state);
        phone = (EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);
        city = (EditText) findViewById(R.id.city);
        postcode = (EditText) findViewById(R.id.postcode);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        selectMap = (Button) findViewById(R.id.selectMapBtn);


        //country
        countryLists = new ArrayList<>();
        countriesAdapter = new CountriesAdapter(this, R.layout.auto_complete_row, countryLists);
        country.setThreshold(1); //number of character user must type before suggestion
        country.setAdapter(countriesAdapter);

        //state
        stateLists = new ArrayList<>();
        statesAdapter = new StatesAdapter(this, R.layout.auto_complete_row, getCountryStates("", stateLists));
        state.setThreshold(1); //number of characters user must type before suggestion
        state.setAdapter(statesAdapter);

        selectMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddressActivity.this, MapsActivity.class);
                startActivityForResult(intent, MY_MAP_ADDRESS_REQUEST_CODE);
            }
        });

        country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String theCountry = ((CountryLists) adapterView.getItemAtPosition(i)).getCode();
                state.setText("");
                state.setAdapter(new StatesAdapter(AddressActivity.this, R.layout.auto_complete_row, getCountryStates(theCountry, stateLists)));
            }
        });
//        state.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                selectedState = ((CountryStates) adapterView.getItemAtPosition(i)).getCode();
//            }
//        });



        saveBtn.setOnClickListener(saveListener);

        //check for cart is not empty
        loader = ProgressDialog.show(this, "Loading", "Please wait...", true);
        fetchCartData(this);

        if (userSession.logged())
            getToken();

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

    public void fetchCartData(Context context) {
        String url = Site.CART + userSession.userID;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseCartData(response);
            }
        }, (VolleyError error) -> {
            //handle error
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Alert");
            builder.setMessage("Unable to collect cart.");
            builder.setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //refresh this activity
                    startActivity(getIntent());
                    finish();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //go back to cart activity
                    startActivity(new Intent(AddressActivity.this, MainActivity.class).putExtra("selected_tab", "cart"));
                }
            });

            AlertDialog dialog = builder.create();
            loader.hide();
            dialog.show();
        });
        RequestQueue rQueue = Volley.newRequestQueue(context);
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }

    @SuppressLint("SetTextI18n")
    public void parseCartData(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (object.has("items")) {
                JSONArray array = object.getJSONArray("items");
                if (array.length() > 0) {
                    //cart items is not empty
                    setFormValues();
                } else {
                    //cart items is empty
                    cartItemIsEmpty();
                }
            } else {
                //cart items is empty
                cartItemIsEmpty();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //when fetching is ready
    }

    public void setFormValues() {
//        fetch user data and set form if the user have the fields
        loader.show();
        String url = Site.USER + userSession.userID + "?with_regions=1";
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject object = null;
                try {
                    object = new JSONObject(response);
                    JSONObject address = new JSONObject(object.getString("shipping_address"));
                    fname.setText(address.getString("shipping_first_name"));
                    lname.setText(address.getString("shipping_last_name"));
                    company.setText(address.getString("shipping_company"));
                    address_1.setText(address.getString("shipping_address_1"));
                    address_2.setText(address.getString("shipping_address_2"));
                    city.setText(address.getString("shipping_city"));
                    postcode.setText(address.getString("shipping_postcode"));
                    country.setText(address.getString("shipping_country"));
                    state.setText(address.getString("shipping_state"));
                    phone.setText(address.getString("shipping_phone"));
                    email.setText(address.getString("shipping_email"));

                    //setup regions and states
                    JSONObject regions = new JSONObject(object.getString("regions"));
                    JSONObject countriesObject = new JSONObject(regions.getString("countries"));
                    JSONObject statesObject = new JSONObject(regions.getString("states"));

                    countryLists = new ArrayList<>();
                    stateLists = new ArrayList<>();
                    //countries loop
                    Iterator<String> countryKeys = countriesObject.keys();
                    while (countryKeys.hasNext()) {
                        String key = countryKeys.next();
                        //add countries to autocomplete adapter;
                        countryLists.add(new CountryLists(key, countriesObject.getString(key)));
                    }

                    //states loop
                    Iterator<String> stateKeys = statesObject.keys();
                    while (stateKeys.hasNext()) {
                        String key = stateKeys.next();
//                        //get all country states of the country key
                        List<CountryStates> countryStates = new ArrayList<>();
                        Object countryStatesObject = statesObject.get(key);
                        if (countryStatesObject instanceof JSONObject) {
                            Iterator<String> countryStateKeys = ((JSONObject) countryStatesObject).keys();
                            while (countryStateKeys.hasNext()) {
                                String stateKey = countryStateKeys.next();
                                countryStates.add(new CountryStates(((JSONObject) countryStatesObject).getString(stateKey)));
                            }
                        }

//                        //add states to autocomplete adapter;
                        stateLists.add(new StateLists(key, countryStates));
                    }

                    countriesAdapter = new CountriesAdapter(AddressActivity.this, R.layout.auto_complete_row, countryLists);
                    country.setAdapter(countriesAdapter);
                    statesAdapter = new StatesAdapter(AddressActivity.this, R.layout.auto_complete_row, getCountryStates("PK", stateLists));
                    state.setAdapter(statesAdapter);
                    loader.hide();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loader.hide();
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(AddressActivity.this, "Can't fetch your address, Try to go back.", Toast.LENGTH_LONG).show();
            loader.hide();
        });
        RequestQueue rQueue = Volley.newRequestQueue(AddressActivity.this);
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
    }

    public List<CountryStates> getCountryStates(String countryCode, List<StateLists> stateLists) {
        for (StateLists countryStates : stateLists) {
            if (countryStates.getCountryCode().equals(countryCode))
                return countryStates.getStates();
        }
        return null;
    }


    public void cartItemIsEmpty() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Oops!");
        builder.setMessage("Your Cart is empty. Please go back and select product to buy.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //go back to shop
                startActivity(new Intent(AddressActivity.this, MainActivity.class).putExtra("selected_tab", "cart"));
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        loader.hide();
        dialog.show();
    }

    public View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean formPassed = true;
            if (isEmpty(fname)) {
                fname.setError("First name required!");
                fname.requestFocus();
                formPassed = false;
            }
            if (isEmpty(lname)) {
                lname.setError("Last name required!");
                lname.requestFocus();
                formPassed = false;
            }
            if (isEmpty(country)) {
                country.setError("Country required!");
                country.requestFocus();
                formPassed = false;
            }
            if (isEmpty(address_1)) {
                address_1.setError("Address required!");
                address_1.requestFocus();
                formPassed = false;
            }
            if (isEmpty(state)) {
                state.setError("State required!");
                state.requestFocus();
                formPassed = false;
            }
            if (isEmpty(city)) {
                city.setError("City required!");
                city.requestFocus();
                formPassed = false;
            }
            if (isEmpty(postcode)) {
                postcode.setError("Postcode required!");
                postcode.requestFocus();
                formPassed = false;
            }
            if (isEmpty(phone)) {
                phone.setError("Phone required!");
                phone.requestFocus();
                formPassed = false;
            }
            if (!isEmail(email) || isEmpty(email)) {
                email.setError("Enter valid email!");
                email.requestFocus();
                formPassed = false;
            }

            if (formPassed) {
                //submit data
                try {
                    submitDetails();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(AddressActivity.this, "Please fill eg required fields!", Toast.LENGTH_LONG).show();
            }

        }
    };

    public boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

    boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public void submitDetails() throws JSONException {
        String url = Site.UPDATE_SHIPPING + userSession.userID;
        JSONObject data = new JSONObject();
        data.put("first_name", fname.getText().toString());
        data.put("last_name", lname.getText().toString());
        data.put("company", company.getText().toString());
        data.put("country", country.getText().toString());
        data.put("state", state.getText().toString());
        data.put("city", city.getText().toString());
        data.put("postcode", postcode.getText().toString());
        data.put("address_1", address_1.getText().toString());
        data.put("address_2", address_2.getText().toString());
        data.put("email", email.getText().toString());
        data.put("phone", phone.getText().toString());
//        data.put("selected_country", selectedCountry);
//        data.put("selected_state", selectedState);

        loader.show();
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
//                        Log.e("res:", response.toString());
                        //savings result
                        try {
                            JSONObject object = new JSONObject(response.toString());
                            if (object.getString("code").equals("saved")) {
                                loader.hide();
                                startActivity(new Intent(AddressActivity.this, PaymentActivity.class));
                                finish();
                            } else {
                                //not saved
                                Toast toast = Toast.makeText(AddressActivity.this, "Address not saved!", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                                toast.show();
                                loader.hide();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(AddressActivity.this, "Unable to save address.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
                toast.show();
                loader.hide();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(AddressActivity.this);
        postRequest.setShouldCache(false);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check that it is the MapsActivity with an OK result
        if (requestCode == MY_MAP_ADDRESS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get String data from Intent
                String addressLineExtra = data.getStringExtra("address_line");
                String countryExtra = data.getStringExtra("country");
                String stateExtra = data.getStringExtra("state");
                String cityExtra = data.getStringExtra("city");
                String postcodeExtra = data.getStringExtra("postcode");
                address_1.setText(addressLineExtra);
                country.setText(countryExtra);
                state.setText(stateExtra);
                city.setText(cityExtra);
                postcode.setText(postcodeExtra);
            }
        }
    }



    private void getToken() {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                //If task is failed then
                if (task.isSuccessful()) {
                    //Token
                    String token = task.getResult();
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

                    RequestQueue rQueue = Volley.newRequestQueue(AddressActivity.this);
                    postRequest.setShouldCache(false);
                    postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    rQueue.add(postRequest);
                }


            }
        });
    }
}