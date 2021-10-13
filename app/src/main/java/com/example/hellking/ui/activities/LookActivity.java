package com.example.hellking.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hellking.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class LookActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look);

        // 해당 운동 이름 받아오고 뷰 설정
        String name = getIntent().getExtras().getString("name");
        TextView exName = findViewById(R.id.title3);
        exName.setText(name);

        // 해당 운동 설명 받아오고 뷰 설정
        String contents = getIntent().getExtras().getString("content");
        contents = contents.substring(1, contents.length()-2);
        TextView exContent = findViewById(R.id.textbox3);
        exContent.setText(contents);

        // 파이어스토어에서 해당 운동 사진 받아오고
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference rootRef = firebaseStorage.getReference();
        StorageReference imgRef = rootRef.child("picture/" + name +".png");

        // 해당 이미지에 뷰설정
        ImageView image = findViewById(R.id.imageView2);
        if(imgRef!=null){
            imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(LookActivity.this)
                            .load(uri)
                            .into(image);
                }
            });
        }

        // 동영상 보기 버튼 클릭시 리스너
        Button btn_vdieo = findViewById(R.id.select3);
        btn_vdieo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LookActivity.this, VdieoActivity.class);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });
    }
}