package com.example.inclass08_09.model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inclass08_09.R;

import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private ArrayList<Friend> friends;

    private IfriendsListRecyclerAction mListener;

    public FriendAdapter() {
    }

    public FriendAdapter(ArrayList<Friend> friends, Context context) {
        Log.d("demo", "FriendAdapter: context "+context);
        this.friends = friends;
        if(context instanceof IfriendsListRecyclerAction){
            this.mListener = (IfriendsListRecyclerAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IeditButtonAction");
        }

    }

    public ArrayList<Friend> getFriends() {
        return friends;
    }

    public void setUsers(ArrayList<Friend> friends) {
        this.friends = friends;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView textViewName, textViewEmail;
        private Button buttonChat;


        public TextView getTextViewName() {
            return textViewName;
        }

        public TextView getTextViewEmail() {
            return textViewEmail;
        }

        public Button getButtonChat() {
            return buttonChat;
        }

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            textViewName = itemView.findViewById(R.id.textView_userName);
            textViewEmail = itemView.findViewById(R.id.textView_userEmail);
            buttonChat = itemView.findViewById(R.id.buttonChat);


        }


    }

    @NonNull
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRecyclerView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.users_list,parent, false);

        return new ViewHolder(itemRecyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.ViewHolder holder, int position) {

        Friend curFriend = this.getFriends().get(position);

        if (curFriend != null) {
            if (curFriend.getName() != null) {
                holder.getTextViewName().setText(curFriend.getName());
            } else {
                holder.getTextViewName().setText("Unknown Name");
            }

            if (curFriend.getEmail() != null) {
                holder.getTextViewEmail().setText(curFriend.getEmail());
            } else {
                holder.getTextViewEmail().setText("Unknown Email");
            }
        } else {
            holder.getTextViewName().setText("Null Friend");
            holder.getTextViewEmail().setText("Null Friend");
        }

        holder.getButtonChat().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (curFriend != null && curFriend.getEmail() != null) {
                    String email = friends.get(holder.getAdapterPosition()).getEmail();
                    mListener.chatButtonClickedFromRecyclerView(email);
                } else {
                    Log.e("FriendAdapter", "Cannot perform chatButtonClickedFromRecyclerView() due to null Friend or null Email");
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        if (this.getFriends() != null) {
            return this.getFriends().size();
        } else {
            return 0;
        }
    }

    public interface IfriendsListRecyclerAction {
        void chatButtonClickedFromRecyclerView(String email);
    }
}
