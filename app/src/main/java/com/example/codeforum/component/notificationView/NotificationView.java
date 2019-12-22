package com.example.codeforum.component.notificationView;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.codeforum.R;

public class NotificationView extends ConstraintLayout {
    public NotificationView(Context context) {
        this(context, null);
    }

    public NotificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //在构造函数中将Xml中定义的布局解析出来。
        LayoutInflater.from(context).inflate(R.layout.notification_view, this, true);
    }

    public void setImageResource(Bitmap bitmap) {
        ImageView mImage = (ImageView) findViewById(R.id.notification_view_icon);
        mImage.setImageBitmap(bitmap);
    }

    public void setNameText(String text) {
        TextView mText = (TextView) findViewById(R.id.notification_view_name);
        mText.setText(text);
    }

    public void setPhoneText(String text) {
        TextView mText = (TextView) findViewById(R.id.notification_view_phone);
        mText.setText(text);
    }

    public void setMessageText(String text) {
        TextView mText = (TextView) findViewById(R.id.notification_view_message);
        mText.setText(text);
    }

    public void setImage(int id) {
        ImageView mImage = (ImageView) findViewById(R.id.notification_view_icon);
        mImage.setImageResource(id);
    }
}
