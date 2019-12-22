package com.example.codeforum.ui.blog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.codeforum.R;
import com.example.codeforum.Utils;

public class BlogActivity extends Activity {
    private ImageView blog_back;
    private ImageView blog_author_icon;
    private TextView blog_author_name;
    private TextView blog_author_phone;
    private TextView blog_title;
    private TextView blog_author_content;
    private TextView blog_classification;
    private ProgressBar blog_progress;
    private WebView blog_content;
    private ImageView blog_comment;
    private String name;
    private String phone;
    private String date;
    private String icon;
    private String title;
    private String mUrl;
    private String content;
    private String classification;
    private String comment_name;
    private final static int commentActivity=10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_blog);

        blog_back = (ImageView) findViewById(R.id.blog_back);
        blog_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri data = null;
                Intent intent = new Intent(null, data);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            name = intent.getStringExtra("name");
            phone = intent.getStringExtra("phone");
            date = intent.getStringExtra("date");
            icon = intent.getStringExtra("icon");
            title = intent.getStringExtra("title");
            mUrl = intent.getStringExtra("url");
            content = intent.getStringExtra("content");
            classification = intent.getStringExtra("classification");
            comment_name = intent.getStringExtra("comment_name");
        }

        blog_author_icon=(ImageView)findViewById(R.id.blog_author_icon);
        blog_author_name=(TextView)findViewById(R.id.blog_author_name);
        blog_author_phone=(TextView)findViewById(R.id.blog_author_phone);
        blog_title=(TextView)findViewById(R.id.blog_title);
        blog_progress=(ProgressBar)findViewById(R.id.blog_progress);
        blog_content=(WebView)findViewById(R.id.blog_content);
        blog_author_content=(TextView) findViewById(R.id.blog_author_content);
        blog_classification=(TextView) findViewById(R.id.blog_classification);
        blog_comment=(ImageView) findViewById(R.id.blog_comment);

        blog_author_icon.setImageBitmap(Utils.toRoundBitmap(Utils.stringtoBitmap(icon)));
        blog_author_name.setText(name);
        blog_author_phone.setText(phone);
        blog_author_phone.setText(date);
        blog_author_content.setText(content);
        blog_classification.setText("分类："+classification);
        blog_progress.setMax(100);
        blog_title.setText(title);
        setWebView(blog_content);

        blog_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(BlogActivity.this, CommentActivity.class);
                intent.putExtra("comment_name", comment_name);
                startActivityForResult(intent, commentActivity);
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setWebView(WebView wb) {
        WebSettings settings = wb.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);

        wb.loadUrl(mUrl);
        wb.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //view.loadUrl(String.valueOf(request.getUrl()));
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        wb.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                blog_progress.setProgress(newProgress);
            }
        });
    }
}
