package com.example.inclass08_09.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inclass08_09.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private static final int MSG_LEFT = 0;
    private static final int MSG_RIGHT = 1;


    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private ArrayList<Chat> messages;




    public ChatsAdapter() {
    }

    public ChatsAdapter(ArrayList<Chat> messages) {
        this.messages = messages;


    }

    public ArrayList<Chat> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Chat> messages) {
        this.messages = messages;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView textViewMessage;

        private ImageView chatImage;

        public ImageView getChatImage() {
            return chatImage;
        }

        public void setChatImage(ImageView chatImage) {
            this.chatImage = chatImage;
        }

        public TextView getTextViewMessage() {
            return textViewMessage;
        }


        public ViewHolder(@NonNull View itemView){
            super(itemView);

            textViewMessage = itemView.findViewById(R.id.textView_message);

            chatImage = itemView.findViewById(R.id.imageView_chatImage);
        }


    }

    @Override
    public int getItemViewType(int position){
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (messages.get(position).getSender().equals(mUser.getEmail())){
            return MSG_RIGHT;
        } else{
            return MSG_LEFT;
        }
    }

    @NonNull
    @Override
    public ChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_LEFT){
            View view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.message_left,parent, false);

            return new ViewHolder(view);
        } else{
            View view = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.message_right,parent, false);

            return new ViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull ChatsAdapter.ViewHolder holder, int position) {

        Chat chat = messages.get(position);

        holder.textViewMessage.setText(chat.getMessage());


        Glide.with(holder.itemView.getContext())
                .load(chat.getImageURL())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.chatImage);


    }

    @Override
    public int getItemCount() {
        if (messages != null) {
            return messages.size();
        } else {
            return 0;
        }
    }


}