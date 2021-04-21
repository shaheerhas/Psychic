package com.example.psychic;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText username, email, password;
    Button btn_register;
    FirebaseAuth auth;
    DatabaseReference reference;
    FirebaseUser fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set ids to xml things
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        btn_register = findViewById(R.id.btn_register);
        auth = FirebaseAuth.getInstance();
        //reference = FirebaseDatabase.getInstance();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(txt_email)) {
                    Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                } else {
                    //registers user on Firebase and on Django server
                    register(txt_username, txt_email, txt_password);

                }
            }
        });

    }


    void PostUser(final String fuser) {

        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = StartActivity.serverAddress + "/api/users/register/";
        final String userID = fuser;
        Log.d("UID", userID);
        JSONObject postparams = new JSONObject();
        try {
            Log.d("UID", userID);
            postparams.put("user_hash", userID);

        } catch (JSONException e) {
            //Toast t = new Toast();
            e.printStackTrace();
        }
        JsonObjectRequest MyJsonRequest = new JsonObjectRequest(Request.Method.POST, url, postparams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {


                    if (!response.isNull("user_hash")) {
                        Log.d("response", response.get("user_hash").toString());
                        Toast.makeText(RegisterActivity.this, "User Registered on Server", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(RegisterActivity.this, "User couldn't be registered on server", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        MyRequestQueue.add(MyJsonRequest);
        //set timeout to 15 seconds
        MyJsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }

    // sign up user
    private void register(final String username, final String email, final String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser fireBaseUser = auth.getCurrentUser();
                            assert fireBaseUser != null;
                            String userid = fireBaseUser.getUid();

                            PostUser(userid);
                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                            Log.d("USERID", userid);
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username);
                            hashMap.put("imageURL", "default");

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(RegisterActivity.this, "You cannot register with Email or Password", Toast.LENGTH_SHORT).show();


                        }
                    }
                });

    }
}