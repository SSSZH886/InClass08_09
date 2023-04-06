package com.example.inclass08_09.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.inclass08_09.R;
import com.example.inclass08_09.model.Friend;
import com.example.inclass08_09.model.FriendAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the  {@link FragmentMain#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentMain extends Fragment {

    private static final String ARG_FRIENDS = "friendsarray";

    private static final String ARG_URL = "imageURL";

    private static final String ARG_USERNAME = "userName";

    private Friend mFriend;

    private ImageButton logout;


    private ImageView goToProfile;

    private TextView greeting;

    private RecyclerView recyclerView;
    private FriendAdapter friendsAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private ArrayList<Friend> mFriends;

    private ImainFragmentButtonAction mListener;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof ImainFragmentButtonAction){
            mListener = (ImainFragmentButtonAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IaddButtonAction");
        }
    }

    public FragmentMain() {
        // Required empty public constructor
    }


    public static FragmentMain newInstance(String name, String imageURL) {
        FragmentMain fragment = new FragmentMain();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME,name);
        args.putString(ARG_URL, imageURL);
        args.putSerializable(ARG_FRIENDS, new ArrayList<Friend>());
        fragment.setArguments(args);
        return fragment;
    }

    public static FragmentMain newInstance() {
        FragmentMain fragment = new FragmentMain();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FRIENDS, new ArrayList<Friend>());
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_FRIENDS)) {
                mFriends = (ArrayList<Friend>) args.getSerializable(ARG_FRIENDS);
                Log.d("main fragment - initial friends data", mFriends.toString());
            }

        } else {
            mFriends = new ArrayList<>(); // initialize the mFriends ArrayList here
        }
        //            Initializing Firebase...
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        Log.d(TAG, "onCreate: " + args.getString("edit name") );
        if (args.containsKey(ARG_USERNAME)) {
            editProfile(args.getString(ARG_USERNAME), args.getString(ARG_URL));
        }
        //            Loading initial data...
        loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        goToProfile = rootView.findViewById(R.id.imageView_goToProfile);
        greeting = rootView.findViewById(R.id.textView_greetings);

        Glide.with(this)
                .load(getArguments().getString(ARG_URL))
                .into(goToProfile);

        logout = rootView.findViewById(R.id.imageButtonLogout);
        greeting.setText("Hello "+mUser.getEmail()+"!!!");

        //      Setting up RecyclerView........
        recyclerView = rootView.findViewById(R.id.recyclerReview);
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        friendsAdapter = new FriendAdapter(mFriends, getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(friendsAdapter);

        goToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.populateProfile();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.logoutPressed();
            }
        });

        return rootView;
    }


    //    Updating the RecyclerView when something gets changed...
    public void updateRecyclerView(ArrayList<Friend> friends){
        this.mFriends = friends;
        Log.d("Main fragment - updating recyler view ", mFriends.toString());
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        friendsAdapter = new FriendAdapter(mFriends, getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(friendsAdapter);
    }

    private void loadData() {
        ArrayList<Friend> friends = new ArrayList<>();
        db.collection("users")
                .document("ssszh886@gmail.com")
                .collection("Friends")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
//                                Just like GSON..... Friend has to be Serializable,
//                                has to exactly match the variable names with the keys in the documents,
//                                and must have getters, setters, and toString() ....

                                Friend friend = documentSnapshot.toObject(Friend.class);
                                friends.add(friend);

                            }
                            updateRecyclerView(friends);
                        }
                    }
                });
    }

    private void editProfile(String name, String imageURL){
        //String email = String.valueOf(editTextEmail.getText());
        db.collection("users")
                .document("ssszh886@gmail.com")
                .collection("Friends")
                .document(mUser.getEmail())
                .delete();

        mFriend = new Friend(name, mUser.getEmail(), imageURL);

        addToFirebase(mFriend);
    }

    private void addToFirebase(Friend friend) {
        db.collection("users")
                .document("ssszh886@gmail.com")
                .collection("Friends")
                .document(friend.getEmail())
                .set(friend)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("register fragment: friends added", "friends added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FAILED TO ADD A FRIEND", String.valueOf(e));
                    }
                });
    }

    public interface ImainFragmentButtonAction {
        void logoutPressed();

        void populateProfile();
    }


}