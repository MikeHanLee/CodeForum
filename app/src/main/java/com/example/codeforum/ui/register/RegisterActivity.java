package com.example.codeforum.ui.register;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.codeforum.R;
import com.example.codeforum.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class RegisterActivity extends AppCompatActivity {
    private Button register;
    private EditText register_user_name;
    private EditText register_user_pwd;
    private EditText register_twice_user_pwd;
    private EditText register_cellphone;
    private ImageView register_hide;
    private ImageView register_show;
    private ImageView register_hide_twice;
    private ImageView register_show_twice;
    private ImageView register_back;
    private Button register_send_sms;
    private EditText register_sms;
    private ActionBar actionBar;
    private Boolean flag;
    private String phoneNumber;
    private Handler mHandler;
    private String verificationCode;
    private String user_name;
    private String user_pwd;
    private String twice_user_pwd;
    private String cellphone;
    private String default_icon;

    private CountDownTimer timer = new CountDownTimer(60*1000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            register_send_sms.setText("再次发送("+millisUntilFinished/1000+"s)");
            register_send_sms.setClickable(false);
            register_send_sms.setBackgroundResource(R.drawable.button_enabled);
            register_send_sms.setTextColor(getResources().getColor(R.color.colorDarkGrey));
        }

        @Override
        public void onFinish() {
            register_send_sms.setText("再次发送");
            register_send_sms.setClickable(true);
            register_send_sms.setBackgroundResource(R.drawable.button);
            register_send_sms.setTextColor(getResources().getColor(R.color.colorWhite));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        actionBar=getSupportActionBar();
        actionBar.hide();
        register_back = (ImageView) findViewById(R.id.register_back);
        register_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        register=(Button)findViewById(R.id.register);
        register_user_name = (EditText) findViewById(R.id.register_user_name);
        register_user_pwd = (EditText) findViewById(R.id.register_user_pwd);
        register_twice_user_pwd=(EditText)findViewById(R.id.register_twice_user_pwd);
        register_cellphone=(EditText)findViewById(R.id.register_cellphone);
        register_send_sms=(Button)findViewById(R.id.register_send_sms);
        register_sms=(EditText)findViewById(R.id.register_sms);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;

                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 如果操作成功
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        // 校验验证码，返回校验的手机和国家代码

                        Toast.makeText(RegisterActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    int result=register(default_icon);
                                    if(result==1){
                                        Log.i("register", "注册成功！");
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
                                        Log.e("register", "手机号已被注册！");
                                        Looper.prepare();
                                        Toast.makeText(RegisterActivity.this, "手机号已被注册！", Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                    }else if(result==-2){
                                        Log.e("register", "用户名已被注册！");
                                        Looper.prepare();
                                        Toast.makeText(RegisterActivity.this, "用户名已被注册！", Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                    }
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }).start();
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        // 获取验证码成功，true为智能验证，false为普通下发短信
                        Toast.makeText(RegisterActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                    } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                        // 返回支持发送验证码的国家列表
                    }
                } else {
                    // 如果操作失败
                    if (flag) {
                        Toast.makeText(RegisterActivity.this, "验证码获取失败，请重新获取", Toast.LENGTH_SHORT).show();
                        register_sms.requestFocus();
                    } else {
                        ((Throwable) data).printStackTrace();
                        Toast.makeText(RegisterActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        EventHandler eh=new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                // TODO 此处不可直接处理UI线程，处理后续操作需传到主线程中操作
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh);


        register_hide=(ImageView)findViewById(R.id.register_hide);
        register_show=(ImageView)findViewById(R.id.register_show);
        register_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register_hide.setVisibility(View.GONE);
                register_show.setVisibility(View.VISIBLE);
                register_user_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });
        register_show.setVisibility(View.GONE);
        register_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register_hide.setVisibility(View.VISIBLE);
                register_show.setVisibility(View.GONE);
                register_user_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        register_hide_twice=(ImageView)findViewById(R.id.register_hide_twice);
        register_show_twice=(ImageView)findViewById(R.id.register_show_twice);
        register_hide_twice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register_hide_twice.setVisibility(View.GONE);
                register_show_twice.setVisibility(View.VISIBLE);
                register_twice_user_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });
        register_show_twice.setVisibility(View.GONE);
        register_show_twice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register_hide_twice.setVisibility(View.VISIBLE);
                register_show_twice.setVisibility(View.GONE);
                register_twice_user_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        register_send_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(register_cellphone.getText())) {
                    if (Utils.isMobile(register_cellphone.getText().toString())) {
                        phoneNumber = register_cellphone.getText().toString();
                        SMSSDK.getVerificationCode("86", phoneNumber); // 发送验证码给号码的 phoneNumber 的手机
                        register_sms.requestFocus();
                        timer.start();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "请输入完整的电话号码", Toast.LENGTH_SHORT).show();
                        register_cellphone.requestFocus();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "请输入电话号码", Toast.LENGTH_SHORT).show();
                    register_cellphone.requestFocus();
                }
            }
        });

        Bitmap bitmap;
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
            Drawable vectorDrawable = getApplicationContext().getDrawable(R.drawable.ic_default_personal_icon_256dp);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        }else {
            bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_default_personal_icon_256dp);
        }
        default_icon=Utils.bitmaptoStringTenPercent(bitmap);
        //提交注册信息后执行注册处理
        register.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        beforeRegister();
                        /*try {
                            int result=register(default_icon);
                            if(result==1){
                                Log.i("register", "注册成功！");
                                String user_name=register_user_name.getText().toString();
                                String cellphone=register_cellphone.getText().toString();
                                String iconString=default_icon;
                                String iconPath = getApplicationContext().getExternalFilesDir("") + "/" + cellphone + "/userIcon";
                                File icon_file = new File(new File(iconPath), "image.jpg");
                                if (icon_file.exists()) {
                                    Bitmap local_photo = BitmapFactory.decodeFile(iconPath + "/image.jpg");
                                    Bitmap photo = Utils.stringtoBitmap(iconString);
                                    if (photo != local_photo) {
                                        Utils.savePhoto(photo, iconPath, "image");
                                    }
                                } else {
                                    Bitmap photo = Utils.stringtoBitmap(iconString);
                                    Utils.savePhoto(photo, iconPath, "image");
                                }
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
                                Log.e("register", "手机号已被注册！");
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "手机号已被注册！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }else if(result==-2){
                                Log.e("register", "用户名已被注册！");
                                Looper.prepare();
                                Toast.makeText(RegisterActivity.this, "用户名已被注册！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }catch(IOException e){
                            e.printStackTrace();
                        }*/
                    }
                }).start();
            }
        });
    }

    //注册
    private void beforeRegister(){
        user_name=register_user_name.getText().toString();
        user_pwd=register_user_pwd.getText().toString();
        twice_user_pwd=register_twice_user_pwd.getText().toString();
        cellphone=register_cellphone.getText().toString();
        if(user_name==null || user_name.length() <= 0){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "请输入用户名", Toast.LENGTH_LONG).show();
            Looper.loop();
            return;
        }
        if(user_pwd==null || user_pwd.length() <= 0){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "请输入密码", Toast.LENGTH_LONG).show();
            Looper.loop();
            return;
        }
        if(twice_user_pwd==null || twice_user_pwd.length() <= 0){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "请再次输入密码", Toast.LENGTH_LONG).show();
            Looper.loop();
            return;
        }
        if(cellphone==null || cellphone.length() <= 0){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "请输入手机号", Toast.LENGTH_LONG).show();
            Looper.loop();
            return;
        }
        if(!Utils.isMobile(cellphone)){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "请输入正确的手机号", Toast.LENGTH_LONG).show();
            Looper.loop();
            return;
        }
        if(!isPassword(user_pwd)){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "请输入8到16位由数字和字母组成的密码", Toast.LENGTH_LONG).show();
            Looper.loop();
            return;
        }
        if(!twice_user_pwd.equals(user_pwd)){
            Looper.prepare();
            Toast.makeText(RegisterActivity.this, "两次密码输入不一致，请重新输入", Toast.LENGTH_LONG).show();
            Looper.loop();
            return;
        }
        if (!TextUtils.isEmpty(register_sms.getText())) {
            if (register_sms.getText().length() == 4) {
                verificationCode = register_sms.getText().toString();
                SMSSDK.submitVerificationCode("86", phoneNumber, verificationCode);
                flag = false;
            } else {
                Toast.makeText(this, "请输入完整的验证码", Toast.LENGTH_SHORT).show();
                register_sms.requestFocus();
            }
            Looper.prepare();
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            Looper.loop();
            register_sms.requestFocus();
            //return returnResult;
        }
    }

    private int register(String icon)throws IOException{
        /*user_name=register_user_name.getText().toString();
        user_pwd=register_user_pwd.getText().toString();
        twice_user_pwd=register_twice_user_pwd.getText().toString();
        cellphone=register_cellphone.getText().toString();*/
        int returnResult=0;
        String urlstr = "http://58.87.100.195/CodeForum/register.php";
        String params = "name=" + user_name + '&' + "pwd=" + user_pwd+ '&' + "phone=" + cellphone + '&' + "icon=" + icon;
        InputStream is= Utils.connect(urlstr,params);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line = "";
        StringBuilder sb = new StringBuilder();
        while (null != (line = bufferedReader.readLine())) {
            sb.append(line);
        }
        String result = sb.toString();
        try {
            JSONObject jsonObject = new JSONObject(result);
            returnResult = jsonObject.getInt("status");
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
        return returnResult;
    }

    public static boolean isPassword(final String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$");
        m = p.matcher(str);
        b = m.matches();
        return b;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();  // 注销回调接口
    }
}
