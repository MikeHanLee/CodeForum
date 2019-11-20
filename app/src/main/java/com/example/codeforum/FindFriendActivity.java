package com.example.codeforum;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FindFriendActivity extends AppCompatActivity {
    private ListView listView;
    private SearchView searchView;
    private ArrayAdapter mAdapter;
    private int status = 0;
    String[] data = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);
        searchView = (SearchView) findViewById(R.id.search_friend);
        listView = (ListView) findViewById(R.id.search_friend_result);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            //输入框提交结果时进行查询结果视图数据更新
            public boolean onQueryTextSubmit(String newText) {
                if (status == 1) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                if (data != null && data[0] != null) {
                                                    final String phone = data[0].split("phone: ")[1];
                                                    addFriend(phone);
                                                }
                                            } catch (Exception e) {
                                                Log.e("log_tag", "the Error parsing data " + e.toString());
                                            }
                                        }
                                    }).start();
                                }
                            });
                            Log.e("log_tag", "查找成功！");
                            Looper.prepare();
                            Toast.makeText(FindFriendActivity.this, "查找成功！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }).start();
                    listView.setAdapter(mAdapter);
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("log_tag", "没有匹配项！");
                            Looper.prepare();
                            Toast.makeText(FindFriendActivity.this, "没有匹配项！", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }).start();
                    data = new String[]{};
                    mAdapter = new ArrayAdapter(FindFriendActivity.this, android.R.layout.simple_list_item_1, data);
                    listView.setAdapter(mAdapter);
                }
                return false;
            }

            @Override
            //输入框内输入文本或者删除文本时执行向服务器查询数据
            public boolean onQueryTextChange(String newText) {
                final String user_name = newText;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject result = search(user_name);
                            String name = "";
                            String phone = "";
                            try {
                                status = result.getInt("status");
                                name = result.getString("name");
                                phone = result.getString("phone");
                                data = new String[]{"name: " + name + "\n" + "phone: " + phone};
                                mAdapter = new ArrayAdapter(FindFriendActivity.this, android.R.layout.simple_list_item_1, data);
                            } catch (Exception e) {
                                Log.e("log_tag", "the Error parsing data " + e.toString());
                            }
                        } catch (Exception e) {
                            Log.e("log_tag", "the Error parsing data " + e.toString());
                        }
                    }
                }).start();
                return false;
            }
        });
    }

    //查找新的好友
    public JSONObject search(String user_name) throws IOException {
        JSONObject returnResult = new JSONObject();
        try {
            returnResult.put("status", 0);
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
        String urlstr = "http://58.87.100.195/CodeForum/newFriend.php";
        URL url = new URL(urlstr);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        String params = "name=" + user_name;
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
            returnResult = new JSONObject(result);
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
        return returnResult;
    }

    //添加好友
    private void addFriend(String phone) throws IOException {
        String user_phone = "";
        SharedPreferences user = getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
            if (!user_phone.equals("默认值")) {
                String urlstr = "http://58.87.100.195/CodeForum/addFriend.php";
                URL url = new URL(urlstr);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                String params = "phone=" + phone + "&" + "name=" + user_phone;

                http.setDoOutput(true);
                http.setDoInput(true);
                http.setRequestMethod("POST");
                http.connect();
                OutputStream out = http.getOutputStream();
                out.write(params.getBytes());
                out.flush();
                out.close();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(http.getInputStream()));//获得输入流
                String line = "";
                StringBuilder sb = new StringBuilder();
                while (null != (line = bufferedReader.readLine())) {
                    sb.append(line);
                }
                final String result = sb.toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            JSONObject jsonObject=new JSONObject(result);
                            final int resultstatus=jsonObject.getInt("status");
                            if(resultstatus==1){
                                Log.e("log_tag", "添加好友成功！");
                                Uri data=null;
                                Intent result=new Intent(null,data);
                                setResult(RESULT_OK,result);
                                finish();
                                Looper.prepare();
                                Toast.makeText(FindFriendActivity.this, "添加好友成功！", Toast.LENGTH_SHORT).show();
                                Looper.loop();

                            }else if(resultstatus==-1){
                                Log.e("log_tag", "你已经添加过该好友！");
                                Looper.prepare();
                                Toast.makeText(FindFriendActivity.this, "你已经添加过该好友！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }else if(resultstatus==-2){
                                Log.e("log_tag", "你不能添加自己为好友！");
                                Looper.prepare();
                                Toast.makeText(FindFriendActivity.this, "你不能添加自己为好友！", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch(Exception e){
                            Log.e("log_tag", "the Error parsing data " + e.toString());
                        }
                    }
                }).start();
            }
        }
    }
}
