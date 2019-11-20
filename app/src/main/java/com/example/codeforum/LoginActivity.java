package com.example.codeforum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private Button login;
    private Button register_login;
    private EditText et;
    private EditText pwd;
    private static final int registerActivity=3;
    @Override
    //读取注册页面返回的结果
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case(registerActivity):{
                if(resultCode== Activity.RESULT_OK){
                    String name=data.getStringExtra("name");
                    String phone=data.getStringExtra("phone");
                    returnMainActivity(name);
                    setUserInfo(name,phone);
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et = (EditText) findViewById(R.id.user_name);
        pwd = (EditText) findViewById(R.id.user_pwd);
        login = findViewById(R.id.login);
        register_login=findViewById(R.id.register_login);

        //提交登录信息后进行登录处理
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject=login();
                            int result=0;
                            String name="";
                            String phone="";
                            try{
                                result = jsonObject.getInt("status");
                                name = jsonObject.getString("name");
                                phone=jsonObject.getString("phone");
                            }catch(Exception e){
                                Log.e("log_tag", "the Error parsing data " + e.toString());
                            }
                            if (result == 1) {
                                Log.e("log_tag", "登陆成功！");
                                returnMainActivity(name);
                                setUserInfo(name,phone);
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (result == -2) {
                                Log.e("log_tag", "密码错误！");
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this, "密码错误！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (result == -1) {
                                Log.e("log_tag", "不存在该用户！");
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this, "不存在该用户！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }).start();
            }
        });

        //注册跳转
        register_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent,registerActivity);
            }
        });
    }

    //登录
    private JSONObject login() throws IOException {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("status",0);
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }

        String user_id = et.getText().toString();
        String input_pwd = pwd.getText().toString();
        if (user_id == null || user_id.length() <= 0) {
            Looper.prepare();
            Toast.makeText(LoginActivity.this, "请输入账号", Toast.LENGTH_LONG).show();
            Looper.loop();
            return jsonObject;
        }
        if (input_pwd == null || input_pwd.length() <= 0) {
            Looper.prepare();
            Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_LONG).show();
            Looper.loop();
            return jsonObject;
        }

        String urlstr = "http://58.87.100.195/CodeForum/login.php";
        URL url = new URL(urlstr);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        String params = "uid=" + user_id + '&' + "pwd=" + input_pwd;
        http.setDoOutput(true);
        http.setDoInput(true);
        http.setRequestMethod("POST");
        http.connect();

        OutputStream out = http.getOutputStream();
        out.write(params.getBytes());//post提交参数
        out.flush();
        out.close();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
        String line = "";
        StringBuilder sb = new StringBuilder();
        while (null != (line = bufferedReader.readLine())) {
            sb.append(line);
        }
        String result = sb.toString();
        try {
            jsonObject = new JSONObject(result);
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }

        return jsonObject;
    }

    //返回主页面
    private void returnMainActivity(String name){
        Uri data=null;
        Intent result = new Intent(null,data);
        result.putExtra("name",name);
        setResult(RESULT_OK,result);
        finish();
    }

    //将登录信息存入SharedPreferences
    private void setUserInfo(String name,String phone){
        SharedPreferences sharedPreferences =getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", name);
        editor.putString("user_phone",phone);
        editor.commit();
    }
}
