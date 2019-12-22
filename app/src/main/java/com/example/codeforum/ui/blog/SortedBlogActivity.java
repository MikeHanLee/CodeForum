package com.example.codeforum.ui.blog;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.codeforum.R;
import com.example.codeforum.Utils;
import com.example.codeforum.component.discoverView.DiscoverView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SortedBlogActivity extends Activity {
    private TextView sorted_blog_name;
    private ImageView sorted_blog_back;
    private LinearLayout sorted_blog;
    private String title_name;
    private String user_phone;
    private SharedPreferences user;
    private SortedBlogHandler handler = new SortedBlogHandler();
    private final static int blogActivity=7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sorted_blog);

        sorted_blog_name=(TextView)findViewById(R.id.sorted_blog_name);
        sorted_blog_back=(ImageView)findViewById(R.id.sorted_blog_back);
        sorted_blog=(LinearLayout)findViewById(R.id.sorted_blog);

        sorted_blog_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            title_name = intent.getStringExtra("title_name");
            sorted_blog_name.setText(title_name);
        }

        user = getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
        }

        if(title_name.equals("我的博客")){
            if(user!=null&&!user_phone.equals("默认值"))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        getMyBlog();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        getClassBlog();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void getMyBlog()throws IOException{
        Bundle bundle = new Bundle();
        String urlstr = "http://58.87.100.195/CodeForum/getMyBlog.php";
        String params;
        params = "phone=" + user_phone;
        InputStream is = Utils.connect(urlstr, params);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line = "";
        StringBuilder sb = new StringBuilder();
        while (null != (line = bufferedReader.readLine())) {
            sb.append(line);
        }
        String result = sb.toString();
        Log.d("result",result);
        try {
            JSONArray jsonArray = new JSONArray(result);
            String[] nameArray = new String[jsonArray.length()];
            String[] phoneArray = new String[jsonArray.length()];
            String[] dateArray = new String[jsonArray.length()];
            String[] iconArray = new String[jsonArray.length()];
            String[] titleArray = new String[jsonArray.length()];
            String[] urlArray = new String[jsonArray.length()];
            String[] contentArray = new String[jsonArray.length()];
            String[] classificationArray = new String[jsonArray.length()];
            String[] commentNameArray = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                nameArray[i] = jsonObject.getString("name");
                phoneArray[i] = jsonObject.getString("phone");
                dateArray[i] = jsonObject.getString("date");
                iconArray[i] = jsonObject.getString("icon");
                titleArray[i] = jsonObject.getString("title");
                urlArray[i] = jsonObject.getString("url");
                contentArray[i] = jsonObject.getString("content");
                classificationArray[i] = jsonObject.getString("classification");
                commentNameArray[i] = jsonObject.getString("comment_name");
            }
            bundle.putStringArray("name", nameArray);
            bundle.putStringArray("phone", phoneArray);
            bundle.putStringArray("date", dateArray);
            bundle.putStringArray("icon", iconArray);
            bundle.putStringArray("title", titleArray);
            bundle.putStringArray("url", urlArray);
            bundle.putStringArray("content", contentArray);
            bundle.putStringArray("classification", classificationArray);
            bundle.putStringArray("comment_name", commentNameArray);
            Message msg = new Message();
            msg.setData(bundle);
            handler.sendMessage(msg);
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
    }

    private void getClassBlog()throws IOException{
        Bundle bundle = new Bundle();
        String urlstr = "http://58.87.100.195/CodeForum/getClassBlog.php";
        String params;
        params = "classification=" + title_name;
        InputStream is = Utils.connect(urlstr, params);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line = "";
        StringBuilder sb = new StringBuilder();
        while (null != (line = bufferedReader.readLine())) {
            sb.append(line);
        }
        String result = sb.toString();
        Log.d("result",result);
        try {
            JSONArray jsonArray = new JSONArray(result);
            String[] nameArray = new String[jsonArray.length()];
            String[] phoneArray = new String[jsonArray.length()];
            String[] dateArray = new String[jsonArray.length()];
            String[] iconArray = new String[jsonArray.length()];
            String[] titleArray = new String[jsonArray.length()];
            String[] urlArray = new String[jsonArray.length()];
            String[] contentArray = new String[jsonArray.length()];
            String[] classificationArray = new String[jsonArray.length()];
            String[] commentNameArray = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                nameArray[i] = jsonObject.getString("name");
                phoneArray[i] = jsonObject.getString("phone");
                dateArray[i] = jsonObject.getString("date");
                iconArray[i] = jsonObject.getString("icon");
                titleArray[i] = jsonObject.getString("title");
                urlArray[i] = jsonObject.getString("url");
                contentArray[i] = jsonObject.getString("content");
                classificationArray[i] = jsonObject.getString("classification");
                commentNameArray[i] = jsonObject.getString("comment_name");
            }
            bundle.putStringArray("name", nameArray);
            bundle.putStringArray("phone", phoneArray);
            bundle.putStringArray("date", dateArray);
            bundle.putStringArray("icon", iconArray);
            bundle.putStringArray("title", titleArray);
            bundle.putStringArray("url", urlArray);
            bundle.putStringArray("content", contentArray);
            bundle.putStringArray("classification", classificationArray);
            bundle.putStringArray("comment_name", commentNameArray);
            Message msg = new Message();
            msg.setData(bundle);
            handler.sendMessage(msg);
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
    }

    private class SortedBlogHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0: {
                    String[] name = msg.getData().getStringArray("name");
                    String[] phone = msg.getData().getStringArray("phone");
                    String[] date = msg.getData().getStringArray("date");
                    String[] icon = msg.getData().getStringArray("icon");
                    String[] title = msg.getData().getStringArray("title");
                    String[] url = msg.getData().getStringArray("url");
                    String[] content = msg.getData().getStringArray("content");
                    String[] classification = msg.getData().getStringArray("classification");
                    String[] comment_name = msg.getData().getStringArray("comment_name");
                    sorted_blog.removeAllViewsInLayout();
                    Bitmap bitmap;
                    DiscoverView[] discoverView = new DiscoverView[name.length];
                    for (int i = 0; i < name.length; i++) {
                        if (!name[i].equals("null")) {
                            discoverView[i] = new DiscoverView(getApplicationContext());
                            discoverView[i].setNameText(name[i]);
                            discoverView[i].setPhoneText(phone[i]);
                            discoverView[i].setPhoneText(date[i]);
                            bitmap=Utils.stringtoBitmap(icon[i]);
                            bitmap = Utils.toRoundBitmap(bitmap);
                            discoverView[i].setImageResource(bitmap);
                            discoverView[i].setTitleText(title[i]);
                            discoverView[i].setContentText(content[i]);
                            discoverView[i].setClassificationText("分类："+classification[i]);
                            final String _name = name[i];
                            final String _phone = phone[i];
                            final String _icon = icon[i];
                            final String _title = title[i];
                            final String _url = url[i];
                            final String _date = date[i];
                            final String _content = content[i];
                            final String _classification = classification[i];
                            final String _comment_name = comment_name[i];
                            if (!phone[i].equals("")) {
                                discoverView[i].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent();
                                        intent.setClass(SortedBlogActivity.this, BlogActivity.class);
                                        intent.putExtra("name", _name);
                                        intent.putExtra("phone", _phone);
                                        intent.putExtra("icon", _icon);
                                        intent.putExtra("title", _title);
                                        intent.putExtra("url", _url);
                                        intent.putExtra("date", _date);
                                        intent.putExtra("content", _content);
                                        intent.putExtra("classification", _classification);
                                        intent.putExtra("comment_name", _comment_name);
                                        startActivityForResult(intent, blogActivity);
                                    }
                                });
                            }
                            sorted_blog.addView(discoverView[i]);
                        }
                    }
                    break;
                }
                case 1:{
                    break;
                }
                default:
                    break;
            }
        }
    }
}
