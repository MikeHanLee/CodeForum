package com.example.codeforum.ui.notifications;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.codeforum.CommunicationActivity;
import com.example.codeforum.MainActivity;
import com.example.codeforum.MyWebSocketClientService;
import com.example.codeforum.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private ArrayAdapter mAdapter;
    private static Handler handler;
    private ListView my_notifications;
    private String user_phone = "";
    private final static int communicationActivity = 4;
    private SharedPreferences user;
    @Override
    //获取单独信息交流页面的返回结果
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case (communicationActivity): {
                if (resultCode == Activity.RESULT_OK) {
                    SharedPreferences user = getActivity().getSharedPreferences("user", 0);
                    if (user != null) {
                        user_phone = user.getString("user_phone", "默认值");
                        if (!user_phone.equals("默认值")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        getNotification(user_phone);
                                    } catch (IOException e) {
                                        Log.e("log_tag", "the Error parsing data " + e.toString());
                                    }
                                }
                            }).start();
                        }
                    }
                }
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        /*final TextView textView = root.findViewById(R.id.text_notifications);
        notificationsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        //获取消息列表后进行视图数据更新
        my_notifications = (ListView) root.findViewById(R.id.my_notifications);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String[] message = msg.getData().getString("message").split(",");
                mAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, message);
                my_notifications.setAdapter(mAdapter);
                final String[] data = message;
                my_notifications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.e("message", data[position]);
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), CommunicationActivity.class);
                        intent.putExtra("name", data[position]);
                        intent.putExtra("phone",data[position]);
                        startActivityForResult(intent, communicationActivity);
                    }
                });
            }
        };

        //获取消息列表
        user = getActivity().getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
            if (!user_phone.equals("默认值")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getNotification(user_phone);
                        } catch (IOException e) {
                            Log.e("log_tag", "the Error parsing data " + e.toString());
                        }
                    }
                }).start();
            }
        }
        SharedPreferences user = getActivity().getSharedPreferences("user", 0);
        if(user!=null){
                //Intent myServiceIntent = new Intent(MainActivity.this, MyWebSocketClientService.class);
                //bindService(myServiceIntent, mServiceConnection,Context.BIND_AUTO_CREATE);
            IntentFilter filter = new IntentFilter("com.example.codeforum.servicecallback.content");
            getActivity().registerReceiver(broadcastReceiver, filter);
        }
        //Intent myServiceIntent = new Intent(getActivity(), MyWebSocketClientService.class);
        //getActivity().bindService(myServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        return root;
    }

    //获取新消息列表
    private void getNotification(String user_phone) throws IOException {
        Bundle bundle = new Bundle();
        if (!user_phone.equals("默认值")) {
            String urlstr = "http://58.87.100.195/CodeForum/getMessage.php";
            URL url = new URL(urlstr);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            String params = "phone=" + user_phone;
            http.setDoOutput(true);
            http.setDoInput(true);
            http.setRequestMethod("POST");
            http.connect();
            OutputStream out = http.getOutputStream();
            out.write(params.getBytes());
            out.flush();
            out.close();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));//获得输入流
            String line = "";
            StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }
            String result = sb.toString();
            Log.e("result",result);
            try {
                JSONArray jsonArray = new JSONArray(result);
                String messageline = "";
                messageline=jsonArray.getString(0);
                for(int i=1;i<jsonArray.length();i++){
                    messageline=messageline+","+jsonArray.getString(i);
                }
                if (messageline.equals("null") || messageline.length() == 0) {
                    bundle.putString("message", "暂无新消息！");
                } else {
                    bundle.putString("message", messageline);
                }
                Message msg = new Message();
                msg.setData(bundle);
                Log.e("message:", msg.getData().toString());
                handler.sendMessage(msg);
            } catch (Exception e) {
                Log.e("log_tag", "the Error parsing data " + e.toString());
            }
        }
    }
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyWebSocketClientService myService = ((MyWebSocketClientService.MyWebSocketClientBinder) service).getService();
            String authorName = myService.getAuthorName();
            Toast.makeText(getActivity(), "author name is: " + authorName, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context,Intent intent){
            // TODO Auto-generated method stub  
            //textView.setText(intent.getExtras().getString("data"));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getNotification(user_phone);
                            } catch (IOException e) {
                                Log.e("log_tag", "the Error parsing data " + e.toString());
                            }
                        }
                    }).start();
        }
    };
}