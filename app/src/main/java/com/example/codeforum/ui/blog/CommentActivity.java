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
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.codeforum.R;
import com.example.codeforum.Utils;
import com.example.codeforum.component.discoverView.DiscoverView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CommentActivity extends Activity {
    private ImageView comment_back;
    private LinearLayout comment_container;
    private EditText new_comment_content;
    private Button new_comment;
    private String comment_name;
    private String user_phone;
    private SharedPreferences user;
    private String new_comment_info;
    private CommentHandler handler = new CommentHandler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_comment);

        comment_back = (ImageView) findViewById(R.id.comment_back);
        comment_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        comment_container=(LinearLayout)findViewById(R.id.comment);
        new_comment_content=(EditText)findViewById(R.id.new_comment_content);
        new_comment=(Button)findViewById(R.id.new_comment);

        new_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            newComment();
                        } catch (IOException e) {
                            Log.e("log_tag", "the Error parsing data " + e.toString());
                        }
                    }
                }).start();
            }
        });

        new_comment_content.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(KeyEvent.KEYCODE_ENTER == keyCode && event.getAction() == KeyEvent.ACTION_DOWN){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                newComment();
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    return true;
                }
                return false;
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            comment_name = intent.getStringExtra("comment_name");
        }

        user = getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getComment();
                } catch (IOException e) {
                    Log.e("log_tag", "the Error parsing data " + e.toString());
                }
            }
        }).start();
    }

    private void getComment()throws IOException{
        Bundle bundle = new Bundle();
        String urlstr = "http://58.87.100.195/CodeForum/getComment.php";
        String params;
        params = "comment_name=" + comment_name;
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
            String[] commentArray = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                nameArray[i] = jsonObject.getString("name");
                phoneArray[i] = jsonObject.getString("phone");
                dateArray[i] = jsonObject.getString("date");
                iconArray[i] = jsonObject.getString("icon");
                commentArray[i] = jsonObject.getString("comment");
            }
            bundle.putStringArray("name", nameArray);
            bundle.putStringArray("phone", phoneArray);
            bundle.putStringArray("date", dateArray);
            bundle.putStringArray("icon", iconArray);
            bundle.putStringArray("comment", commentArray);
            Message msg = new Message();
            msg.setData(bundle);
            handler.sendMessage(msg);
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
    }

    private void newComment()throws IOException{
        new_comment_info=new_comment_content.getText().toString();
        if(!user_phone.equals("默认值")) {
            String urlstr = "http://58.87.100.195/CodeForum/newComment.php";
            String params = "phone=" + user_phone + '&' + "comment=" + new_comment_info + '&' + "comment_name=" + comment_name;
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
            } catch (Exception e) {
                Log.e("log_tag", "the Error parsing data " + e.toString());
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getComment();
                    } catch (IOException e) {
                        Log.e("log_tag", "the Error parsing data " + e.toString());
                    }
                }
            }).start();
        }
    }

    private class CommentHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0: {
                    String[] name = msg.getData().getStringArray("name");
                    String[] phone = msg.getData().getStringArray("phone");
                    String[] date = msg.getData().getStringArray("date");
                    String[] icon = msg.getData().getStringArray("icon");
                    String[] comment = msg.getData().getStringArray("comment");
                    comment_container.removeAllViewsInLayout();
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
                            discoverView[i].setContentText(comment[i]);
                            discoverView[i].hideClassification();
                            discoverView[i].hideTitle();
                            final String _name = name[i];
                            final String _phone = phone[i];
                            final String _icon = icon[i];
                            if (!phone[i].equals("")) {
                                discoverView[i].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                });
                            }
                            comment_container.addView(discoverView[i]);
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
