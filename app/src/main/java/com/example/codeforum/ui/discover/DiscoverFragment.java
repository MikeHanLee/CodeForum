package com.example.codeforum.ui.discover;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.codeforum.MainActivity;
import com.example.codeforum.R;
import com.example.codeforum.Utils;
import com.example.codeforum.component.discoverView.DiscoverView;
import com.example.codeforum.component.notificationView.NotificationView;
import com.example.codeforum.ui.blog.BlogActivity;
import com.example.codeforum.ui.blog.NewBlogActivity;
import com.example.codeforum.ui.communication.CommunicationActivity;
import com.example.codeforum.ui.findFriend.FindFriendActivity;
import com.example.codeforum.ui.login.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DiscoverFragment extends Fragment {

    private DiscoverViewModel discoverViewModel;
    private EditText discover_search_content;
    private ImageView discover_search;
    private FloatingActionButton new_blog;
    private LinearLayout discover_content;
    private SharedPreferences user;
    private String user_phone;
    private String search_content;
    private DiscoverHandler handler = new DiscoverHandler();
    private final static int blogActivity=7;
    private final static int newBlogActivity=8;
    private Thread pleaseLoginThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            Toast.makeText(getActivity(), "请先登录", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    });

    @Override
    //获取登录界面返回结果
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case (newBlogActivity): {
                if (resultCode == Activity.RESULT_OK) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getDiscover();
                            } catch (IOException e) {
                                Log.e("log_tag", "the Error parsing data " + e.toString());
                            }
                        }
                    }).start();
                }
            }
            default:
                break;
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        discoverViewModel =
                ViewModelProviders.of(this).get(DiscoverViewModel.class);
        View root = inflater.inflate(R.layout.fragment_discover, container, false);
        /*final TextView textView = root.findViewById(R.id.text_discover);
        discoverViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        discover_search_content=(EditText)root.findViewById(R.id.discover_search_cotent);
        discover_search=(ImageView)root.findViewById(R.id.discover_search);
        new_blog=(FloatingActionButton)root.findViewById(R.id.new_blog);
        discover_content=(LinearLayout)root.findViewById(R.id.discover);

        user = getActivity().getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "default");
        }

        discover_content.removeAllViewsInLayout();

        new_blog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("user_phone",user_phone);
                if(user!=null) {
                    if(user_phone.equals("default")){
                        if(pleaseLoginThread.getState().toString().equals("NEW")){
                            pleaseLoginThread.start();
                        }
                    }else {
                        toNewBlogActivity();
                    }
                }else{
                    if(pleaseLoginThread.getState().toString().equals("NEW")){
                        pleaseLoginThread.start();
                    }
                }
            }
        });

        discover_search_content.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(KeyEvent.KEYCODE_ENTER == keyCode && event.getAction() == KeyEvent.ACTION_DOWN){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                getDiscoverSearch();
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

        discover_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            getDiscoverSearch();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getDiscover();
                } catch (IOException e) {
                    Log.e("log_tag", "the Error parsing data " + e.toString());
                }
            }
        }).start();
        return root;
    }

    private void getDiscover() throws IOException{
        Bundle bundle = new Bundle();
        String urlstr = "http://58.87.100.195/CodeForum/getBlog.php";
        String params;
        if(user!=null){
            params = "phone=" + user_phone;
        }else{
            params = "phone=" + "default";
        }
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

    private void getDiscoverSearch() throws IOException{
        Bundle bundle = new Bundle();
        search_content=discover_search_content.getText().toString();
        String urlstr = "http://58.87.100.195/CodeForum/getSearchBlog.php";
        String params;
        params = "content=" + search_content;
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

    private void toNewBlogActivity(){
        Intent intent = new Intent();
        intent.setClass(getActivity(), NewBlogActivity.class);
        startActivityForResult(intent, newBlogActivity);
    }

    private class DiscoverHandler extends Handler {
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
                    discover_content.removeAllViewsInLayout();
                    Bitmap bitmap;
                    DiscoverView[] discoverView = new DiscoverView[name.length];
                    for (int i = 0; i < name.length; i++) {
                        if (!name[i].equals("null")) {
                            discoverView[i] = new DiscoverView(getContext());
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
                                        intent.setClass(getActivity(), BlogActivity.class);
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
                            discover_content.addView(discoverView[i]);
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