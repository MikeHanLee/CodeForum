package com.example.codeforum.ui.personal;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.codeforum.LoginActivity;
import com.example.codeforum.MainActivity;
import com.example.codeforum.MyWebSocketClientService;
import com.example.codeforum.R;

import de.tavendo.autobahn.WebSocketConnection;

public class PersonalFragment extends Fragment {
    private PersonalViewModel personalViewModel;
    private TextView person_name;
    private ConstraintLayout login_register;
    private Button return_login;
    private String user_phone="";
    private MyWebSocketClientService.MyWebSocketClientBinder myBinder;
    private static final int loginActivity=1;

    @Override
    //获取登录界面返回结果
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case(loginActivity):{
                if(resultCode== Activity.RESULT_OK){
                    String name = data.getStringExtra("name");
                    if (name != null) {
                        person_name.setText(name);
                    }
                    ((MainActivity)getActivity()).mainBindService();
                }else{
                    person_name.setText("登录/注册");
                }
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        personalViewModel =
                ViewModelProviders.of(this).get(PersonalViewModel.class);
        View root = inflater.inflate(R.layout.fragment_personal, container, false);
        /*final TextView textView = root.findViewById(R.id.text_personal);
        personalViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        person_name = root.findViewById(R.id.person_name);

        //跳转到登录界面进行登录或注册
        login_register = (ConstraintLayout) root.findViewById(R.id.login_register);
        login_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((MainActivity)getActivity()).getBinder()!=null){
                    ((MainActivity)getActivity()).mainUnbindService();
                    ((MainActivity)getActivity()).setBinder();
                }
                SharedPreferences user = getActivity().getSharedPreferences("user", 0);
                if(user!=null){
                    user.edit().clear().apply();
                }
                Intent intent = new Intent();
                intent.setClass(getActivity(), LoginActivity.class);
                startActivityForResult(intent,loginActivity);
            }
        });


        //退出登录
        return_login = (Button)root.findViewById(R.id.return_login);
        return_login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                SharedPreferences user = getActivity().getSharedPreferences("user", 0);
                user.edit().clear().commit();
                getUser();
                if(((MainActivity)getActivity()).getBinder()!=null){
                    ((MainActivity)getActivity()).mainUnbindService();
                    ((MainActivity)getActivity()).setBinder();
                }
            }
        });

        getUser();
        return root;
    }

    //获取当前用户
    private void getUser(){
        SharedPreferences user = getActivity().getSharedPreferences("user", 0);
        if (user != null) {
            String name = user.getString("user_id", "默认值");
            if (!name.equals("默认值")) {
                person_name.setText(name);
            } else {
                person_name.setText("登录/注册");
            }
        }
    }

    /*ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyWebSocketClientService myService = ((MyWebSocketClientService.MyWebSocketClientBinder) service).getService();
            myBinder=(MyWebSocketClientService.MyWebSocketClientBinder) service;
            String authorName = myService.getAuthorName();
            Toast.makeText(getActivity(), "author name is: " + authorName, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };*/
}