package com.example.codeforum.ui.personal;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.codeforum.ui.blog.BlogActivity;
import com.example.codeforum.ui.blog.SortedBlogActivity;
import com.example.codeforum.ui.login.LoginActivity;
import com.example.codeforum.MainActivity;
import com.example.codeforum.service.MyWebSocketClientService;
import com.example.codeforum.R;
import com.example.codeforum.ui.setInfo.SetInfoActivity;
import com.example.codeforum.ui.userIcon.UserIconActivity;
import com.example.codeforum.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cn.smssdk.SMSSDK;

public class PersonalFragment extends Fragment {
    private PersonalViewModel personalViewModel;
    private TextView person_name;
    private ImageView person_icon;
    private ConstraintLayout login_register;
    private ConstraintLayout my_data;
    private ConstraintLayout my_blog;
    private ConstraintLayout return_login;
    private SharedPreferences user;
    private String user_name;
    private String user_phone;
    private Bitmap photo;
    private ImageHandler imgHandler = new ImageHandler();
    private MyWebSocketClientService.MyWebSocketClientBinder myBinder;
    private ProgressBar progressBar;
    private static final int loginActivity = 1;
    private static final int userIconActivity = 5;
    private static final int setInfoActivity = 6;
    private static final int sortedBlogActivity = 9;
    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                getFriendIcon(user_phone);
                Message msg = imgHandler.obtainMessage();
                msg.what = 2;
                imgHandler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    //获取登录界面返回结果
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case (loginActivity): {
                if (resultCode == Activity.RESULT_OK) {
                    my_blog.setVisibility(View.VISIBLE);
                    my_data.setVisibility(View.VISIBLE);
                    return_login.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    getUser();
                    if (user != null) {
                        if (!user_name.equals("默认值")) {
                            thread.start();
                        }
                    }
                    ((MainActivity) getActivity()).mainBindService();
                } else {
                    my_blog.setVisibility(View.GONE);
                    my_data.setVisibility(View.GONE);
                    return_login.setVisibility(View.GONE);
                    person_name.setText("登录/注册");
                    person_icon.setImageResource(R.drawable.ic_default_personal_icon_256dp);
                    login_register.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setLoginRegister();
                        }
                    });
                }
                break;
            }
            case (userIconActivity): {
                final String iconPath = getContext().getExternalFilesDir("") + "/" + user_phone + "/userIcon";
                File icon = new File(new File(iconPath), "image.jpg");
                if (icon.exists()) {
                    Bitmap photo = BitmapFactory.decodeFile(iconPath + "/image.jpg");
                    if (photo != null) {
                        photo = Utils.toRoundBitmap(photo);
                        person_icon.setImageBitmap(photo);
                    } else {
                        person_icon.setImageResource(R.drawable.ic_default_personal_icon_256dp);
                    }
                }
                break;
            }
            case (setInfoActivity): {
                user = getActivity().getSharedPreferences("user", 0);
                if (user != null) {
                    user_phone = user.getString("user_phone", "默认值");
                    user_name = user.getString("user_id", "默认值");
                    if (!user_name.equals("默认值")) {
                        person_name.setText(user_name);
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    public void onDestroy() {
        super.onDestroy();
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
        progressBar = (ProgressBar) root.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        person_name = (TextView) root.findViewById(R.id.person_name);
        person_icon = (ImageView) root.findViewById(R.id.person_icon);
        login_register = (ConstraintLayout) root.findViewById(R.id.login_register);

        //退出登录
        my_data = root.findViewById(R.id.my_data);
        my_blog = (ConstraintLayout) root.findViewById(R.id.my_blog);
        return_login = (ConstraintLayout) root.findViewById(R.id.return_login);
        my_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), SetInfoActivity.class);
                startActivityForResult(intent, setInfoActivity);
            }
        });
        getUser();
        my_blog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), SortedBlogActivity.class);
                intent.putExtra("title_name", "我的博客");
                startActivityForResult(intent, sortedBlogActivity);
            }
        });
        return_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user = getActivity().getSharedPreferences("user", 0);
                user.edit().clear().commit();
                getUser();
                if (((MainActivity) getActivity()).getBinder() != null) {
                    ((MainActivity) getActivity()).mainUnbindService();
                    ((MainActivity) getActivity()).setBinder();
                }
            }
        });
        return root;
    }

    //获取当前用户
    private void getUser() {
        user = getActivity().getSharedPreferences("user", 0);
        if (user != null) {
            user_phone = user.getString("user_phone", "默认值");
            user_name = user.getString("user_id", "默认值");
            if (!user_name.equals("默认值")) {
                my_blog.setVisibility(View.VISIBLE);
                my_data.setVisibility(View.VISIBLE);
                return_login.setVisibility(View.VISIBLE);
                person_name.setText(user_name);
                login_register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toUserIcon();
                    }
                });
                String iconPath = getContext().getExternalFilesDir("") + "/" + user_phone + "/userIcon";
                File icon_file = new File(new File(iconPath), "image.jpg");
                if (icon_file.exists()) {
                    Bitmap local_photo = BitmapFactory.decodeFile(iconPath + "/image.jpg");
                    if (local_photo != null) {
                        local_photo = Utils.toRoundBitmap(local_photo);
                        person_icon.setImageBitmap(local_photo);
                    } else {
                        person_icon.setImageResource(R.drawable.ic_default_personal_icon_256dp);
                    }
                }
            } else {
                my_blog.setVisibility(View.GONE);
                my_data.setVisibility(View.GONE);
                return_login.setVisibility(View.GONE);
                person_name.setText("登录/注册");
                person_icon.setImageResource(R.drawable.ic_default_personal_icon_256dp);
                login_register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setLoginRegister();
                    }
                });
            }
        } else {
            my_blog.setVisibility(View.GONE);
            my_data.setVisibility(View.GONE);
            return_login.setVisibility(View.GONE);
            person_name.setText("登录/注册");
            person_icon.setImageResource(R.drawable.ic_default_personal_icon_256dp);
            login_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setLoginRegister();
                }
            });
        }
    }

    private void toUserIcon() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), UserIconActivity.class);
        startActivityForResult(intent, userIconActivity);
    }

    private void setLoginRegister() {
        if (((MainActivity) getActivity()).getBinder() != null) {
            ((MainActivity) getActivity()).mainUnbindService();
            ((MainActivity) getActivity()).setBinder();
        }
        Intent intent = new Intent();
        intent.setClass(getActivity(), LoginActivity.class);
        startActivityForResult(intent, loginActivity);
    }

