package com.example.codeforum.ui.blog;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.codeforum.R;
import com.example.codeforum.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NewBlogActivity extends Activity {
    private ImageView new_blog_back;
    private EditText new_blog_title;
    private EditText new_blog_url;
    private Spinner new_blog_classification;
    private EditText new_blog_content;
    private Button new_blog;
    private String title_info;
    private String url_info;
    private String classification_info;
    private String content_info;
    private String user_phone;
    private SharedPreferences user;
    private List<String> list;
    private ArrayAdapter<String> adapter;
    private static String classificationJson = "{'classifications':['Java','Python','C语言','php','JS','Web','Android','iOS','Linux']}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_new_blog);
        new_blog_title=(EditText)findViewById(R.id.new_blog_title);
        new_blog_url=(EditText)findViewById(R.id.new_blog_url);
        new_blog_content=(EditText)findViewById(R.id.new_blog_content);
        new_blog=(Button)findViewById(R.id.new_blog);
        new_blog_classification=(Spinner)findViewById(R.id.new_blog_classification);
        new_blog_back = (ImageView) findViewById(R.id.new_blog_back);
        new_blog_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });


        list = new ArrayList();
        try
        {
            JSONObject jo1 = new JSONObject(classificationJson);
            JSONArray ja1 = jo1.getJSONArray("classifications");
            for(int i = 0; i < ja1.length(); i++)
            {
                String cityName = ja1.getString(i);
                list.add(cityName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter=new ArrayAdapter<String>(this,R.layout.my_spinner,list);
        adapter.setDropDownViewResource(R.layout.my_spinner);
        new_blog_classification.setAdapter(adapter);
        new_blog_classification.setOnItemSelectedListener(new spinnerSelectedListenner());

        user = getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
        }

        new_blog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user!=null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int result=newBlog();
                                if(result==1){
                                    Uri data=null;
                                    Intent resultIntent = new Intent(null,data);
                                    setResult(RESULT_OK,resultIntent);
                                    finish();
                                    Looper.prepare();
                                    Toast.makeText(NewBlogActivity.this,"新建博客成功！",Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                }else{
                                    Looper.prepare();
                                    Toast.makeText(NewBlogActivity.this,"新建博客失败！",Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }else{
                    Log.e("new_blog","还没有登录！");
                    Looper.prepare();
                    Toast.makeText(NewBlogActivity.this,"您还没有登录，请先登录后在进行此操作！",Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }

    private int newBlog()throws IOException{
        title_info=new_blog_title.getText().toString();
        url_info=new_blog_url.getText().toString();
        content_info=new_blog_content.getText().toString();
        int returnResult=0;
        if(!user_phone.equals("默认值")) {
            String urlstr = "http://58.87.100.195/CodeForum/newBlog.php";
            String params = "phone=" + user_phone + '&' + "title=" + title_info + '&' + "url=" + url_info + '&' + "classification=" + classification_info+ '&' + "content=" + content_info;
            InputStream is = Utils.connect(urlstr, params);
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
        }
        return returnResult;
    }

    private class spinnerSelectedListenner implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            // TODO Auto-generated method stub
            classification_info=parent.getItemAtPosition(position).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub

        }
    }
}
