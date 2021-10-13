package com.example.hellking.utils;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellking.R;
import com.example.hellking.models.Model;
import com.example.hellking.ui.activities.AddActivity;
import com.example.hellking.ui.activities.LoginActivity;
import com.example.hellking.ui.activities.MainActivity;
import com.example.hellking.ui.activities.MakeActivity;

import java.util.ArrayList;


// 리사이클러뷰 어뎁터
public class ReAdapter extends RecyclerView.Adapter<ReHolder> {
    // 콘텍스트
    Context c;
    // 인텐트
    Intent intent;
    // 모델
    ArrayList<Model> models;


    public ReAdapter(Context c, ArrayList<Model> models) {
        this.c = c;
        this.models = models;
    }




    @NonNull
    @Override
    public ReHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, null);

        return new ReHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReHolder holder, int position) {

        // 뷰 설정
        holder.mTitle.setText(models.get(position).getTitle());
        holder.mDes.setText(models.get(position).getDescription());
        holder.mImageView.setImageResource(models.get(position).getImg());



        // 운동 메뉴 리스너 선택 리스너
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClickListener(View v, int position) { // 추가하기에 대해 인텐트를 이용해 AddActivity로 이동
                if(models.get(position).getTitle().equals("추가하기")) {
                    intent = new Intent(c.getApplicationContext(), AddActivity.class);
                    c.startActivity(intent);
                }
                else{ // 아닐경우 선택한 정보 받아오기
                    MainActivity.mAdapter = new ArrayAdapter<String>(c, android.R.layout.simple_list_item_1, LoginActivity.all_routine.get(position));
                    MakeActivity.listview.setAdapter(MainActivity.mAdapter);
                    MakeActivity.selected_position = position;
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return models.size();
    }

}