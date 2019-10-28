package com.example.codeforum;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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
            public boolean onQueryTextChange(String newText) {
                //mAdapter.getFilter().filter(newText);
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

    public JSONObject search(String user_name) throws IOException {
        JSONObject returnResult = new JSONObject();
        try {
            returnResult.put("status", 0);
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
        String urlstr = "http://58.87.100.195/CodeForum/newFriend.php";
        //建立网络连接
        URL url = new URL(urlstr);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        //往网页写入POST数据，和网页POST方法类似，参数间用‘&’连接
        String params = "name=" + user_name;
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
            returnResult = new JSONObject(result);
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
        return returnResult;
    }

    private void addFriend(String phone) throws IOException {
        String user_phone = "";
        SharedPreferences user = getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
            if (!user_phone.equals("默认值")) {
                String urlstr = "http://58.87.100.195/CodeForum/addFriend.php";
                //建立网络连接
                URL url = new URL(urlstr);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                //往网页写入POST数据，和网页POST方法类似，参数间用‘&’连接
                String params = "phone=" + phone + "&" + "name=" + user_phone;

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
                Log.e(result,result);
            }
        }
    }
}
