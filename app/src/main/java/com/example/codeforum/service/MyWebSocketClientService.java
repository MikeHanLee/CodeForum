package com.example.codeforum.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class MyWebSocketClientService extends Service {
    private final String TAG = "SosWebSocketClient";
    private final WebSocketConnection mConnection = new WebSocketConnection();
    private final IBinder mBinder = new MyWebSocketClientBinder();

    @Override
    public void onCreate() {
        initWebSocket();
        super.onCreate();
        Log.d(TAG, "Service Create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    /**
     * 销毁
     */
    @Override
    public void onDestroy() {
        if (mConnection.isConnected()) {
            mConnection.disconnect();
        }
        super.onDestroy();
        Log.d(TAG, "Service Destroy");
    }

    private void initWebSocket(){
        //注意连接和服务名称要一致
		final String wsuri = "ws://58.87.100.195:9200/CodeForum";
        try {
            mConnection.connect(wsuri, new WebSocketHandler() {
                @Override
                public void onOpen() {
                    SharedPreferences user = getSharedPreferences("user", 0);
                    mConnection.sendTextMessage(user.getString("user_phone","default"));
                    Log.i(TAG, "WebSocket open");
                }

                @Override
                public void onTextMessage(String text) {
                    Intent intent = new Intent();
                    intent.setAction("com.example.codeforum.servicecallback.content");
                    intent.putExtra("message", text);
                    sendBroadcast(intent);
                    Log.i(TAG, "onTextMessage");
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.i(TAG, "Connection lost.");
                }
            });
        } catch (WebSocketException e) {
            Log.d(TAG, e.toString());
        }
    }



    public class MyWebSocketClientBinder extends Binder {
        public MyWebSocketClientService getService() {
            return MyWebSocketClientService.this;
        }

        public void sendMessage(String addr){
            if(mConnection.isConnected())
                mConnection.sendTextMessage(addr);
        }
    }
    public String getAuthorName(){
        return "guchuanhang";
    }
}