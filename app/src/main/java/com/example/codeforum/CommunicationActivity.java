package com.example.codeforum;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class CommunicationActivity extends Activity {
    private String name;
    private String user_name;
    private String phone;
    private String user_phone;
    private TextView target_name;
    private EditText send_text;
    private Button send;
    private TextView back;
    private ScrollView message_container;
    private Handler handler = new Handler();
    private Handler textHandler;
    private ArrayAdapter mAdapter;
    private MyWebSocketClientService myService;
    private MyWebSocketClientService.MyWebSocketClientBinder myBinder;
    String[] data = {"abc", "efg", "abc", "efg", "abc", "efg", "abc", "efg", "abc", "efg", "abc", "efg", "abc", "efg", "abc", "efg", "abc", "efg", "abc", "efg", "abc", "efg", "abc", "efg", "abc", "efg"};
    private LinearLayout message_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_communication);

        Intent myServiceIntent = new Intent(CommunicationActivity.this, MyWebSocketClientService.class);
        bindService(myServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter("com.example.codeforum.servicecallback.content");
        registerReceiver(broadcastReceiver, filter);

        textHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Log.e("result",msg.getData().toString());
                String message=msg.getData().getString("str");
                String pos=msg.getData().getString("pos");
                Log.e("result",message);
                makeTextView(message,pos);
            }
        };

        message_container = (ScrollView) findViewById(R.id.message_container);
        handler.post(new Runnable() {
            @Override
            public void run() {
                message_container.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        name = "";
        Intent intent = getIntent();
        if (intent != null) {
            name = intent.getStringExtra("name");
            phone = intent.getStringExtra("phone");
        }
        SharedPreferences user = getSharedPreferences("user", 0);
        if (user != null) {
            user_name = user.getString("user_name", "默认值");
            user_phone = user.getString("user_phone", "默认值");
        }
        final String associationName = phone.compareTo(user_phone) > 0 ? user_phone + "_" + phone : phone + "_" + user_phone;
        Log.e("associationName",associationName);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getThisMessage(associationName);
                } catch (IOException e) {
                    Log.e("getThisMessageError", e.toString());
                }
            }
        }).start();

        target_name = findViewById(R.id.target_name);
        target_name.setText(name);
        message_list = (LinearLayout) findViewById(R.id.message_list);

        send_text = (EditText) findViewById(R.id.send_text);
        send_text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });
        send_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        message_container.scrollTo(0, message_list.getBottom());
                    }
                });
            }
        });
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = send_text.getText().toString();
                if (text == null || text.length() <= 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            Toast.makeText(CommunicationActivity.this, "不能发送空消息！", Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                    }).start();
                } else {
                    myBinder.sendMessage(user_phone+"&"+text+"&"+phone);
                    makeTextView(text,"right");
                    send_text.setText("");
                }
            }
        });

        back = (TextView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void getThisMessage(String association_name) throws IOException {
        if (!association_name.equals("默认值")) {
            String urlstr = "http://58.87.100.195/CodeForum/getThisMessage.php";
            //建立网络连接
            URL url = new URL(urlstr);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            //往网页写入POST数据，和网页POST方法类似，参数间用‘&’连接
            String params = "association_name=" + association_name;
            Log.e("association_name",association_name);
            http.setDoOutput(true);
            http.setDoInput(true);
            http.setRequestMethod("POST");
            http.connect();
            OutputStream out = http.getOutputStream();
            out.write(params.getBytes());//post提交参数
            out.flush();
            out.close();
            //读取网页返回的数据
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));//获得输入流
            String line = "";
            StringBuilder sb = new StringBuilder();//建立输入缓冲区
            while (null != (line = bufferedReader.readLine())) {//结束会读入一个null值
                sb.append(line);//写缓冲区
            }
            String result = sb.toString();//返回结果
            try {
                JSONArray jsonArray = new JSONArray(result);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonobject=new JSONObject(jsonArray.getString(i));
                    Bundle bundle=new Bundle();
                    if(jsonobject.getString("fromClient").equals(user_phone)){
                        bundle.putString("str",jsonobject.getString("text"));
                        bundle.putString("pos","right");
                        Message msg=new Message();
                        msg.setData(bundle);
                        textHandler.sendMessage(msg);
                        Log.e("result",jsonobject.getString("text"));
                    }else{
                        bundle.putString("str",jsonobject.getString("text"));
                        bundle.putString("pos","left");
                        Message msg=new Message();
                        msg.setData(bundle);
                        textHandler.sendMessage(msg);
                        Log.e("result",jsonobject.getString("text"));
                    }
                }
            } catch (Exception e) {
                Log.e("log_tag", "the Error parsing data " + e.toString());
            }
        }
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((MyWebSocketClientService.MyWebSocketClientBinder) service).getService();
            myBinder = (MyWebSocketClientService.MyWebSocketClientBinder) service;
            String authorName = myService.getAuthorName();
            Toast.makeText(CommunicationActivity.this, "author name is: " + authorName, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("broadcastmessage",intent.getStringExtra("message"));
            makeTextView(intent.getStringExtra("message").split("&")[1],"left");
        }
    };

    private void makeTextView(String str,String pos){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if(pos.equals("left")){
            layoutParams.gravity = Gravity.START;
            layoutParams.leftMargin = 30;
        }else{
            layoutParams.gravity = Gravity.END;
            layoutParams.rightMargin = 30;
        }
        layoutParams.topMargin = 30;
        TextView textView = new TextView(this);
        textView.setMaxWidth(800);
        textView.setBackgroundColor(Color.parseColor("#dddddd"));
        textView.setPadding(10, 10, 10, 10);
        textView.setTextSize(20);
        textView.setText(str);
        message_list.addView(textView, layoutParams);
        handler.post(new Runnable() {
            @Override
            public void run() {
                message_container.scrollTo(0, message_list.getBottom());
            }
        });
    }
}