/*  //单独获取用户头像
    private Bitmap getFile(String user_phone) throws IOException {
        if (!user_phone.equals("默认值")) {
            String urlstr = "http://58.87.100.195/CodeForum/getFile.php";
            String params = "name=" + user_phone;
            InputStream is = Utils.connect(urlstr, params);
            photo = BitmapFactory.decodeStream(is);
        }
        return photo;
    }*/

    private class ImageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    photo = (Bitmap) msg.obj;
                    if (photo != null) {
                        photo = Utils.toRoundBitmap(photo);
                        person_icon.setImageBitmap(photo);
                    } else {
                        person_icon.setImageResource(R.drawable.ic_default_personal_icon_256dp);
                    }
                    break;
                case 1:
                    photo = ((BitmapDrawable) person_icon.getDrawable()).getBitmap();
                    Utils.savePhoto(photo, msg.getData().getString("iconPath"), "image");
                    person_icon.setImageBitmap(photo);
                    break;
                case 2:
                    progressBar.setVisibility(View.GONE);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    break;
                default:
                    break;
            }
        }
    }

    private void getFriendIcon(String user_phone) throws IOException {
        String urlstr = "http://58.87.100.195/CodeForum/getFriendIcon.php";
        String params = "phone=" + user_phone;
        InputStream is = Utils.connect(urlstr, params);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line = "";
        StringBuilder sb = new StringBuilder();
        while (null != (line = bufferedReader.readLine())) {
            sb.append(line);
        }
        String result = sb.toString();
        try {
            JSONArray jsonArray = new JSONArray(result);
            String nameLine = "";
            String phoneLine = "";
            String iconLine = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                nameLine = jsonObject.getString("name");
                phoneLine = jsonObject.getString("phone");
                if (!phoneLine.equals("null")) {
                    iconLine = jsonObject.getString("icon");
                    Bitmap bitmap = Utils.stringtoBitmap(iconLine);
                    final String iconPath = getContext().getExternalFilesDir("") + "/" + user_phone + "/friend/" + phoneLine + "/userIcon";
                    Utils.savePhoto(bitmap, iconPath, "image");
                }
            }
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
    }
}