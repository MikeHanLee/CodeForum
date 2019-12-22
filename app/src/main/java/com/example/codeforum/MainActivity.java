package com.example.codeforum;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.codeforum.service.MyWebSocketClientService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.tavendo.autobahn.WebSocketConnection;

public class MainActivity extends AppCompatActivity {
    private String user_phone = "";
    private MyWebSocketClientService.MyWebSocketClientBinder myBinder;
    private WebSocketConnection mConnection = new WebSocketConnection();
    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                getFriendIcon(user_phone);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_discover, R.id.navigation_notifications, R.id.navigation_friend, R.id.navigation_personal)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        SharedPreferences user = getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
            if (!user_phone.equals("默认值")) {
                thread.start();
                Intent myServiceIntent = new Intent(MainActivity.this, MyWebSocketClientService.class);
                bindService(myServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    private void getFriendIcon(String user_phone) throws IOException {
        String urlstr = "http://58.87.100.195/CodeForum/getFriendIcon.php";
        String params = "phone=" + user_phone;
        InputStream is = Utils.connect(urlstr, params);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line = "";
        StringBuilder sb = new StringBuilder();
        while (null != (line = bufferedReader.readLine())) {
            sb.append(line);
        }
        String result = sb.toString();
        try {
            JSONArray jsonArray = new JSONArray(result);
            String nameLine = "";
            String phoneLine = "";
            String iconLine = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                nameLine = jsonObject.getString("name");
                phoneLine = jsonObject.getString("phone");
                if(!phoneLine.equals("null")) {
                    iconLine = jsonObject.getString("icon");
                    Bitmap bitmap = Utils.stringtoBitmap(iconLine);
                    final String iconPath = getApplicationContext().getExternalFilesDir("") + "/" + user_phone + "/friend/" + phoneLine + "/userIcon";
                    Utils.savePhoto(bitmap, iconPath, "image");
                }
            }
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyWebSocketClientService myService = ((MyWebSocketClientService.MyWebSocketClientBinder) service).getService();
            myBinder = (MyWebSocketClientService.MyWebSocketClientBinder) service;
            String authorName = myService.getAuthorName();
            //Toast.makeText(MainActivity.this, "author name is: " + authorName, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void mainUnbindService() {
        unbindService(mServiceConnection);
    }

    public void mainBindService() {
        Intent myServiceIntent = new Intent(MainActivity.this, MyWebSocketClientService.class);
        bindService(myServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public IBinder getBinder() {
        return myBinder;
    }

    public void setBinder() {
        myBinder = null;
    }
}
