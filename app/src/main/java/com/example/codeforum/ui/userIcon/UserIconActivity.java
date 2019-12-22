package com.example.codeforum.ui.userIcon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.codeforum.BuildConfig;
import com.example.codeforum.FileUploader;
import com.example.codeforum.R;
import com.example.codeforum.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserIconActivity extends AppCompatActivity {
    private Button change_user_icon_button;
    private ImageView change_user_icon_icon;
    private ImageView change_user_icon_back;
    private SharedPreferences user;
    private String user_name;
    private String user_phone;
    private ActionBar actionBar;
    private static final int CHOOSE_PICTURE = 0;
    private static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    protected static Uri tokenImageUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // 如果返回码是可以用的
            switch (requestCode) {
                case TAKE_PICTURE:
                    startPhotoZoom(tokenImageUri); // 开始对图片进行裁剪处理
                    break;
                case CHOOSE_PICTURE:
                    startPhotoZoom(data.getData()); // 开始对图片进行裁剪处理
                    break;
                case CROP_SMALL_PICTURE:
                    if (data != null) {
                        setImageToView(); // 让刚才选择裁剪得到的图片显示在界面上
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_icon);
        actionBar=getSupportActionBar();
        actionBar.hide();
        change_user_icon_button = (Button) findViewById(R.id.change_user_icon_button);
        change_user_icon_icon = (ImageView) findViewById(R.id.change_user_icon_icon);
        change_user_icon_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(UserIconActivity.this).create();
                LayoutInflater inflater = LayoutInflater.from(UserIconActivity.this);
                dialog.setTitle(null);
                View view = inflater.inflate(R.layout.user_icon, null);
                ImageView img = (ImageView) view.findViewById(R.id.user_icon_image_iew);
                final String iconPath=getApplicationContext().getExternalFilesDir("")+"/"+user_phone+"/croppedImage";
                File icon = new File(new File(iconPath),"image.jpg");
                if(icon.exists()){
                    Bitmap photo = BitmapFactory.decodeFile(iconPath+"/image.jpg");
                    if(photo!=null){
                        img.setImageBitmap(photo);
                    }else{
                        img.setImageResource(R.drawable.ic_default_personal_icon_256dp);
                    }
                }
                dialog.setView(view);
                dialog.show();
            }
        });
        change_user_icon_back = (ImageView) findViewById(R.id.change_user_icon_back);
        change_user_icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        user = getApplicationContext().getSharedPreferences("user", 0);
        if(user!=null){
            user_phone = user.getString("user_phone", "默认值");
            user_name = user.getString("user_id", "默认值");
            if (!user_name.equals("默认值")) {
                final String iconPath=getApplicationContext().getExternalFilesDir("")+"/"+user_phone+"/userIcon";
                File icon = new File(new File(iconPath),"image.jpg");
                if(icon.exists()){
                    Bitmap photo = BitmapFactory.decodeFile(iconPath+"/image.jpg");
                    if(photo!=null){
                        photo = Utils.toRoundBitmap(photo);
                        change_user_icon_icon.setImageBitmap(photo);
                    }else{
                        change_user_icon_icon.setImageResource(R.drawable.ic_default_personal_icon_256dp);
                    }
                }
                change_user_icon_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showChoosePicDialog();
                    }
                });
            }
        }
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    int result=getUserIcon();
                    if(result==1){
                        Log.d("saveUser","下载成功");
                    }else{
                        Log.d("saveUser","下载失败");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

    private void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置头像");
        String[] items = {"选择本地照片", "拍照"};
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CHOOSE_PICTURE: // 选择本地照片
                        Intent openAlbumIntent = new Intent(
                                Intent.ACTION_GET_CONTENT);
                        openAlbumIntent.setType("image/*");
                        startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                        break;
                    case TAKE_PICTURE: // 拍照
                        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        openCameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        File imagePath = new File(getApplicationContext().getExternalFilesDir(""), "tokenImage");
                        File tokenImage = new File(imagePath,"image.jpg");
                        String tokenPath = tokenImage.getAbsolutePath();
                        if (!tokenImage.getParentFile().exists()) {
                            Boolean dirFlag=tokenImage.getParentFile().mkdir();
                            try {
                                Boolean flag = tokenImage.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (tokenImage.exists()) {
                                Boolean dirFlag = tokenImage.delete();
                            }
                            try {
                                Boolean flag = tokenImage.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        tokenImageUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID+".provider", tokenImage);
                        openCameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        openCameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tokenImageUri);
                        startActivityForResult(openCameraIntent, TAKE_PICTURE);
                        break;
                }
            }
        });
        builder.create().show();
    }

    protected void startPhotoZoom(Uri uri) {
        try {
            if (uri == null) {
                Log.e("tag", "The uri is not exist.");
            }
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri,"image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File imagePath = new File(getApplicationContext().getExternalFilesDir("")+"/"+user_phone, "croppedImage");
            File cropImage = new File(imagePath, "image.jpg");
            String path = cropImage.getAbsolutePath();
            if (!cropImage.getParentFile().exists()) {
                Boolean dirFlag=cropImage.getParentFile().mkdir();
                try {
                    Boolean flag = cropImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (cropImage.exists()) {
                    Boolean dirFlag = cropImage.delete();
                }
                try {
                    Boolean flag = cropImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Uri saveFileUri=Uri.fromFile(cropImage);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 320);
            intent.putExtra("outputY", 320);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, saveFileUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("return-data", false);
            startActivityForResult(intent, CROP_SMALL_PICTURE);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    protected void setImageToView(){
        String croppedPath=getApplicationContext().getExternalFilesDir("")+"/"+user_phone+"/croppedImage/image.jpg";
        Bitmap photo = BitmapFactory.decodeFile(croppedPath);
        final String saveIcon=Utils.bitmaptoStringTenPercent(photo);
        String userIconPath=getApplicationContext().getExternalFilesDir("")+"/"+user_phone+"/userIcon";
        Utils.savePhoto(Utils.stringtoBitmap(saveIcon),userIconPath,"image");
        photo = Utils.toRoundBitmap(photo);
        change_user_icon_icon.setImageBitmap(photo);
        String iconPath=getApplicationContext().getExternalFilesDir("")+"/"+user_phone+"/croppedImage";
        final File cropImage = new File(new File(iconPath),"image.jpg");
        final String phone=user_phone;
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Utils.uploadFile(cropImage,"http://58.87.100.195/CodeForum/uploadFile.php",phone);
                final HashMap<String,String> map = new HashMap<String,String>();
                map.put("phone",phone);
                FileUploader.upload("http://58.87.100.195/CodeForum/uploadFile.php", cropImage, map, new FileUploader.FileUploadListener() {
                    @Override
                    public void onProgress(long pro, double precent) {
                        Log.i("cky", precent+"");
                    }

                    @Override
                    public void onFinish(int code, String res, Map<String, List<String>> headers) {
                        Log.i("cky", res);
                    }
                });
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    int result=saveUserIcon(saveIcon);
                    if(result==1){
                        Log.d("saveUser","保存成功");
                    }else{
                        Log.d("saveUser","保存失败");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private int saveUserIcon(String userIcon)throws IOException{
        int returnResult=0;
        String urlstr = "http://58.87.100.195/CodeForum/saveUserIcon.php";
        String params = "phone=" + user_phone+ '&' + "icon=" +userIcon;
        InputStream is= Utils.connect(urlstr,params);
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
        return returnResult;
    }

    /*private  int getUserIcon()throws IOException{
        int returnResult=0;
        String urlstr = "http://58.87.100.195/CodeForum/getUserIcon.php";
        String params = "phone=" + user_phone;
        InputStream is= Utils.connect(urlstr,params);
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
            String iconLine = jsonObject.getString("info");
            Bitmap bitmap = Utils.stringtoBitmap(iconLine);
            final String iconPath = getApplicationContext().getExternalFilesDir("") + "/" + user_phone + "/userIcon";
            File icon = new File(new File(iconPath), "image.jpg");
            Utils.savePhoto(bitmap, iconPath, "image");
        } catch (Exception e) {
            Log.e("log_tag", "the Error parsing data " + e.toString());
        }
        return returnResult;
    }*/
}
