package com.example.codeforum.ui.personal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.codeforum.LoginActivity;
import com.example.codeforum.MainActivity;
import com.example.codeforum.R;

public class PersonalFragment extends Fragment {
    private PersonalViewModel personalViewModel;
    private TextView person_name;
    private ConstraintLayout login_register;
    private Button return_login;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        personalViewModel =
                ViewModelProviders.of(this).get(PersonalViewModel.class);
        View root = inflater.inflate(R.layout.fragment_personal, container, false);
        final TextView textView = root.findViewById(R.id.text_personal);
        personalViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        person_name = root.findViewById(R.id.person_name);

        //跳转到登录界面进行登录或注册
        login_register = (ConstraintLayout) root.findViewById(R.id.login_register);
        login_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), LoginActivity.class);
                startActivity(intent);
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
            }
        });

        getBundle();
        getUser();
        return root;
    }
    private void getBundle(){
        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString("name");
            if (name != null) {
                person_name.setText(name);
            } else {
                person_name.setText("登录/注册");
            }
        }
    }
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
}