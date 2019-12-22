package com.example.codeforum.component.messageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.codeforum.R;

public class MessageView extends LinearLayout {
    public MessageView(Context context) {
        this(context, null);
    }

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //在构造函数中将Xml中定义的布局解析出来。
        LayoutInflater.from(context).inflate(R.layout.message_view, this, true);
    }

    public void setImageResource(Bitmap bitmap) {
        ImageView mImage = (ImageView) findViewById(R.id.message_view_icon);
        mImage.setImageBitmap(bitmap);
    }

    public void setTextViewText(String text) {
        TextView mText = (TextView) findViewById(R.id.message_view_text);
        mText.setText(text);
    }

    public void setDirection(String pos){
        LinearLayout parent=(LinearLayout)findViewById(R.id.message_view);
        if(pos.equals("right")){
            parent.setLayoutDirection(LAYOUT_DIRECTION_RTL);
        }else{
            parent.setLayoutDirection(LAYOUT_DIRECTION_LTR);
        }
    }
}