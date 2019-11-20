package com.example.codeforum.ui.home;

import android.os.Bundle;
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
import com.example.codeforum.myadpater.GridIconAdapter;
import com.example.codeforum.myadpater.Icon;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private GridView grid_class;
    private BaseAdapter mAdapter = null;
    private ArrayList<Icon> mData = null;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        /*final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        grid_class = (GridView) root.findViewById(R.id.grid_class);

        mData = new ArrayList<com.example.codeforum.myadpater.Icon>();
        mData.add(new com.example.codeforum.myadpater.Icon(R.mipmap.ic_launcher, "图标1"));
        mData.add(new com.example.codeforum.myadpater.Icon(R.mipmap.ic_launcher, "图标2"));
        mData.add(new com.example.codeforum.myadpater.Icon(R.mipmap.ic_launcher, "图标3"));
        mData.add(new com.example.codeforum.myadpater.Icon(R.mipmap.ic_launcher, "图标4"));
        mData.add(new com.example.codeforum.myadpater.Icon(R.mipmap.ic_launcher, "图标5"));
        mData.add(new com.example.codeforum.myadpater.Icon(R.mipmap.ic_launcher, "图标6"));
        mData.add(new com.example.codeforum.myadpater.Icon(R.mipmap.ic_launcher, "图标7"));

        //设置九宫格导航栏样式
        mAdapter = new GridIconAdapter<com.example.codeforum.myadpater.Icon>(mData, R.layout.grid_class_icon) {
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

            }
        });
        return root;
    }
}