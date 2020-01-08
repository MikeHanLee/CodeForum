package com.example.codeforum.ui.userInfo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.codeforum.R;
import com.example.codeforum.Utils;
import com.example.codeforum.ui.findFriend.FindFriendActivity;
import com.example.codeforum.ui.login.LoginActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.view.View.GONE;

public class UserInfoActivity extends Activity {

    private ImageView user_info_back;
    private ImageView user_info_user_icon;
    private TextView user_info_user_name;
    private TextView user_info_user_phone;
    private TextView user_info_birth_date;
    private TextView user_info_user_gender;
    private TextView user_info_hometown;
    private Button add_delete_friend;
    private String flag;
    private String phone;
    private UserInfoHandler handler=new UserInfoHandler();
    private String user_phone;
    private SharedPreferences user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user_info);

        user_info_back = (ImageView) findViewById(R.id.user_info_back);
        user_info_user_icon = (ImageView) findViewById(R.id.user_info_user_icon);
        user_info_user_name = (TextView) findViewById(R.id.user_info_user_name);
        user_info_user_phone = (TextView) findViewById(R.id.user_info_user_phone);
        user_info_user_gender = (TextView) findViewById(R.id.user_info_user_gender);
        user_info_birth_date = (TextView) findViewById(R.id.user_info_birth_date);
        user_info_hometown = (TextView) findViewById(R.id.user_info_hometown);
        add_delete_friend = (Button) findViewById(R.id.add_delete_friend);

        user_info_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        user = getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
        }

            Intent intent = getIntent();
        if (intent != null) {
            //flag = intent.getStringExtra("flag");
            phone = intent.getStringExtra("phone");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getInfo();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getInfo() throws IOException {
        Bundle bundle = new Bundle();
        if (phone != null && !phone.equals("")) {
            String urlstr = "http://58.87.100.195/CodeForum/getThisInfo.php";
            String params = "phone=" + phone;
            InputStream is = Utils.connect(urlstr, params);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));//获得输入流
            String line = "";
            StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }
            String result = sb.toString();
            try {
                JSONObject jsonObject = new JSONObject(result);
                int _status = jsonObject.getInt("status");
                if (_status == 1) {
                    String _name = jsonObject.getString("name");
                    String _phone = jsonObject.getString("phone");
                    String _icon = jsonObject.getString("icon");
                    String _birth_date = jsonObject.getString("birth_date");
                    String _gender = jsonObject.getString("gender");
                    String _hometown = jsonObject.getString("hometown");
                    String _friend = jsonObject.getString("friend");
                    bundle.putString("name", _name);
                    bundle.putString("phone", _phone);
                    bundle.putString("icon", _icon);
                    bundle.putString("birth_date", _birth_date);
                    bundle.putString("gender", _gender);
                    bundle.putString("hometown", _hometown);
                    bundle.putString("friend", _friend);

                    Message msg = Message.obtain();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } else {
                    Log.e("getInfoError:", "获取信息失败！");
                }
            } catch (Exception e) {
                Log.e("getInfo", "the Error parsing data " + e.toString());
            }
        }
    }

    private class UserInfoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String _name = msg.getData().getString("name");
            String _phone = msg.getData().getString("phone");
            String _icon = msg.getData().getString("icon");
            String _birth_date = msg.getData().getString("birth_date");
            String _gender = msg.getData().getString("gender");
            String _hometown = msg.getData().getString("hometown");
            String _friend = msg.getData().getString("friend");
            Log.d("friend",_friend);
            Log.d("user_phone",user_phone);
            if(_phone.equals(user_phone)){
                add_delete_friend.setVisibility(View.GONE);
            }else if (_friend.contains(user_phone)) {
                //add_delete_friend.setVisibility(GONE);
                add_delete_friend.setVisibility(View.VISIBLE);
                add_delete_friend.setText("删除该好友");
                add_delete_friend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String result=deleteFriend(phone);
                                    try{
                                        if(result.equals("1")){
                                            Uri data = null;
                                            Intent resultIntent = new Intent(null, data);
                                            setResult(RESULT_OK, resultIntent);
                                            finish();
                                            Looper.prepare();
                                            Toast.makeText(UserInfoActivity.this, "已删除该好友！", Toast.LENGTH_LONG).show();
                                            Looper.loop();
                                        }
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                } catch (IOException e) {
                                    Log.e("log_tag", "the Error parsing data " + e.toString());
                                }
                            }
                        }).start();
                    }
                });
            }else{
                add_delete_friend.setVisibility(View.VISIBLE);
                add_delete_friend.setText("添加为好友");
                final String thisIcon=_icon;
                add_delete_friend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    addFriend(phone,thisIcon);
                                } catch (IOException e) {
                                    Log.e("log_tag", "the Error parsing data " + e.toString());
                                }
                            }
                        }).start();
                    }
                });
            }
            user_info_user_name.setText(_name);
            user_info_user_phone.setText(_phone);
            user_info_user_icon.setImageBitmap(Utils.toRoundBitmap(Utils.stringtoBitmap(_icon)));
            if (_birth_date.equals("null") || _birth_date.equals("")) {
                user_info_birth_date.setVisibility(GONE);
            } else {
                user_info_birth_date.setText(_birth_date);
            }
            if (_gender.equals("man")) {
                user_info_user_gender.setText("男");
            } else if (_gender.equals("woman")) {
                user_info_user_gender.setText("女");
            } else {
                user_info_user_gender.setVisibility(GONE);
            }
            if (!_hometown.equals("null") && _hometown.length() > 0) {
                user_info_hometown.setText(_hometown);
            } else {
                user_info_hometown.setVisibility(GONE);
            }

        }
    }

    private void addFriend(String phone,String icon) throws IOException {
        final String _phone=phone;
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
                        Toast.makeText(UserInfoActivity.this, "添加好友成功！", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(UserInfoActivity.this, "你已经添加过该好友！", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    } else if (resultstatus == -2) {
                        Log.e("addFriend", "你不能添加自己为好友！");
                        Looper.prepare();
                        Toast.makeText(UserInfoActivity.this, "你不能添加自己为好友！", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } catch (Exception e) {
                    Log.e("log_tag", "the Error parsing data " + e.toString());
                }
                    /*}
                }).start();*/
            }
    }

    private String deleteFriend(String phone) throws IOException {
        final String _phone=phone;
        String returnResult="";
        if (!user_phone.equals("默认值")) {
            String urlstr = "http://58.87.100.195/CodeForum/deleteFriend.php";
            String params = "phone=" + phone + "&" + "user_phone=" + user_phone;
            InputStream is = Utils.connect(urlstr, params);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));//获得输入流
            String line = "";
            StringBuilder sb = new StringBuilder();
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
            }
            String result = sb.toString();
            Log.d("result",result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                returnResult = jsonObject.getString("status");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return returnResult;
    }
}
