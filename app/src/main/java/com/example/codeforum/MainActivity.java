package com.example.codeforum;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.codeforum.ui.personal.PersonalFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;

public class MainActivity extends AppCompatActivity{
    private TextView person_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_discover, R.id.navigation_notifications, R.id.navigation_friend, R.id.navigation_personal)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        loginGetIntentInfo(navController);
    }
    private void loginGetIntentInfo(NavController navController){
        Intent intent=getIntent();
        if(intent!=null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                //String name = bundle.getString("name");
                int id=bundle.getInt("id");
                if(id==1){
                    navController.navigate(R.id.navigation_personal, bundle);
                }
            }
        }
    }
}
