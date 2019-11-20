package com.example.codeforum;

import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity {
    private Button register;
    private EditText register_user_name;
    private EditText register_user_pwd;
    private EditText register_twice_user_pwd;
    private EditText register_cellphone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        register=(Button)findViewById(R.id.register);
        register_user_name = (EditText) findViewById(R.id.register_user_name);
        register_user_pwd = (EditText) findViewById(R.id.register_user_pwd);
        register_twice_user_pwd=(EditText)findViewById(R.id.register_twice_user_pwd);
        register_cellphone=(EditText)findViewById(R.id.register_cellphone);

        //提交注册信息后执行注册处理
        register.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            int result=register();
                            if(result==1){
                                Log.e("log_tag", "注册成功！");
                                String user_name=register_user_name.getText().toString();
                                String cellphone=register_cellphone.getText().toString();
                                Uri data=null;
                                Intent intent = new Intent(null,data);
                                intent.putExtra("name",user_name);
                                intent.putExtra("phone",cellphone);
                                setResult(RESULT_OK,intent);
                                finish();
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }else if(result==-1){
                                Log.e("log_tag", "手机号已被注册！");
                                //Toast toast=null;
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "手机号已被注册！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }else if(result==-2){
                                Log.e("log_tag", "用户名已被注册！");
                                //Toast toast=null;
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "用户名已被注册！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }).start();
            }
        });
    }

    //注册
    private int register()throws IOException{
        String user_name=register_user_name.getText().toString();
        String user_pwd=register_user_pwd.getText().toString();
        String twice_user_pwd=register_twice_user_pwd.getText().toString();
        String cellphone=register_cellphone.getText().toString();
        int returnResult=0;
        if(user_name==null || user_name.length() <= 0){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "请输入用户名", Toast.LENGTH_LONG).show();
            Looper.loop();
            return returnResult;
        }
        if(user_pwd==null || user_pwd.length() <= 0){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "请输入密码", Toast.LENGTH_LONG).show();
            Looper.loop();
            return returnResult;
        }
        if(twice_user_pwd==null || twice_user_pwd.length() <= 0){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "请再次输入密码", Toast.LENGTH_LONG).show();
            Looper.loop();
            return returnResult;
        }
        if(cellphone==null || cellphone.length() <= 0){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "请输入手机号", Toast.LENGTH_LONG).show();
            Looper.loop();
            return returnResult;
        }
        if(!twice_user_pwd.equals(user_pwd)){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "两次密码输入不一致，请重新输入", Toast.LENGTH_LONG).show();
            Looper.loop();
            return returnResult;
        }
        String urlstr = "http://58.87.100.195/CodeForum/register.php";
        URL url = new URL(urlstr);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        String params = "name=" + user_name + '&' + "pwd=" + user_pwd+ '&' + "phone=" + cellphone;
        Log.e("error",params);
        http.setDoOutput(true);
        http.setDoInput(true);
        http.setRequestMethod("POST");
        http.connect();

        OutputStream out = http.getOutputStream();
        out.write(params.getBytes());
        out.flush();
        out.close();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));
        String line = "";
        StringBuilder sb = new StringBuilder();
        while (null != (line = bufferedReader.readLine())) {
            sb.append(line);
        }
        Log.e("error",sb.toString());
        String result = sb.toString();
        Log.e("error",result);
        try {
            JSONObject jsonObject = new JSONObject(result);
            returnResult = jsonObject.getInt("status");
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
        return returnResult;
    }
}
