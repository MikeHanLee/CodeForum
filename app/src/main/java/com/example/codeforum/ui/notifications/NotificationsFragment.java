package com.example.codeforum.ui.notifications;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.codeforum.ui.communication.CommunicationActivity;
import com.example.codeforum.service.MyWebSocketClientService;
import com.example.codeforum.R;
import com.example.codeforum.Utils;
import com.example.codeforum.component.notificationView.NotificationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private NotifivationsHandler handler = new NotifivationsHandler();
    private LinearLayout my_notifications;
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

        my_notifications = (LinearLayout) root.findViewById(R.id.my_notifications);
        my_notifications.removeAllViewsInLayout();
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
        if (user != null) {
            IntentFilter filter = new IntentFilter("com.example.codeforum.servicecallback.content");
            getActivity().registerReceiver(broadcastReceiver, filter);
        }
        return root;
    }

    //获取新消息列表
    private void getNotification(String user_phone) throws IOException {
        Bundle bundle = new Bundle();
        if (!user_phone.equals("默认值")) {
            String urlstr = "http://58.87.100.195/CodeForum/getMessage.php";
            String params = "phone=" + user_phone;
            InputStream is = Utils.connect(urlstr, params);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));//获得输入流
            String line = "";
            StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }
            String result = sb.toString();
            try {
                JSONArray jsonArray = new JSONArray(result);
                String[] nameArray = new String[jsonArray.length()];
                String[] phoneArray = new String[jsonArray.length()];
                String[] statusArray = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                    nameArray[i] = jsonObject.getString("name");
                    phoneArray[i] = jsonObject.getString("phone");
                    statusArray[i] = jsonObject.getString("status");
                }
                bundle.putStringArray("name", nameArray);
                bundle.putStringArray("phone", phoneArray);
                bundle.putStringArray("status", statusArray);
                Message msg = Message.obtain();
                msg.setData(bundle);
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
        public void onReceive(Context context, Intent intent) {
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

    private class NotifivationsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String[] name = msg.getData().getStringArray("name");
            String[] phone = msg.getData().getStringArray("phone");
            String[] status = msg.getData().getStringArray("status");
            Bitmap bitmap;
            NotificationView[] notificationView = new NotificationView[name.length];
            for(int i=0;i<name.length;i++){
                notificationView[i]=new NotificationView(getContext());
                notificationView[i].setNameText(name[i]);
                notificationView[i].setPhoneText(phone[i]);
                final String iconPath = getContext().getExternalFilesDir("") + "/" + user_phone + "/friend/" + phone[i] + "/userIcon";
                File icon = new File(new File(iconPath), "image.jpg");
                if (icon.exists()) {
                    bitmap = BitmapFactory.decodeFile(iconPath + "/image.jpg");
                    if (bitmap != null) {
                        bitmap = Utils.toRoundBitmap(bitmap);
                        notificationView[i].setImageResource(bitmap);
                    }
                }
                final String _name = name[i];
                final String _phone = phone[i];
                final String _status = status[i];
                if (status[i].equals("0")) {
                    notificationView[i].setMessageText("新消息");
                } else {
                    notificationView[i].setMessageText("");
                }
                if (!phone[i].equals("")) {
                    notificationView[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), CommunicationActivity.class);
                            intent.putExtra("name", _name);
                            intent.putExtra("phone", _phone);
                            startActivityForResult(intent, communicationActivity);
                        }
                    });
                }
                my_notifications.addView(notificationView[i]);
            }
        }
    }
}