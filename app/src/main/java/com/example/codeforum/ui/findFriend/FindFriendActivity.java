package com.example.codeforum.ui.findFriend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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

public class FindFriendActivity extends AppCompatActivity {
    private LinearLayout listView;
    private SearchView searchView;
    private ImageView search_friend_back;
    private ActionBar actionBar;
    private FindFriendHandler handler = new FindFriendHandler();
    private Bitmap photo;
    private ProgressBar progressBar;
    private String user_phone;
    private Bitmap _bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        listView = (LinearLayout) findViewById(R.id.search_friend_result);
        actionBar = getSupportActionBar();
        actionBar.hide();
        search_friend_back = (ImageView) findViewById(R.id.search_friend_back);
        search_friend_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.find_friend_progress_bar);
        progressBar.setVisibility(View.GONE);

        searchView = (SearchView) findViewById(R.id.search_friend);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            //输入框提交结果时进行查询结果视图数据更新
            public boolean onQueryTextSubmit(String newText) {
                final String user_name = newText;
                progressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            search(user_name);
                        } catch (Exception e) {
                            Log.e("log_tag", "the Error parsing data " + e.toString());
                        }
                    }
                });
                thread.start();
                return false;
            }

            @Override
            //输入框内输入文本或者删除文本时执行向服务器查询数据
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    //查找新的好友
    public void search(String user_name) throws IOException {
        Bundle bundle = new Bundle();
        String urlstr = "http://58.87.100.195/CodeForum/newFriend.php";
        String params = "name=" + user_name;
        InputStream is = Utils.connect(urlstr, params);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));//获得输入流
        String line = "";
        StringBuilder sb = new StringBuilder();//建立输入缓冲区
        while (null != (line = bufferedReader.readLine())) {//结束会读入一个null值
            sb.append(line);//写缓冲区
        }
        String result = sb.toString();//返回结果
        try {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsonObject = new JSONObject(jsonArray.getString(0));
            String[] nameArray = new String[jsonArray.length()];
            String[] phoneArray = new String[jsonArray.length()];
            String[] iconArray = new String[jsonArray.length()];
            String[] statusArray = new String[jsonArray.length()];
            if (jsonObject.getString("status").equals("1")) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = new JSONObject(jsonArray.getString(i));
                    nameArray[i] = jsonObject.getString("name");
                    phoneArray[i] = jsonObject.getString("phone");
                    iconArray[i] = jsonObject.getString("icon");
                    statusArray[i] = jsonObject.getString("status");
                }
                bundle.putStringArray("name", nameArray);
                bundle.putStringArray("phone", phoneArray);
                bundle.putStringArray("icon", iconArray);
                bundle.putStringArray("status", statusArray);
            }
            Message msg = Message.obtain();
            msg.what = 0;
            msg.setData(bundle);
            handler.sendMessage(msg);
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
    }

    //添加好友
    private void addFriend(String phone,String icon) throws IOException {
        SharedPreferences user = getSharedPreferences("user", 0);
        final String _phone=phone;
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
            if (!user_phone.equals("默认值")) {
                String urlstr = "http://58.87.100.195/CodeForum/addFriend.php";
                String params = "phone=" + phone + "&" + "name=" + user_phone;
                InputStream is = Utils.connect(urlstr, params);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));//获得输入流
                String line = "";
                StringBuilder sb = new StringBuilder();
                while (null != (line = bufferedReader.readLine())) {
                    sb.append(line);
                }
                final String result = sb.toString();
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {*/
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            final int resultstatus = jsonObject.getInt("status");
                            if (resultstatus == 1) {
                                if(user_phone!=null&&user_phone.length()>0) {
                                    final String iconPath = getApplicationContext().getExternalFilesDir("") + "/" + user_phone + "/friend/" + _phone + "/userIcon";
                                    File iconFile = new File(new File(iconPath), "image.jpg");
                                    Utils.savePhoto(Utils.stringtoBitmap(icon), iconPath, "image");
                                }
                                Log.i("addFriend", "添加好友成功！");
                                Uri data = null;
                                Intent resultIntent = new Intent(null, data);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                                Looper.prepare();
                                Toast.makeText(FindFriendActivity.this, "添加好友成功！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                                /*new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try{
                                            _bitmap = getFile(_phone);
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                        if(user_phone!=null&&user_phone.length()>0) {
                                            final String iconPath = getApplicationContext().getExternalFilesDir("") + "/" + user_phone + "/friend/" + _phone + "/croppedImage";
                                            File icon = new File(new File(iconPath), "image.jpg");
                                            Utils.savePhoto(_bitmap, iconPath, "image");
                                        }
                                        Log.e("log_tag", "添加好友成功！");
                                        Uri data = null;
                                        Intent result = new Intent(null, data);
                                        setResult(RESULT_OK, result);
                                        finish();
                                        Looper.prepare();
                                        Toast.makeText(FindFriendActivity.this, "添加好友成功！", Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                    }
                                }).start();*/
                            } else if (resultstatus == -1) {
                                Log.e("addFriend", "你已经添加过该好友！");
                                Looper.prepare();
                                Toast.makeText(FindFriendActivity.this, "你已经添加过该好友！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (resultstatus == -2) {
                                Log.e("addFriend", "你不能添加自己为好友！");
                                Looper.prepare();
                                Toast.makeText(FindFriendActivity.this, "你不能添加自己为好友！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (Exception e) {
                            Log.e("log_tag", "the Error parsing data " + e.toString());
                        }
                    /*}
                }).start();*/
            }
        }
    }

    private Bitmap getFile(String user_phone) throws IOException {
        String urlstr = "http://58.87.100.195/CodeForum/getFile.php";
        String params = "name=" + user_phone;
        InputStream is = Utils.connect(urlstr, params);
        if(!is.toString().contains("ChunkedSource")){
            return BitmapFactory.decodeStream(is);
        }else{
            return null;
        }
    }

    private class FindFriendHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    if (msg.getData().getStringArray("name")!=null) {
                        String[] name = msg.getData().getStringArray("name");
                        String[] phone = msg.getData().getStringArray("phone");
                        String[] icon = msg.getData().getStringArray("icon");
                        String[] status = msg.getData().getStringArray("status");
                        NotificationView[] notificationView = new NotificationView[name.length];
                        listView.removeAllViewsInLayout();
                        for (int i = 0; i < name.length; i++) {
                            notificationView[i] = new NotificationView(FindFriendActivity.this);
                            notificationView[i].setNameText(name[i]);
                            notificationView[i].setPhoneText(phone[i]);
                            notificationView[i].setImageResource(Utils.toRoundBitmap(Utils.stringtoBitmap(icon[i])));
                            notificationView[i].setMessageText("");
                            final int _index=i;
                            final int end_index=name.length;
                            final String _phone=phone[i];
                            /*if(_phone!=null) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Bitmap _user_photo = getFile(_phone);
                                            Message msg = Message.obtain();
                                            JSONObject _obj = new JSONObject();
                                            msg.what = 1;
                                            msg.arg1 = _index;
                                            if(_index==end_index-1){
                                                msg.arg2=1;
                                            }else{
                                                msg.arg2=0;
                                            }
                                            Bundle bundle = new Bundle();
                                            try {
                                                if(_user_photo!=null) {
                                                    bundle.putString("img", Utils.bitmaptoString(_user_photo));
                                                }else{
                                                    bundle.putString("img",null);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            msg.setData(bundle);
                                            handler.sendMessage(msg);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }*/
                            /*final String _name = name[i];
                            final String _status = status[i];*/
                            final String thisIcon=icon[i];
                            notificationView[i].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                addFriend(_phone,thisIcon);
                                            } catch (IOException e) {
                                                Log.e("log_tag", "the Error parsing data " + e.toString());
                                            }
                                        }
                                    }).start();
                                }
                            });
                            listView.addView(notificationView[i]);
                        }
                        progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        listView.removeAllViews();
                    }
                    break;
                }
                /*case 1:{
                    NotificationView _notificationView=(NotificationView)listView.getChildAt(msg.arg1);
                    if(_notificationView!=null) {
                        if(msg.getData().getString("img")!=null){
                            _notificationView.setImageResource(Utils.stringtoBitmap(msg.getData().getString("img")));
                        }else{
                            _notificationView.setImage(R.drawable.ic_default_personal_icon_256dp);
                        }
                    }
                    if(msg.arg2!=0) {
                        Log.e("log_tag", "查找完毕!");
                        progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                Toast.makeText(FindFriendActivity.this, "查找完毕!", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }).start();
                    }
                    break;
                }
                case 2:{
                    progressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    break;
                }*/
                default:
                    break;
            }

        }
    }
}
