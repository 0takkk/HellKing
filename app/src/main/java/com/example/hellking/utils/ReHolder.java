package com.example.hellking.utils;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellking.R;

public class ReHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener{

    // 뷰
    ImageView mImageView;
    TextView mTitle, mDes;
    ItemClickListener itemClickListener;


    public ReHolder(@NonNull View itemView) {
        super(itemView);

        // 뷰 설정
        this.mImageView = itemView.findViewById(R.id.image1);
        this.mTitle = itemView.findViewById(R.id.title);
        this.mDes = itemView.findViewById(R.id.desc);

        // 리스너 설정
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    // 클릭 리스너
    @Override
    public void onClick(View view) {
        this.itemClickListener.onItemClickListener(view, getLayoutPosition());
    }

    // 아이템클릭리스너
    public void setItemClickListener(ItemClickListener ic) {

        this.itemClickListener = ic;
    }

    // 콘텍스트 메뉴
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.add(0, 121, getAdapterPosition(), "변경");
        contextMenu.add(0, 122, getAdapterPosition(), "삭제");

    }


}
