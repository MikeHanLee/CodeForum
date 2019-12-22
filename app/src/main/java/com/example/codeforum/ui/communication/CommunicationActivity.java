package com.example.codeforum.ui.communication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.codeforum.service.MyWebSocketClientService;
import com.example.codeforum.R;
import com.example.codeforum.Utils;
import com.example.codeforum.component.messageView.MessageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CommunicationActivity extends Activity {
    private String name="";
    private String phone="";
    private String user_name;
    private String user_phone;
    private TextView target_name;
    private TextView target_phone;
    private EditText send_text;
    private Button send;
    private ImageView back;
    private ScrollView message_container;
    private Handler handler = new Handler();
    private CommunicationHandler textHandler=new CommunicationHandler();
    private ArrayAdapter mAdapter;
    private MyWebSocketClientService myService;
    private MyWebSocketClientService.MyWebSocketClientBinder myBinder;
    private Bitmap user_bitmap;
    private Bitmap _bitmap;
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

        message_container = (ScrollView) findViewById(R.id.message_container);
        handler.post(new Runnable() {
            @Override
            public void run() {
                message_container.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            name = intent.getStringExtra("name");
            phone = intent.getStringExtra("phone");
        }
        target_name = findViewById(R.id.target_name);
        target_name.setText(name);
        target_phone = findViewById(R.id.target_phone);
        target_phone.setText(phone);

        SharedPreferences user = getSharedPreferences("user", 0);
        if (user != null) {
            user_name = user.getString("user_name", "默认值");
            user_phone = user.getString("user_phone", "默认值");
        }
        final String associationName = phone.compareTo(user_phone) > 0 ? user_phone + "_" + phone : phone + "_" + user_phone;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getThisMessage(associationName,phone);
                } catch (IOException e) {
                    Log.e("getThisMessageError", e.toString());
                }
            }
        }).start();

        message_list = (LinearLayout) findViewById(R.id.message_list);

        send_text = (EditText) findViewById(R.id.send_text);
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
                    makeMessageView(text,"right");
                    send_text.setText("");
                }
            }
        });
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        final String iconPath=getApplicationContext().getExternalFilesDir("")+"/"+user_phone+"/friend/"+phone+"/userIcon";
        File icon = new File(new File(iconPath),"image.jpg");
        if(icon.exists()){
            _bitmap = BitmapFactory.decodeFile(iconPath+"/image.jpg");
            if(_bitmap!=null){
                _bitmap = Utils.toRoundBitmap(_bitmap);
            }
        }
        final String user_iconPath=getApplicationContext().getExternalFilesDir("")+"/"+user_phone+"/userIcon";
        File user_icon = new File(new File(iconPath),"image.jpg");
        if(user_icon.exists()){
            user_bitmap = BitmapFactory.decodeFile(user_iconPath+"/image.jpg");
            if(user_bitmap!=null){
                user_bitmap = Utils.toRoundBitmap(user_bitmap);
            }
        }
    }

    private void getThisMessage(String association_name,String phone) throws IOException {
        if (!association_name.equals("默认值")) {
            String urlstr = "http://58.87.100.195/CodeForum/getThisMessage.php";
            //建立网络连接
            String params = "association_name=" + association_name+"&"+"phone="+phone+"&"+"user_phone="+user_phone;
            InputStream is= Utils.connect(urlstr,params);
            //读取网页返回的数据
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));//获得输入流
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
                    }else{
                        bundle.putString("str",jsonobject.getString("text"));
                        bundle.putString("pos","left");
                        Message msg=new Message();
                        msg.setData(bundle);
                        textHandler.sendMessage(msg);
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
            //Toast.makeText(CommunicationActivity.this, "author name is: " + authorName, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            makeMessageView(intent.getStringExtra("message").split("&")[1],"left");
        }
    };

    private void makeMessageView(String str,String pos){
        MessageView messageView=new MessageView(this);
        messageView.setTextViewText(str);
        messageView.setDirection(pos);
        if(pos.equals("left")){
            messageView.setImageResource(_bitmap);
        }else{
            messageView.setImageResource(user_bitmap);
        }
        message_list.addView(messageView);
        handler.post(new Runnable() {
            @Override
            public void run() {
                message_container.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private class CommunicationHandler extends  Handler{
        @Override
        public void handleMessage(Message msg) {
            String message=msg.getData().getString("str");
            String pos=msg.getData().getString("pos");
            makeMessageView(message,pos);
        }
    }
}
