package com.example.codeforum.ui.friend;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.codeforum.ui.communication.CommunicationActivity;
import com.example.codeforum.ui.findFriend.FindFriendActivity;
import com.example.codeforum.R;
import com.example.codeforum.Utils;
import com.example.codeforum.component.notificationView.NotificationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FriendFragment extends Fragment {

    private FriendViewModel friendViewModel;
    private LinearLayout my_friend;
    private FloatingActionButton find_friend;
    private String user_phone = "";
    private SharedPreferences user;
    private FriendHandler handler = new FriendHandler();
    private static final int findFriendActivity = 2;
    private final static int communicationActivity = 4;
    private Thread pleaseLoginThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            Toast.makeText(getActivity(), "请先登录", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    });

    @Override
    //获取查找新的好友页面的返回结果
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case (findFriendActivity): {
                if (resultCode == Activity.RESULT_OK) {
                    SharedPreferences user = getActivity().getSharedPreferences("user", 0);
                    if (user != null) {
                        user_phone = user.getString("user_phone", "默认值");
                        if (!user_phone.equals("默认值")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        getFriend(user_phone);
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
        friendViewModel =
                ViewModelProviders.of(this).get(FriendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_friend, container, false);
        /*final TextView textView = root.findViewById(R.id.text_friend);
        friendViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/


        //获取好友列表后进行视图数据更新
        my_friend = (LinearLayout) root.findViewById(R.id.my_friend);
        my_friend.removeAllViewsInLayout();
        //获取当前用户电话
        user = getActivity().getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
            if (!user_phone.equals("默认值")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getFriend(user_phone);
                        } catch (IOException e) {
                            Log.e("log_tag", "the Error parsing data " + e.toString());
                        }
                    }
                }).start();
            }
        }

        //查找并添加新的好友
        find_friend = (FloatingActionButton) root.findViewById(R.id.find_friend);
        find_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("user_phone", user_phone);
                if (user != null) {
                    if (user_phone.equals("默认值")) {
                        if(pleaseLoginThread.getState().toString().equals("NEW")){
                            pleaseLoginThread.start();
                        }
                    } else {
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), FindFriendActivity.class);
                        startActivityForResult(intent, findFriendActivity);
                    }
                } else {
                    if(pleaseLoginThread.getState().toString().equals("NEW")){
                        pleaseLoginThread.start();
                    }
                }
            }
        });

        return root;
    }

    //获取好友列表
    private void getFriend(String user_phone) throws IOException {
        Bundle bundle = new Bundle();
        if (!user_phone.equals("默认值")) {
            String urlstr = "http://58.87.100.195/CodeForum/getFriend.php";
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
                String[] nameArray = new String[jsonArray.length()];
                String[] phoneArray = new String[jsonArray.length()];
                String[] iconArray = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                    nameArray[i] = jsonObject.getString("name");
                    phoneArray[i] = jsonObject.getString("phone");
                    iconArray[i] = jsonObject.getString("icon");
                }
                bundle.putStringArray("name", nameArray);
                bundle.putStringArray("phone", phoneArray);
                bundle.putStringArray("icon", iconArray);
                Message msg = new Message();
                msg.setData(bundle);
                handler.sendMessage(msg);
            } catch (Exception e) {
                Log.e("log_tag", "the Error parsing data " + e.toString());
            }
        }
    }

    private class FriendHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String[] name = msg.getData().getStringArray("name");
            String[] phone = msg.getData().getStringArray("phone");
            String[] icon = msg.getData().getStringArray("icon");
            my_friend.removeAllViewsInLayout();
            NotificationView[] notificationView = new NotificationView[name.length];
            for (int i = 0; i < name.length; i++) {
                if (!name[i].equals("null")) {
                    notificationView[i] = new NotificationView(getContext());
                    notificationView[i].setNameText(name[i]);
                    notificationView[i].setPhoneText(phone[i]);
                    notificationView[i].setImageResource(Utils.toRoundBitmap(Utils.stringtoBitmap(icon[i])));
                    final String iconPath = getContext().getExternalFilesDir("") + "/" + user_phone + "/friend/" + phone[i] + "/userIcon";
                    File iconFile = new File(new File(iconPath), "image.jpg");
                    if (!iconFile.exists()) {
                        Bitmap local_bitmap = BitmapFactory.decodeFile(iconPath + "/image.jpg");
                        Bitmap bitmap = Utils.stringtoBitmap(icon[i]);
                        if (local_bitmap != null) {
                            if (bitmap != local_bitmap) {
                                Utils.savePhoto(bitmap, iconPath, "image");
                            }
                        } else {
                            Utils.savePhoto(bitmap, iconPath, "image");
                        }
                    }
                    final String _name = name[i];
                    final String _phone = phone[i];
                    notificationView[i].setMessageText("");
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
                    my_friend.addView(notificationView[i]);
                }
            }
        }
    }
}