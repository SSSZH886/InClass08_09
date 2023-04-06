package com.example.inclass08_09.fragments;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FragmentChat extends Fragment {

    private static final int PERMISSIONS_CODE = 0x100;
    Boolean cameraAllowed, readAllowed, writeAllowed;

    //private String receiverEmail;

    private static final String ARG_MESSAGES = "messageArray";

    private static final String ARG_RECEIVEREMAIL = "receiverEmail";

    private static final String ARG_URL = "imageURL";

    private Friend mFriend;

    private EditText editTextMessage;

    //private String imageUri;

    private Button buttonSend, buttonCamera;

    private IChatCameraFragmentButtonAction mListener;

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
        //fragment.receiverEmail = receiverEmail;
        return fragment;
    }

    public static FragmentChat newInstance(String receiverEmail, String imageURL) {
        FragmentChat fragment = new FragmentChat();
        Bundle args = new Bundle();
        args.putString(ARG_RECEIVEREMAIL, receiverEmail);
        args.putString(ARG_URL, imageURL);
        args.putSerializable(ARG_MESSAGES, new ArrayList<Chat>());
        fragment.setArguments(args);
        //fragment.receiverEmail = receiverEmail;
        return fragment;
    }

//    public static FragmentChat newInstance(Uri URI) {
//        FragmentChat fragment = new FragmentChat();
//        Bundle args = new Bundle();
//        args.putParcelable(ARG_URL, URI);
//        args.putSerializable(ARG_MESSAGES, new ArrayList<Chat>());
//        fragment.setArguments(args);
//        return fragment;
//    }


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
        Log.d(TAG, "onCreate: " + getArguments().getString(ARG_RECEIVEREMAIL));
        Log.d(TAG, "onCreate: " + mUser.getEmail());
        Log.d(TAG, "onCreate: " + getArguments().getString(ARG_URL));
        loadMessage(mUser.getEmail(), args.getString(ARG_RECEIVEREMAIL));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);
        buttonCamera = view.findViewById(R.id.button_OpenCamera);

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionsAndStartCamera();
            }
        });

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
                    if (getArguments().getString(ARG_URL) != null){
                        sendMessages(mUser.getEmail(),getArguments().getString(ARG_RECEIVEREMAIL), "", getArguments().getString(ARG_URL));
                    }
                }
                editTextMessage.setText("");
            }
        });


        return view;
    }

    private void sendMessages(String sender, String receiver, String message, String imageURL){
        //db = FirebaseFirestore.getInstance();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("imageURL", imageURL);

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

    private void sendMessages(String sender, String receiver, String message) {
        sendMessages(sender, receiver, message, ""); // Call the modified version with an empty imageURL
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
                        if (chat.getSender() != null && chat.getReceiver() != null) {
                            if ((chat.getSender().equals(senderEmail) && chat.getReceiver().equals(receiverEmail))
                                    || (chat.getReceiver().equals(senderEmail) && chat.getSender().equals(receiverEmail))) {
                                chats.add(chat);
                            }
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


    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentChat.IChatCameraFragmentButtonAction) {
            mListener = (FragmentChat.IChatCameraFragmentButtonAction) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement IProfileEditFragmentButtonAction");
        }
    }

    private void checkPermissionsAndStartCamera() {
        cameraAllowed = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        readAllowed = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        writeAllowed = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (cameraAllowed && readAllowed && writeAllowed) {
            mListener.openCamera();
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_CODE) {
            if (grantResults.length > 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                mListener.openCamera();
            } else {
                Toast.makeText(requireActivity(), "You must allow Camera and Storage permissions!", Toast.LENGTH_LONG).show();
            }
        }
    }

//    public void onImageUriReceived(Uri imageUri) {
//        this.imageUri = imageUri.toString();
//    }

    public interface IChatCameraFragmentButtonAction {
        void openCamera();
        //void onImageUriReceived(Uri imageUri);
    }
}