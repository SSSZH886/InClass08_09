package com.example.inclass08_09;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.inclass08_09.fragments.FragmentChat;
import com.example.inclass08_09.fragments.FragmentLogin;
import com.example.inclass08_09.fragments.FragmentMain;
import com.example.inclass08_09.fragments.RegisterFragment;
import com.example.inclass08_09.model.FriendAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements FragmentLogin.IloginFragmentAction, FragmentMain.ImainFragmentButtonAction,
        FriendAdapter.IfriendsListRecyclerAction, RegisterFragment.IregisterFragmentAction{

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

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
    public void chatButtonClickedFromRecyclerView(String email) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, FragmentChat.newInstance(email),"chatFragment")
                .commit();
    }
}