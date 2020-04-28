package com.example.mobileapplication;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.mobileapplication.ui.activities.ActivitiesFragment;
import com.example.mobileapplication.ui.selectcars.SelectCarsFragment;
import com.example.mobileapplication.ui.home.HomeFragment;
import com.example.mobileapplication.ui.profiles.ProfilesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.util.List;
import java.util.Locale;



//main activity in which are defined the main four fragment

public class StartActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{


    String email;
    String token;
    public BottomNavigationView navigationView;


    private ActionBar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        navigationView = (BottomNavigationView) findViewById(R.id.nav_view);

        navigationView.setOnNavigationItemSelectedListener(this);

        toolbar = getSupportActionBar();
        toolbar.setTitle(" ");


        Intent myIntent = getIntent(); // gets the previously created intent
        email = myIntent.getStringExtra("email");
        token = myIntent.getStringExtra("token");


        openFragment(HomeFragment.newInstance(email, token));


    }
    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Log.e("StartActivity", email+ " : "+token);


        switch (item.getItemId()) {
            case R.id.navigation_home:

                openFragment(HomeFragment.newInstance(email, token));
                toolbar.setTitle(" ");


                return true;
            case R.id.navigation_activities:

                openFragment(ActivitiesFragment.newInstance(email, token));

                toolbar.setTitle(" ");

                return true;
            case R.id.navigation_selectcars:

                openFragment(SelectCarsFragment.newInstance(email, token));
                toolbar.setTitle(" ");



                return true;
            case R.id.navigation_profile:

                openFragment(ProfilesFragment.newInstance(email, token));
                toolbar.setTitle(" ");

                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {

        navigationView.getMenu().findItem(R.id.navigation_home).setChecked(true);

        openFragment(HomeFragment.newInstance(email, token));

    }

}
