package com.example.psychic;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ChatActivity extends AppCompatActivity {


    // server address for chat history api/users/history/user_hash/

    public FirebaseAuth mAuth;
    TextView username;
    FirebaseUser fuser;
    /*    DatabaseReference reference;
        ImageButton btn_send;
        EditText text_send;
        CircleImageView profile_image;
        Intent intent;

        FirebaseDatabase database;
        DatabaseReference reference2;
        Uri selectedImage = null;*/
    MessageListJavaAdapter adapter;
    ImageButton send, selectImage, clearImage;
    View imagePreviewBackground;
    EditText messageContent;
    RecyclerView recyclerView;
    ImageButton backButton;
    TextView appbar_heading;
    ArrayList<Message> messagesList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent data = getIntent();
        String id = data.getStringExtra("id");

        //String senderId= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        send = findViewById(R.id.send_button);
        selectImage = findViewById(R.id.select_image);
        messageContent = findViewById(R.id.message_content);
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


        username = findViewById(R.id.username);
     /*   messagesList.add(new Message("1","2","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("2","1","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("1","2","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("2","1","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("1","2","hi",System.currentTimeMillis()/1000));*/

        // populate messages from DB in the messages list
        GetMessageList();
        adapter = new MessageListJavaAdapter(this, messagesList, "2", "1");
        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);


        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(adapter.getItemCount());


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
// send message from text dialogue
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageContent.getText().toString().equals("")) {
                    Toast.makeText(ChatActivity.this, "Empty Message!", Toast.LENGTH_SHORT).show();
                } else {
                    String msg = messageContent.getText().toString();
                    long timestamp = System.currentTimeMillis() / 1000;
                    messagesList.add(new Message("1", "2", msg, timestamp));
                    if (messageContent.length() > 0) {
                        TextKeyListener.clear(messageContent.getText());
                    }
                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(adapter.getItemCount());
                    PostMessage(msg, timestamp);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            //user logs out
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChatActivity.this, StartActivity.class));
                finish();
                return true;
        }
        return false;
    }


    void PostMessage(final String msg, final Long timestamp) {

        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = StartActivity.serverAddress + "/api/users/message/";
        final String userID = fuser.getUid();
        Log.d("UID", userID);
        JSONObject postparams = new JSONObject();
        try {

            postparams.put("user", userID);
            postparams.put("msg_text", msg);
            postparams.put("timestamp", timestamp);
            postparams.put("is_bot", false);
            Log.d("TRY", postparams.toString());
        } catch (JSONException e) {
            e.printStackTrace();

        }
        JsonObjectRequest MyJsonRequest = new JsonObjectRequest(Request.Method.POST, url, postparams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    Log.d("RESSS", response.get("msg_text").toString() + "RES");
                    messagesList.add(new
                            Message("2", "1", response.get("msg_text").toString(),
                            Long.parseLong(response.get("timestamp").toString())));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("CATCH", e.getMessage());
                }
                Log.d("CATCH", "RES");
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
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


    Boolean GetMessageList() {

        final Boolean[] messagesAdded = {false};
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = StartActivity.serverAddress + "/api/users/message/";

        JsonArrayRequest MyJsonRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() > 0)
                    messagesAdded[0] = true;

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject message = response.getJSONObject(i);
                        String msgText = message.get("msg_text").toString();
                        Log.d("GETMESSAGES", message.get("msg_text").toString());
                        Long timeStamp = Long.parseLong(message.get("timestamp").toString());
                        Boolean isBot = Boolean.parseBoolean(message.get("is_bot").toString());
                        if (isBot)
                            messagesList.add(new Message("2", "1", msgText, timeStamp));
                        else
                            messagesList.add(new Message("1", "2", msgText, timeStamp));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        return messagesAdded[0];
    }


}