package com.example.codeforum.ui.friend;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.codeforum.FindFriendActivity;
import com.example.codeforum.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FriendFragment extends Fragment {

    private FriendViewModel friendViewModel;
    private FloatingActionButton find_friend;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        friendViewModel =
                ViewModelProviders.of(this).get(FriendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_friend, container, false);
        final TextView textView = root.findViewById(R.id.text_friend);
        friendViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        find_friend=(FloatingActionButton)root.findViewById(R.id.find_friend);
        find_friend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent=new Intent();
                intent.setClass(getActivity(), FindFriendActivity.class);
                startActivity(intent);
            }
        });
        return root;
    }
}