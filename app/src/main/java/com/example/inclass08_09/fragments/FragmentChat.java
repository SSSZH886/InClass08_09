package com.example.inclass08_09.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inclass08_09.R;
import com.example.inclass08_09.model.Chat;
import com.example.inclass08_09.model.ChatsAdapter;
import com.example.inclass08_09.model.Friend;
import com.example.inclass08_09.model.FriendAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FragmentChat extends Fragment {

    private static final String ARG_MESSAGES = "messageArray";

    private static final String ARG_RECEIVEREMAIL = "receiverEmail";

    private Friend mFriend;

    private EditText editTextMessage;

    private Button buttonSend;

    private RecyclerView recyclerView;
    private ChatsAdapter chatssAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private ArrayList<Chat> mMessages;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    public FragmentChat() {
        // Required empty public constructor
    }


    public static FragmentChat newInstance(String receiverEmail) {
        FragmentChat fragment = new FragmentChat();
        Bundle args = new Bundle();
        args.putString(ARG_RECEIVEREMAIL, receiverEmail);
        args.putSerializable(ARG_MESSAGES, new ArrayList<Chat>());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_MESSAGES)) {
                mMessages = (ArrayList<Chat>) args.getSerializable(ARG_MESSAGES);
                Log.d("main fragment - initial Message data", mMessages.toString());
            }
        } else {
            mMessages = new ArrayList<>(); // initialize the mFriends ArrayList here
        }
        //            Initializing Firebase...
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        Log.d(TAG, "onCreate: " + args.getString(ARG_RECEIVEREMAIL));
        loadMessage(mUser.getEmail(), args.getString(ARG_RECEIVEREMAIL));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        recyclerView = view.findViewById(R.id.recyclerViewMessage);
        recyclerViewLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        chatssAdapter = new ChatsAdapter();
        recyclerView.setAdapter(chatssAdapter);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = editTextMessage.getText().toString();
                if (!msg.equals("")){
                    sendMessages(mUser.getEmail(),getArguments().getString(ARG_RECEIVEREMAIL), msg);
                } else{
                    Toast.makeText(getContext(), "Send Message Failed!", Toast.LENGTH_LONG).show();
                }
                editTextMessage.setText("");
            }
        });


        return view;
    }

    private void sendMessages(String sender, String receiver, String message){
        //db = FirebaseFirestore.getInstance();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        LocalTime currentTime = LocalTime.now();
        Date date = new Date();
        // Save the message to Firestore
        db.collection("messages").document(date.toString()+ currentTime.toString())
                .set(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("FragmentChat", "Message sent successfully");
                        loadMessage(sender, receiver);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FragmentChat", "Error sending message", e);
                    }
                });
    }

    private void loadMessage(String senderEmail, String receiverEmail) {
        ArrayList<Chat> chats = new ArrayList<>();
        db.collection("messages")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
//                                Just like GSON..... Friend has to be Serializable,
//                                has to exactly match the variable names with the keys in the documents,
//                                and must have getters, setters, and toString() ....

                        Chat chat = documentSnapshot.toObject(Chat.class);
                        if ((chat.getSender().equals(senderEmail) && chat.getReceiver().equals(receiverEmail))
                                || chat.getReceiver().equals(senderEmail) && chat.getSender().equals(receiverEmail) ){
                            chats.add(chat);
                        }

                    }
                    updateRecyclerView(chats);
                }
            }
        });
    }

    public void updateRecyclerView(ArrayList<Chat> messages){
        this.mMessages = messages;
        Log.d("Main fragment - updating recyler view ", mMessages.toString());
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        chatssAdapter = new ChatsAdapter(mMessages);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(chatssAdapter);
    }
}