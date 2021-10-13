package com.example.hellking.ui.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hellking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class VdieoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        // 인텐트를 통해 해당 운동 수신
        String name = getIntent().getExtras().getString("name");
        TextView exName = findViewById(R.id.title3);
        exName.setText(name);

        // 뷰 설정
        VideoView video = findViewById(R.id.videoView);

        // 파이어베이스에서 비디오 정보 받아옴
        final FirebaseFirestore dbv = FirebaseFirestore.getInstance();
        dbv.collection("video")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {  // 해당 비디오 진행
                                if (document.getId().equals(name)) {
                                    String uri=document.getData().toString();
                                    uri = uri.substring(1, uri.length()-2);
                                    Uri videoUri = Uri.parse(uri);
                                    video.setMediaController(new MediaController(VdieoActivity.this));
                                    video.setVideoURI(videoUri);

                                    video.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
                                        public void onPrepared(MediaPlayer mediaPlayer){
                                            video.start();
                                        }
                                    });
                                    break;
                                }
                            }
                        }
                    }
                });

        // 버튼 클릭시 뒤로가기
        Button btn = findViewById(R.id.select4);
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });
    }
}
