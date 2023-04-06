package com.example.inclass08_09.fragments;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.inclass08_09.R;

import android.content.Context;


import androidx.annotation.NonNull;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.inclass08_09.model.Friend;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class RegisterFragment extends Fragment implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private FirebaseUser mUser;
    private EditText editTextName, editTextEmail, editTextPassword, editTextRepPassword;
    private Button buttonRegister;

    PreviewView previewView;

    private ImageCapture imageCapture;

    private final int CAMERA_REQUEST_CODE = 100;
    private Bitmap bitmap;
    private String name, email, password, rep_password;
    private IregisterFragmentAction mListener;
    private FirebaseFirestore db;

    private ImageView imageView;

    private File photoFile;



    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mUser = mAuth.getCurrentUser();



    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IregisterFragmentAction){
            this.mListener = (IregisterFragmentAction) context;
        }else{
            throw new RuntimeException(context.toString()
                    + "must implement RegisterRquest");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle("Register Fragment");
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);
        editTextName = rootView.findViewById(R.id.editTextRegister_Name);
        editTextEmail = rootView.findViewById(R.id.editTextRegister_Email);
        editTextPassword = rootView.findViewById(R.id.editTextRegister_Password);
        editTextRepPassword = rootView.findViewById(R.id.editTextRegister_Rep_Password);
        buttonRegister = rootView.findViewById(R.id.buttonRegister);
        //previewView = rootView.findViewById(R.id.previewView);
        //buttonCapture = rootView.findViewById(R.id.bCapture);
        buttonRegister.setOnClickListener(this);
        //imageView = rootView.findViewById(R.id.imageView2);







        return rootView;
    }

    @Override
    public void onClick(View view) {
        this.name = String.valueOf(editTextName.getText()).trim();
        this.email = String.valueOf(editTextEmail.getText()).trim();
        this.password = String.valueOf(editTextPassword.getText()).trim();
        this.rep_password = String.valueOf(editTextRepPassword.getText()).trim();

        if(view.getId()== R.id.buttonRegister){
//            Validations........
            if(name.equals("")){
                Toast.makeText(getActivity(), "Name must not be empty!", Toast.LENGTH_SHORT).show();
            }
            if(email.equals("")){
                Toast.makeText(getActivity(), "Email must not be empty!", Toast.LENGTH_SHORT).show();
            }
            if(password.equals("")){
                Toast.makeText(getActivity(), "Password must not be empty!", Toast.LENGTH_SHORT).show();
            }
            if(!rep_password.equals(password)){
                Toast.makeText(getActivity(), "Password must match", Toast.LENGTH_SHORT).show();
            }

//            Validation complete.....
            if(!name.equals("") && !email.equals("")
                    && !password.equals("")
                    && rep_password.equals(password)){
                mUser = mAuth.getCurrentUser();
                Friend newFriend = new Friend(name, email);
                addToFirebase(newFriend);

                // Firebase authentication: Create user.......
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Log.d("register fragment", "successfully registered ");
                                    mUser = task.getResult().getUser();
//                                    Adding name to the FirebaseUser...
                                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();

                                    mUser.updateProfile(profileChangeRequest)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Log.d("register fragment", "name added to user");
                                                        mListener.registerDone(mUser);
                                                    }
                                                }
                                            });

                                }
                            }


                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("register fragment failed", e.toString());
                            }
                        });



            }
        }
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



public interface IregisterFragmentAction {
        void registerDone(FirebaseUser mUser);
    }



}