package com.example.codeforum.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.codeforum.R;
import com.example.codeforum.ui.register.RegisterActivity;
import com.example.codeforum.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class LoginActivity extends AppCompatActivity {
    private Button login;
    private Button register_login;
    private Button code_send_sms;
    private Button code_login;
    private Button code_register_login;
    private Button code_tab;
    private Button pwd_tab;
    private EditText et;
    private EditText pwd;
    private EditText code_et;
    private EditText code_sms;
    private ImageView login_show_hide_grey;
    private ImageView login_show_hide_blue;
    private ImageView login_back;
    private ActionBar actionBar;
    private Handler mHandler;
    private String verificationCode;
    private Boolean flag;
    private String phoneNumber;
    private ViewPager mvpGoods;
    private ArrayList<View> mivGoodsList;
    private GoodsAdapter mAdapter;
    private String user_phone;

    private static final int registerActivity=3;

    private CountDownTimer timer = new CountDownTimer(60*1000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            code_send_sms.setText("再次发送("+millisUntilFinished/1000+"s)");
            code_send_sms.setClickable(false);
            code_send_sms.setBackgroundResource(R.drawable.button_enabled);
            code_send_sms.setTextColor(getResources().getColor(R.color.colorDarkGrey));
        }

        @Override
        public void onFinish() {
            code_send_sms.setText("再次发送");
            code_send_sms.setClickable(true);
            code_send_sms.setBackgroundResource(R.drawable.button);
            code_send_sms.setTextColor(getResources().getColor(R.color.colorWhite));
        }
    };

    @Override
    //读取注册页面返回的结果
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case(registerActivity):{
                if(resultCode== Activity.RESULT_OK){
                    String name=data.getStringExtra("name");
                    String phone=data.getStringExtra("phone");
                    setUserInfo(name,phone);
                    returnMainActivity(name);
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        actionBar=getSupportActionBar();
        actionBar.hide();
        login_back = (ImageView) findViewById(R.id.login_back);
        login_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        mivGoodsList=new ArrayList<>();
        LayoutInflater inflater=getLayoutInflater();
        View view1=inflater.inflate(R.layout.login_pwd,null);
        View view2=inflater.inflate(R.layout.login_code,null);
        mivGoodsList.add(view1);
        mivGoodsList.add(view2);
        mAdapter=new GoodsAdapter(this,mivGoodsList);
        mvpGoods= (ViewPager) findViewById(R.id.login_viewpager);
        mvpGoods.setAdapter(mAdapter);
        mvpGoods.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==0){
                    code_tab.setBackgroundResource(R.drawable.button_no_radius_enabled);
                    code_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                    pwd_tab.setBackgroundResource(R.drawable.button_no_radius);
                    pwd_tab.setTextColor(getResources().getColor(R.color.colorWhite));
                }else{
                    pwd_tab.setBackgroundResource(R.drawable.button_no_radius_enabled);
                    pwd_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                    code_tab.setBackgroundResource(R.drawable.button_no_radius);
                    code_tab.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        pwd_tab=findViewById(R.id.pwd_tab);
        code_tab=findViewById(R.id.code_tab);
        pwd_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mvpGoods.setCurrentItem(0);
                code_tab.setBackgroundResource(R.drawable.button_no_radius_enabled);
                code_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                pwd_tab.setBackgroundResource(R.drawable.button_no_radius);
                pwd_tab.setTextColor(getResources().getColor(R.color.colorWhite));
            }
        });

        code_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mvpGoods.setCurrentItem(1);
                pwd_tab.setBackgroundResource(R.drawable.button_no_radius_enabled);
                pwd_tab.setTextColor(getResources().getColor(R.color.colorPrimary));
                code_tab.setBackgroundResource(R.drawable.button_no_radius);
                code_tab.setTextColor(getResources().getColor(R.color.colorWhite));
            }
        });

        et = (EditText) view1.findViewById(R.id.user_name);
        pwd = (EditText) view1.findViewById(R.id.user_pwd);
        login = (Button)view1.findViewById(R.id.login);
        register_login=(Button)view1.findViewById(R.id.register_login);
        code_et = (EditText) view2.findViewById(R.id.code_user_phone);
        code_sms = (EditText) view2.findViewById(R.id.code_sms);
        code_login = (Button)view2.findViewById(R.id.code_login);
        code_send_sms=(Button)view2.findViewById(R.id.code_send_sms);
        code_register_login=(Button)view2.findViewById(R.id.code_register_login);
        login_show_hide_grey=(ImageView)view1.findViewById(R.id.login_show_hide_grey);
        login_show_hide_blue=(ImageView)view1.findViewById(R.id.login_show_hide_blue);
        login_show_hide_grey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    login_show_hide_grey.setVisibility(View.GONE);
                    login_show_hide_blue.setVisibility(View.VISIBLE);
                    pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });
        login_show_hide_blue.setVisibility(View.GONE);
        login_show_hide_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_show_hide_grey.setVisibility(View.VISIBLE);
                login_show_hide_blue.setVisibility(View.GONE);
                pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

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
                        Toast.makeText(LoginActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonObject=codeLogin();
                                    int result=0;
                                    String name="";
                                    String phone="";
                                    String icon="";
                                    try{
                                        result = jsonObject.getInt("status");
                                        name = jsonObject.getString("name");
                                        phone=jsonObject.getString("phone");
                                        icon=jsonObject.getString("icon");
                                    }catch(Exception e){
                                        Log.e("log_tag", "the Error parsing data " + e.toString());
                                    }
                                    if (result == 1) {
                                        String iconString=icon;
                                        String iconPath = getApplicationContext().getExternalFilesDir("") + "/" + phone + "/userIcon";
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
                                        Log.i("log_tag", "登陆成功！");
                                        returnMainActivity(name);
                                        setUserInfo(name,phone);
                                        Looper.prepare();
                                        Toast.makeText(LoginActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                    } else if (result == -1) {
                                        Log.e("getUser", "不存在该用户！");
                                        Looper.prepare();
                                        Toast.makeText(LoginActivity.this, "不存在该用户！", Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                    }
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }).start();
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        // 获取验证码成功，true为智能验证，false为普通下发短信
                        Toast.makeText(LoginActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                    } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                        // 返回支持发送验证码的国家列表
                    }
                } else {
                    // 如果操作失败
                    if (flag) {
                        Toast.makeText(LoginActivity.this, "验证码获取失败，请重新获取", Toast.LENGTH_SHORT).show();
                        code_sms.requestFocus();
                    } else {
                        ((Throwable) data).printStackTrace();
                        Toast.makeText(LoginActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
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

        code_send_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(code_et.getText())) {
                    if (Utils.isMobile(code_et.getText().toString())) {
                        phoneNumber = code_et.getText().toString();
                        SMSSDK.getVerificationCode("86", phoneNumber); // 发送验证码给号码的 phoneNumber 的手机
                        code_sms.requestFocus();
                        timer.start();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "请输入完整的电话号码", Toast.LENGTH_SHORT).show();
                        code_et.requestFocus();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "请输入电话号码", Toast.LENGTH_SHORT).show();
                    code_et.requestFocus();
                }
            }
        });

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
                            String icon="";
                            try{
                                result = jsonObject.getInt("status");
                                name = jsonObject.getString("name");
                                phone=jsonObject.getString("phone");
                                icon=jsonObject.getString("icon");
                            }catch(Exception e){
                                Log.e("log_tag", "the Error parsing data " + e.toString());
                            }
                            if (result == 1) {
                                String iconString=icon;
                                String iconPath = getApplicationContext().getExternalFilesDir("") + "/" + phone + "/userIcon";
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
                                Log.i("pwd_login", "登陆成功！");
                                returnMainActivity(name);
                                setUserInfo(name,phone);
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this, "登陆成功！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (result == -2) {
                                Log.e("register", "密码错误！");
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this, "密码错误！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (result == -1) {
                                Log.e("register", "不存在该用户！");
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

        code_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        beforeCodeLogin();
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
        code_register_login.setOnClickListener(new View.OnClickListener() {
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
        String params = "uid=" + user_id + '&' + "pwd=" + input_pwd;
        InputStream is= Utils.connect(urlstr,params);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
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

    private void beforeCodeLogin(){
        user_phone = code_et.getText().toString();
        if (user_phone == null || user_phone.length() <= 0) {
            Looper.prepare();
            Toast.makeText(LoginActivity.this, "请输入手机号！", Toast.LENGTH_LONG).show();
            Looper.loop();
            return;
        }
        if (!TextUtils.isEmpty(code_sms.getText())) {
            if (code_sms.getText().length() == 4) {
                verificationCode = code_sms.getText().toString();
                SMSSDK.submitVerificationCode("86", phoneNumber, verificationCode);
                flag = false;
            } else {
                Looper.prepare();
                Toast.makeText(this, "请输入完整的验证码", Toast.LENGTH_SHORT).show();
                Looper.loop();
                code_sms.requestFocus();
            }
            Looper.prepare();
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            Looper.loop();
            code_sms.requestFocus();
            //return jsonObject;
        }
    }

    private JSONObject codeLogin() throws IOException {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("status",0);
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }

        String urlstr = "http://58.87.100.195/CodeForum/codeLogin.php";
        String params = "phone=" + user_phone;
        InputStream is= Utils.connect(urlstr,params);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
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

    class GoodsAdapter extends PagerAdapter {
        Context context;
        ArrayList<View> ivGoodsList;
        public GoodsAdapter(Context context,ArrayList<View>ivGoodsList){
            this.context=context;
            this.ivGoodsList=ivGoodsList;
        }

        @Override
        public int getCount() {
            return ivGoodsList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            View view=ivGoodsList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view=(ImageView)object;
            container.removeView(view);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();  // 注销回调接口
    }
}
