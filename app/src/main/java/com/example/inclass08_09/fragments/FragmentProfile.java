package com.example.inclass08_09.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.inclass08_09.R;
import com.example.inclass08_09.model.Friend;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentProfile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentProfile extends Fragment {

    private static final String ARG_URL = "imageURL";
    private static final int PERMISSIONS_CODE = 0x100;
    Boolean cameraAllowed, readAllowed, writeAllowed;

    private IProfileEditFragmentButtonAction mListener;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private EditText editTextName;

    private Button buttonEdit, buttonCamera;

    private ImageView imageViewAvatar;

    public FragmentProfile() {
        // Required empty public constructor
    }

    public static FragmentProfile newInstance() {
        FragmentProfile fragment = new FragmentProfile();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public static FragmentProfile newInstance(String imageURL) {
        FragmentProfile fragment = new FragmentProfile();
        Bundle args = new Bundle();
        args.putString(ARG_URL, imageURL);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);


        editTextName = rootView.findViewById(R.id.editText_editProfileName);
        buttonCamera = rootView.findViewById(R.id.button_takePicture);
        buttonEdit = rootView.findViewById(R.id.button_editProfile);

        imageViewAvatar = rootView.findViewById(R.id.imageView_editProfile);
        Glide.with(this)
                .load(getArguments().getString(ARG_URL))
                .into(imageViewAvatar);

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionsAndStartCamera();
            }
        });
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = String.valueOf(editTextName.getText());
                String url = getArguments().getString(ARG_URL);
                mListener.editProfile(name, url);

            }
        });


        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IProfileEditFragmentButtonAction) {
            mListener = (IProfileEditFragmentButtonAction) context;
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



    public interface IProfileEditFragmentButtonAction {
        void editProfile(String name, String imageURL);
        void openCamera();

    }


}