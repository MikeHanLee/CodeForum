package com.example.codeforum;

import android.content.Intent;
import android.content.SharedPreferences;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et = (EditText) findViewById(R.id.user_name);
        pwd = (EditText) findViewById(R.id.user_pwd);

        login = findViewById(R.id.login);
        register_login=findViewById(R.id.register_login);
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
                                // TODO: handle exception
                                Log.e("log_tag", "the Error parsing data " + e.toString());
                            }
                            //login()为向php服务器提交请求的函数，返回数据类型为int
                            if (result == 1) {
                                Log.e("log_tag", "登陆成功！");
                                //Toast toast=null;
                                toMainActivity(name);
                                setUserInfo(name,phone);
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (result == -2) {
                                Log.e("log_tag", "密码错误！");
                                //Toast toast=null;
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this, "密码错误！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (result == -1) {
                                Log.e("log_tag", "不存在该用户！");
                                //Toast toast=null;
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
        register_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private JSONObject login() throws IOException {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("status",0);
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }

        /*获取用户名和密码*/
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
        //建立网络连接
        URL url = new URL(urlstr);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        //往网页写入POST数据，和网页POST方法类似，参数间用‘&’连接
        String params = "uid=" + user_id + '&' + "pwd=" + input_pwd;
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
            /*获取服务器返回的JSON数据*/
            jsonObject = new JSONObject(result);
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }

        return jsonObject;
    }

    private void toMainActivity(String name){
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, MainActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("name",name);
        bundle.putInt("id",1);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    private void setUserInfo(String name,String phone){
        SharedPreferences sharedPreferences =getSharedPreferences("user", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //如果不能找到Editor接口。尝试使用 SharedPreferences.Editor
        editor.putString("user_id", name);
        editor.putString("user_phone",phone);
        //我将用户信息保存到其中，你也可以保存登录状态
        editor.commit();
    }
}
