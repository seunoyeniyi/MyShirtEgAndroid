package com.fatima.fabric.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.MenuItem;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fatima.fabric.R;
import com.fatima.fabric.Site;
import com.fatima.fabric.handler.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ProfileAddressActivity extends AppCompatActivity {

    UserSession userSession;
    ProgressDialog loader;
    EditText fname, lname, company, country, address_1, address_2, state, city, postcode, phone, email;
    Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_address);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Address");

        userSession = new UserSession(this.getApplicationContext());

        fname = (EditText) findViewById(R.id.fname);
        lname = (EditText) findViewById(R.id.lname);
        company = (EditText) findViewById(R.id.company);
        country = (EditText) findViewById(R.id.country);
        address_1 = (EditText) findViewById(R.id.address_1);
        address_2 = (EditText) findViewById(R.id.address_2);
        state = (EditText) findViewById(R.id.state);
        phone = (EditText) findViewById(R.id.phone);
        email = (EditText) findViewById(R.id.email);
        city = (EditText) findViewById(R.id.city);
        postcode = (EditText) findViewById(R.id.postcode);
        saveBtn = (Button) findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(saveListener);

        //check for cart is not empty
        loader = ProgressDialog.show(this, "Loading", "Please wait...", true);
        setFormValues();


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void setFormValues() {
//        fetch user data and set form if the user have the fields
        loader.show();
        String url = Site.USER + userSession.userID;
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
                    state.setText(address.getString("shipping_state"));
                    postcode.setText(address.getString("shipping_postcode"));
                    country.setText(address.getString("shipping_country"));
                    phone.setText(address.getString("shipping_phone"));
                    email.setText(address.getString("shipping_email"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loader.hide();
            }
        }, (VolleyError error) -> {
            //handle error
            Toast.makeText(ProfileAddressActivity.this, "Can't fetch your address, Try to go back.", Toast.LENGTH_LONG).show();
            loader.hide();
        });
        RequestQueue rQueue = Volley.newRequestQueue(ProfileAddressActivity.this);
        request.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(request);
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
                Toast.makeText(ProfileAddressActivity.this, "Please fill eg required fields!", Toast.LENGTH_LONG).show();
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
    public void submitDetails() throws  JSONException {
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

        loader.show();
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) { //response.toString() to get json as string
                        //savings result
                        try {
                            JSONObject object = new JSONObject(response.toString());
                            if (object.getString("code").equals("saved")) {
                                Toast toast = Toast.makeText(ProfileAddressActivity.this, "Address saved!", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                                toast.show();
                                loader.hide();
                            } else {
                                //not saved
                                Toast toast = Toast.makeText(ProfileAddressActivity.this, "Address not saved!", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
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
                Toast toast = Toast.makeText(ProfileAddressActivity.this, "Unable to save address.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
                toast.show();
                loader.hide();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(ProfileAddressActivity.this);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }
}