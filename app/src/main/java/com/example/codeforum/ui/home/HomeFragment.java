package com.example.codeforum.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.codeforum.R;
import com.example.codeforum.component.myadpater.GridIconAdapter;
import com.example.codeforum.component.myadpater.Icon;
import com.example.codeforum.ui.blog.SortedBlogActivity;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private GridView grid_class;
    private BaseAdapter mAdapter = null;
    private ArrayList<Icon> mData = null;
    private static final int sortedBlogActivity = 9;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        grid_class = (GridView) root.findViewById(R.id.grid_class);

        mData = new ArrayList<com.example.codeforum.component.myadpater.Icon>();
        mData.add(new com.example.codeforum.component.myadpater.Icon(R.drawable.ic_java_primary_64dp, "Java"));
        mData.add(new com.example.codeforum.component.myadpater.Icon(R.drawable.ic_python_primary_64dp, "Python"));
        mData.add(new com.example.codeforum.component.myadpater.Icon(R.drawable.ic_c_primary_64dp, "C语言"));
        mData.add(new com.example.codeforum.component.myadpater.Icon(R.drawable.ic_javascript_primary_64dp, "JS"));
        mData.add(new com.example.codeforum.component.myadpater.Icon(R.drawable.ic_web_primary_64dp, "Web"));
        mData.add(new com.example.codeforum.component.myadpater.Icon(R.drawable.ic_php_primary_64dp, "php"));
        mData.add(new com.example.codeforum.component.myadpater.Icon(R.drawable.ic_android_primary_64dp, "Android"));
        mData.add(new com.example.codeforum.component.myadpater.Icon(R.drawable.ic_linux_primary_64dp, "Linux"));
        mData.add(new com.example.codeforum.component.myadpater.Icon(R.drawable.ic_ios_primary_64dp, "iOS"));

        //设置九宫格导航栏样式
        mAdapter = new GridIconAdapter<com.example.codeforum.component.myadpater.Icon>(mData, R.layout.grid_class_icon) {
            @Override
            public void bindView(ViewHolder holder, Icon obj) {
                holder.setImageResource(R.id.img_icon, obj.getiId());
                holder.setText(R.id.txt_icon, obj.getiName());
            }
        };

        grid_class.setAdapter(mAdapter);

        //导航栏跳转事件
        grid_class.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), SortedBlogActivity.class);
                intent.putExtra("title_name", mData.get(position).getiName());
                startActivityForResult(intent, sortedBlogActivity);
            }
        });
        return root;
    }
}