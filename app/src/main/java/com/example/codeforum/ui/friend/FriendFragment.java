package com.example.codeforum.ui.friend;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.codeforum.CommunicationActivity;
import com.example.codeforum.FindFriendActivity;
import com.example.codeforum.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FriendFragment extends Fragment {

    private FriendViewModel friendViewModel;
    private ListView my_friend;
    private FloatingActionButton find_friend;
    private String user_phone="";
    private ArrayAdapter mAdapter;
    private static Handler handler;
    private static final int findFriendActivity=2;
    @Override
    //获取查找新的好友页面的返回结果
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case(findFriendActivity):{
                if(resultCode== Activity.RESULT_OK){
                    SharedPreferences user = getActivity().getSharedPreferences("user", 0);
                    if(user!=null){
                        user_phone = user.getString("user_phone", "默认值");
                        if(!user_phone.equals("默认值")){
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        getFriend(user_phone);
                                    }catch(IOException e){
                                        Log.e("log_tag", "the Error parsing data " + e.toString());
                                    }
                                }
                            }).start();
                        }
                    }
                }
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        friendViewModel =
                ViewModelProviders.of(this).get(FriendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_friend, container, false);
        /*final TextView textView = root.findViewById(R.id.text_friend);
        friendViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        //获取好友列表后进行视图数据更新
        my_friend = (ListView) root.findViewById(R.id.my_friend);
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                String[] friend=msg.getData().getString("friend").split(",");
                mAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, friend);
                my_friend.setAdapter(mAdapter);
                final String[] data=friend;
                my_friend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.e("friend",data[position]);
                        Intent intent=new Intent();
                        intent.setClass(getActivity(), CommunicationActivity.class);
                        intent.putExtra("name",data[position]);
                        intent.putExtra("phone",data[position]);
                        startActivity(intent);
                    }
                });
            }
        };

        //获取当前用户电话
        SharedPreferences user = getActivity().getSharedPreferences("user", 0);
        if(user!=null){
            user_phone = user.getString("user_phone", "默认值");
            if(!user_phone.equals("默认值")){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            getFriend(user_phone);
                        }catch(IOException e){
                            Log.e("log_tag", "the Error parsing data " + e.toString());
                        }
                    }
                }).start();
            }
        }

        //查找并添加新的好友
        find_friend = (FloatingActionButton) root.findViewById(R.id.find_friend);
        find_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), FindFriendActivity.class);
                startActivityForResult(intent,findFriendActivity);
            }
        });

        return root;
    }

    //获取好友列表
    private void getFriend(String user_phone) throws IOException {
        Bundle bundle=new Bundle();
        if (!user_phone.equals("默认值")) {
            String urlstr = "http://58.87.100.195/CodeForum/getFriend.php";
            URL url = new URL(urlstr);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            String params = "phone=" + user_phone;
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
            String result = sb.toString();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String friendline = jsonObject.getString("friend");
                Log.e("e: ",friendline);
                if(friendline.equals("null")||friendline.length()==0){
                    bundle.putString("friend","你暂无好友，可以点击页面底部的添加按钮查找好友并添加！");
                }else{
                    bundle.putString("friend",friendline);
                }
            } catch (Exception e) {
                Log.e("log_tag", "the Error parsing data " + e.toString());
            }
        }
        Message msg=new Message();
        msg.setData(bundle);
        Log.e("friend:",msg.getData().toString());
        handler.sendMessage(msg);
    }
}