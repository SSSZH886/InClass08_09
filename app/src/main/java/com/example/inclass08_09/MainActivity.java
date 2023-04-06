package com.example.inclass08_09;

import static android.content.ContentValues.TAG;

import android.Manifest;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.inclass08_09.fragments.FragmentCameraController;
import com.example.inclass08_09.fragments.FragmentChat;
import com.example.inclass08_09.fragments.FragmentDisplayImage;
import com.example.inclass08_09.fragments.FragmentLogin;
import com.example.inclass08_09.fragments.FragmentMain;
import com.example.inclass08_09.fragments.FragmentProfile;
import com.example.inclass08_09.fragments.RegisterFragment;
import com.example.inclass08_09.model.FriendAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;

public class MainActivity extends AppCompatActivity implements FragmentLogin.IloginFragmentAction, FragmentMain.ImainFragmentButtonAction,
        FriendAdapter.IfriendsListRecyclerAction, RegisterFragment.IregisterFragmentAction, FragmentProfile.IProfileEditFragmentButtonAction, FragmentCameraController.DisplayTakenPhoto, FragmentDisplayImage.RetakePhoto, FragmentChat.IChatCameraFragmentButtonAction{

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private static final int PERMISSIONS_CODE = 0x100;

    private FirebaseStorage storage;

    private String email;

    boolean cameraAllowed = false, readAllowed = false, writeAllowed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();

    }

    @Override
    protected  void onStart() {

        super.onStart();

        currentUser = mAuth.getCurrentUser();

        populateScreen();
    }

    private void populateScreen() {
        //      Check for Authenticated users ....
        if(currentUser != null){
            //The user is authenticated, Populating The Main Fragment....
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentMain.newInstance(),"mainFragment")

                    .commit();

        }else{
//            The user is not logged in, load the login Fragment....
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentLogin.newInstance(),"loginFragment")
                    .commit();
        }
    }
    @Override
    public void populateMainFragment(FirebaseUser mUser) {
        this.currentUser = mUser;
        populateScreen();
    }
    @Override
    public void registerDone(FirebaseUser mUser) {
        this.currentUser = mUser;
        populateScreen();
    }
    @Override
    public void populateRegisterFragment() {
//            The user needs to create an account, load the register Fragment....
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, RegisterFragment.newInstance(),"registerFragment")
                .commit();
    }

    @Override
    public void logoutPressed() {
        mAuth.signOut();
        currentUser = null;
        populateScreen();
    }

    @Override
    public void populateProfile() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, FragmentProfile.newInstance(),"ProfileFragment")
                .commit();
    }

    @Override
    public void chatButtonClickedFromRecyclerView(String email) {
        this.email = email;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, FragmentChat.newInstance(this.email),"chatFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void editProfile(String name, String imageURL) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, FragmentMain.newInstance(name, imageURL),"mainFragment")
                .commit();
    }

    @Override
    public void openCamera() {
        if(cameraAllowed && readAllowed && writeAllowed){
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentCameraController.newInstance(), "cameraFragment")
                    .commit();
    }else{
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_CODE);
        }


}

//    @Override
//    public void onImageUriReceived(Uri imageUri) {
//        Log.d("demo", "onImageUriReceived: " + imageUri.toString());
////        getSupportFragmentManager().popBackStack();
//        FragmentChat chatFragment = (FragmentChat) getSupportFragmentManager().findFragmentByTag("chatFragment");
//
//        if (chatFragment != null) {
//            chatFragment.onImageUriReceived(imageUri);
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>2){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, FragmentCameraController.newInstance(), "cameraFragment")
                    .commit();
        }else{
            Toast.makeText(this, "You must allow Camera and Storage permissions!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onTakePhoto(Uri imageUri) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain,FragmentDisplayImage.newInstance(imageUri),"displayFragment")
                .commit();
    }

    @Override
    public void onOpenGalleryPressed() {
        openGallery();
    }

    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==RESULT_OK){
                        Intent data = result.getData();
                        Uri selectedImageUri = data.getData();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.containerMain, FragmentChat.newInstance(email, selectedImageUri.toString()), "chatFragment")
                                .addToBackStack("chatFragment") // Add this line
                                .commit();
                    }
                }
            }
    );

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        galleryLauncher.launch(intent);
    }

    @Override
    public void onRetakePressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, FragmentCameraController.newInstance(), "cameraFragment")
                .commit();
    }

    @Override
    public void onUploadButtonPressed(Uri imageUri, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        StorageReference storageReference = storage.getReference().child("images/" + imageUri.getLastPathSegment());
        UploadTask uploadImage = storageReference.putFile(imageUri);
        uploadImage.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Upload Failed! Try again!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Retrieve the image URL from Firebase Storage
                                String imageURL = uri.toString();
                                // Update the user's avatar URL in Firestore database
                                updateAvatarInFirestore(imageURL);
                                // Notify the fragment to load the avatar image into the ImageView

                                progressBar.setVisibility(View.GONE);

                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.containerMain, FragmentProfile.newInstance(imageURL),"ProfileFragment")
                                        .commit();
                            }
                        });
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressBar.setProgress((int) progress);
                    }
                });
    }

    //This works.
    private void updateAvatarInFirestore(String imageURL) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            DocumentReference userRef = db.collection("users").document("ssszh886@gmail.com").collection("Friends").document(user.getEmail());
            userRef.update("imageURL", imageURL)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "Avatar updated in Firestore!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to update avatar in Firestore!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}