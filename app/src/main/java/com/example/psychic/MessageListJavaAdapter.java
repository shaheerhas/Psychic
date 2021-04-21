package com.example.psychic;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListJavaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int MESSAGE_TYPE_IN = 1;
    private final Context context;
    ArrayList<Message> list;
    String rid;
    String sid;
    //   public static final int MESSAGE_TYPE_OUT = 2;

    public MessageListJavaAdapter(Context context, ArrayList<Message> list, String rid, String sid) { // you can pass other parameters in constructor
        this.context = context;
        this.sid = sid;
        this.list = list;
        this.rid = rid;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_TYPE_IN) {
            return new MessageInViewHolder(LayoutInflater.from(context).inflate(R.layout.message_received, parent, false));
        } else {
            return new MessageOutViewHolder(LayoutInflater.from(context).inflate(R.layout.message_sent, parent, false));
        }
    }

    //function to check if string contains url
    private boolean containsURL(String input) {
        final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
        Pattern p = Pattern.compile(URL_REGEX);
        Matcher m = p.matcher(input);//replace with string to compare
        return m.find();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        //only for the server messages (for now)
        if (list.get(position).senderId.equals(rid)) {
            ((MessageInViewHolder) holder).bind(position);
            //copy link for youtube
            ((MessageInViewHolder) holder).messageTV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    String msg = ((MessageInViewHolder) holder).messageTV.getText().toString().trim();
                    String[] msgs = msg.split("\n");
                    for (int i = 0; i < msgs.length; i++) {
                        if (containsURL(msgs[i])) {
                            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("label", msgs[i].replace("\n", ""));
                            if (clipboard == null || clip == null) return false;
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                            clipboard.setPrimaryClip(clip);
                        }
                    }
                    return false;
                }
            });
        } else {
            ((MessageOutViewHolder) holder).bind(position);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).senderId.equals(rid)) {
            return 1;
        } else {
            return 2;
        }
//        else if(list.get(position).senderId.equals(sid)&& list.get(position).containsImage.equals(false)){
//            return 2;
//        }
    }

    private class MessageInViewHolder extends RecyclerView.ViewHolder {

        TextView messageTV;

        MessageInViewHolder(final View itemView) {
            super(itemView);
            messageTV = itemView.findViewById(R.id.text_received);

        }

        void bind(int position) {
            Message message = list.get(position);
            messageTV.setText(message.text);

        }
    }

    private class MessageOutViewHolder extends RecyclerView.ViewHolder {

        TextView messageTV;

        MessageOutViewHolder(final View itemView) {
            super(itemView);
            messageTV = itemView.findViewById(R.id.text_sent);
        }

        void bind(int position) {
            Message message = list.get(position);
            messageTV.setText(message.text);
        }
    }
}
