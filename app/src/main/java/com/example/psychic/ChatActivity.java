package com.example.psychic;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;


public class ChatActivity extends AppCompatActivity {

    TextView username;
    FirebaseUser fuser;
    DatabaseReference reference;
    ImageButton btn_send;
    EditText text_send;
    CircleImageView profile_image;
    Intent intent;


    private FirebaseAuth mAuth;
    RecyclerView recyclerView;
    FirebaseDatabase database;
    DatabaseReference reference2;
    MessageListJavaAdapter adapter;
    ImageButton send,selectImage,clearImage;
    View imagePreviewBackground;
    EditText messageContent;
    Uri selectedImage=null;
    ImageButton backButton;
    TextView appbar_heading;

    // Populate dummy messages in List, you can implement your code here
    ArrayList<Message> messagesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent data=getIntent();
        String id=data.getStringExtra("id");

        //String senderId= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        send=findViewById(R.id.send_button);
        selectImage=findViewById(R.id.select_image);
        messageContent=findViewById(R.id.message_content);
        backButton = findViewById(R.id.backButton);
        clearImage = findViewById(R.id.clear_image);
        imagePreviewBackground = findViewById(R.id.image_preview_bg);
        appbar_heading = findViewById(R.id.appbar_heading);

        //get current user's ID
        mAuth = FirebaseAuth.getInstance();
        fuser = mAuth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


//        adapter.notifyDataSetChanged();
//        recyclerView.smoothScrollToPosition(adapter.getItemCount());
        username = findViewById(R.id.username);
     /*   messagesList.add(new Message("1","2","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("2","1","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("1","2","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("2","1","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("1","2","hi",System.currentTimeMillis()/1000));*/

     //things you do to make the code work or like for love or whatever
     // btw, this just tells the os that all network related requests are not being burdened on the main thread
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        //for android 8 or greater, this has to be done for some freaking security update
        try {
            // Google Play will install latest OpenSSL
            ProviderInstaller.installIfNeeded(getApplicationContext());
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException
                | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }



        adapter = new MessageListJavaAdapter(this, messagesList,"2","1");
        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (messageContent.getText().toString().equals("")) {
                    Toast.makeText(ChatActivity.this,"Empty Message!",Toast.LENGTH_SHORT).show();
                }else{

                    String msg = messageContent.getText().toString();
                    messagesList.add(new Message("1","2",msg,System.currentTimeMillis()/1000));
                    messageContent.setText("");
                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(adapter.getItemCount());
                    PostUser(msg);


                }

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            //user logs out
            case R.id.logout:
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ChatActivity.this,StartActivity.class));
            finish();
            return true;
        }
        return false;
    }


    void PostUser(final String msg){

        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "http://39.32.152.158:9000";
        final String userID = fuser.getUid();
        Log.d("UID",userID);
        JSONObject postparams = new JSONObject();
        try {
            postparams.put("sender",userID);
            postparams.put("message", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
       // StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
        JsonObjectRequest MyJsonRequest = new JsonObjectRequest(Request.Method.POST,url,postparams,new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    Log.d("response",response.get("text").toString());
                    messagesList.add(new
                            Message("2","1",response.get("text").toString(),
                            System.currentTimeMillis()/1000));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MyRequestQueue.add(MyJsonRequest);
        MyJsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


}