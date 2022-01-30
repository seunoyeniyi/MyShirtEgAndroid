package com.myshirt.eg.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.myshirt.eg.R;
import com.myshirt.eg.Site;
import com.myshirt.eg.handler.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Objects;

public class ProfileAddressActivity extends AppCompatActivity {

    UserSession userSession;
    ProgressDialog loader;
    EditText fname, lname, company, gender, dob, country, address_1, address_2, state, city, postcode, phone, other_phone, email;
    Button saveBtn;
    Switch notificationSwitch;
  

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_address);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userSession = new UserSession(getApplicationContext());

        fname = (EditText) findViewById(R.id.fname);
        lname = (EditText) findViewById(R.id.lname);
        company = (EditText) findViewById(R.id.company);
        gender = (EditText) findViewById(R.id.gender);
        dob = (EditText) findViewById(R.id.dob);
        country = (EditText) findViewById(R.id.country);
        address_1 = (EditText) findViewById(R.id.address_1);
        address_2 = (EditText) findViewById(R.id.address_2);
        state = (EditText) findViewById(R.id.state);
        phone = (EditText) findViewById(R.id.phone);
        other_phone = (EditText) findViewById(R.id.other_phone);
        email = (EditText) findViewById(R.id.email);
        city = (EditText) findViewById(R.id.city);
        postcode = (EditText) findViewById(R.id.postcode);
        saveBtn = (Button) findViewById(R.id.saveBtn);

        dob.addTextChangedListener(tw);

        notificationSwitch = (Switch) findViewById(R.id.notification_switch);
        if (userSession.notification_on()) {
            notificationSwitch.setChecked(true);
        } else {
            notificationSwitch.setChecked(false);
        }


        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                userSession.on_notification(b);
            }
        });

        saveBtn.setOnClickListener(saveListener);

        //check for cart is not empty
        loader = ProgressDialog.show(this, "Loading", "Please wait...", true);
        setFormValues();
        
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
                    gender.setText(object.getString("gender"));
                    dob.setText(object.getString("birthday"));
                    address_1.setText(address.getString("shipping_address_1"));
                    address_2.setText(address.getString("shipping_address_2"));
                    city.setText(address.getString("shipping_city"));
                    state.setText(address.getString("shipping_state"));
                    postcode.setText(address.getString("shipping_postcode"));
                    country.setText(address.getString("shipping_country"));
                    phone.setText(address.getString("shipping_phone"));
                    other_phone.setText(object.getString("other_phone"));
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
        RequestQueue rQueue = Volley.newRequestQueue(this);
        request.setShouldCache(false);
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
        data.put("gender", gender.getText().toString());
        data.put("birthday", dob.getText().toString());
        data.put("country", country.getText().toString());
        data.put("state", state.getText().toString());
        data.put("city", city.getText().toString());
        data.put("postcode", postcode.getText().toString());
        data.put("address_1", address_1.getText().toString());
        data.put("address_2", address_2.getText().toString());
        data.put("email", email.getText().toString());
        data.put("phone", phone.getText().toString());
        data.put("other_phone", other_phone.getText().toString());

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

        RequestQueue rQueue = Volley.newRequestQueue(this);
        postRequest.setShouldCache(false);
        postRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue.add(postRequest);
    }

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

    TextWatcher tw = new TextWatcher() {
        private String current = "";
        private String ddmmyyyy = "DDMMYYYY";
        private Calendar cal = Calendar.getInstance();

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!s.toString().equals(current)) {
                String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                int cl = clean.length();
                int sel = cl;
                for (int i = 2; i <= cl && i < 6; i += 2) {
                    sel++;
                }
                //Fix for pressing delete next to a forward slash
                if (clean.equals(cleanC)) sel--;

                if (clean.length() < 8){
                    clean = clean + ddmmyyyy.substring(clean.length());
                }else{
                    //This part makes sure that when we finish entering numbers
                    //the date is correct, fixing it otherwise
                    int day  = Integer.parseInt(clean.substring(0,2));
                    int mon  = Integer.parseInt(clean.substring(2,4));
                    int year = Integer.parseInt(clean.substring(4,8));

                    mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                    cal.set(Calendar.MONTH, mon-1);
                    year = (year<1900)?1900:(year>2100)?2100:year;
                    cal.set(Calendar.YEAR, year);
                    // ^ first set year for the line below to work correctly
                    //with leap years - otherwise, date e.g. 29/02/2012
                    //would be automatically corrected to 28/02/2012

                    day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                    clean = String.format("%02d%02d%02d",day, mon, year);
                }

                clean = String.format("%s/%s/%s", clean.substring(0, 2),
                        clean.substring(2, 4),
                        clean.substring(4, 8));

                sel = sel < 0 ? 0 : sel;
                current = clean;
                dob.setText(current);
                dob.setSelection(sel < current.length() ? sel : current.length());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {}
    };
}