package com.example.codeforum.component.discoverView;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.codeforum.R;

public class DiscoverView extends ConstraintLayout {
    public DiscoverView(Context context) {
        this(context, null);
    }

    public DiscoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //在构造函数中将Xml中定义的布局解析出来。
        LayoutInflater.from(context).inflate(R.layout.discover_view, this, true);
    }

    public void setImageResource(Bitmap bitmap) {
        ImageView mImage = (ImageView) findViewById(R.id.discover_view_icon);
        mImage.setImageBitmap(bitmap);
    }

    public void setNameText(String text) {
        TextView mText = (TextView) findViewById(R.id.discover_view_name);
        mText.setText(text);
    }

    public void setPhoneText(String text) {
        TextView mText = (TextView) findViewById(R.id.discover_view_phone);
        mText.setText(text);
    }

    public void setTitleText(String text) {
        TextView mText = (TextView) findViewById(R.id.discover_view_title);
        mText.setText(text);
    }

    public void setContentText(String text) {
        TextView mText = (TextView) findViewById(R.id.discover_view_content);
        mText.setText(text);
    }

    public void setClassificationText(String text) {
        TextView mText = (TextView) findViewById(R.id.discover_view_classification);
        mText.setText(text);
    }

    public void setImage(int id) {
        ImageView mImage = (ImageView) findViewById(R.id.discover_view_icon);
        mImage.setImageResource(id);
    }

    public void hideTitle(){
        TextView mText = (TextView) findViewById(R.id.discover_view_title);
        mText.setVisibility(GONE);
    }
    public void hideClassification(){
        TextView mText = (TextView) findViewById(R.id.discover_view_classification);
        mText.setVisibility(GONE);
    }
}
