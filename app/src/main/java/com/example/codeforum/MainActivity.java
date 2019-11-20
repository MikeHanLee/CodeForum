package com.example.codeforum;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;

import de.tavendo.autobahn.WebSocketConnection;

public class MainActivity extends AppCompatActivity{
    private String user_phone = "";
    private MyWebSocketClientService.MyWebSocketClientBinder myBinder;
    private WebSocketConnection mConnection=new WebSocketConnection();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_discover, R.id.navigation_notifications, R.id.navigation_friend, R.id.navigation_personal)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        SharedPreferences user = getSharedPreferences("user", 0);
        if(user!=null){
            user_phone = user.getString("user_phone", "默认值");
            if(!user_phone.equals("默认值")){
                Intent myServiceIntent = new Intent(MainActivity.this, MyWebSocketClientService.class);
                bindService(myServiceIntent, mServiceConnection,Context.BIND_AUTO_CREATE);
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyWebSocketClientService myService = ((MyWebSocketClientService.MyWebSocketClientBinder) service).getService();
            myBinder=(MyWebSocketClientService.MyWebSocketClientBinder) service;
            String authorName = myService.getAuthorName();
            Toast.makeText(MainActivity.this, "author name is: " + authorName, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void mainUnbindService(){
        unbindService(mServiceConnection);
    }

    public void mainBindService(){
        Intent myServiceIntent = new Intent(MainActivity.this, MyWebSocketClientService.class);
        bindService(myServiceIntent, mServiceConnection,Context.BIND_AUTO_CREATE);
    }

    public IBinder getBinder(){
        return myBinder;
    }

    public void setBinder(){
        myBinder=null;
    }
}
