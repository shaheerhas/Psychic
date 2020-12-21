package com.example.psychic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
        messagesList.add(new Message("1","2","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("2","1","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("1","2","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("2","1","hi",System.currentTimeMillis()/1000));
        messagesList.add(new Message("1","2","hi",System.currentTimeMillis()/1000));

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
               // messagesList.add(new Message("1","2",messageContent.getText().toString(),System.currentTimeMillis()/1000));

                sendMessage();
                messageContent.setText("");

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

    private void sendMessage() {

        String userID = fuser.getUid();
        String msg = messageContent.getText().toString();
        messagesList.add(new Message("1","2",msg,System.currentTimeMillis()/1000));
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(adapter.getItemCount());
      /*  try {
            URL url = new URL("39.32.152.158:9000");
            String protocol = url.getProtocol();
            System.out.println(String.format("A::main: protocol = '%s'", protocol));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("msg", msg);

            Log.i("JSON", jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();

           // Log.i("STATUS", String.valueOf(conn.getResponseCode()));
          //  Log.i("MSG" , conn.getResponseMessage());
            String responseMsg = conn.getResponseMessage();
            messagesList.add(new Message("2","1",responseMsg,System.currentTimeMillis()/1000));
            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(adapter.getItemCount());
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }*/


        OkHttpClient okHttpClient = new OkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("39.32.152.158:9000")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        if (msg.trim().isEmpty()) {
            Toast.makeText(ChatActivity.this, "Empty message!", Toast.LENGTH_LONG).show();
        } else {
            messagesList.add(new Message(userID,"bot",msg,System.currentTimeMillis()/1000));
        }

      //  Toast.makeText(ChatActivity.this, ""+msg, Toast.LENGTH_LONG).show();

        Message userMessageSend = retrofit.create(Message.class);
        Call<List<Message>> response = userMessageSend.sendMessage(msg);
        response.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if(response.body() == null || response.body().size() == 0){
                    showTextView("Sorry didn't understand",BOT);
                }
                else{
                    BotResponse botResponse = response.body().get(0);
                    showTextView(botResponse.getText(),BOT);
                }
            }
            @Override
            public void onFailure(Call<List<BotResponse>> call, Throwable t) {
                showTextView("Waiting for message",BOT);
                Toast.makeText(MainActivity.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

}


}