package com.example.psychic;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MessageListJavaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    ArrayList<Message> list;
    String rid;
    String sid;
    public static final int MESSAGE_TYPE_IN = 1;
    public static final int MESSAGE_TYPE_OUT = 2;

    public MessageListJavaAdapter(Context context, ArrayList<Message> list, String rid, String sid) { // you can pass other parameters in constructor
        this.context = context;
        this.sid=sid;
        this.list=list;
        this.rid=rid;
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


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_TYPE_IN) {
            return new MessageInViewHolder(LayoutInflater.from(context).inflate(R.layout.message_received, parent, false));
        }
        else{
            return new MessageOutViewHolder(LayoutInflater.from(context).inflate(R.layout.message_sent, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (list.get(position).senderId.equals(rid)) {
            ((MessageInViewHolder) holder).bind(position);
        }
        else{
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
        }
        else {
            return 2;
        }
//        else if(list.get(position).senderId.equals(sid)&& list.get(position).containsImage.equals(false)){
//            return 2;
//        }
    }
}
