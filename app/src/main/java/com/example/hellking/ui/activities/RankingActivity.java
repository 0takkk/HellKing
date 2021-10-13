package com.example.hellking.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.hellking.R;
import com.example.hellking.ui.fragmens.HomeFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

public class RankingActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // 네비게이션 드로어
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    // 파이어베이스 설정
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseUser user;
    String myName;
    int myRank;

    // 리스트 설정
    ArrayList<String> rlist;
    ArrayAdapter<String> rAdapter;

    // 뷰 설정
    ListView listView;
    TextView myRanking;
    TextView name, email;
    ImageView icon;
    View v;

    // 인텐트 설정
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        // 툴바 설정
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 뷰 설정
        rlist = new ArrayList<>();
        listView = findViewById(R.id.listview_rank);
        myRanking = findViewById(R.id.myRanking);

        // 네비게이션 드로어 설정
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        navigationView.bringToFront();
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        navigationView.setNavigationItemSelectedListener(this);

        // 네비게이션 드로어 상단에 파이어베이스를 통해 유저 정보 얻어옴
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        View v = navigationView.getHeaderView(0);
        name = (TextView)v.findViewById(R.id.text_name);
        email = (TextView)v.findViewById(R.id.text_id);
        icon = (ImageView)v.findViewById(R.id.person_img);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    myName = "" + ds.child("name").getValue();
                    String qname = "" + ds.child("name").getValue();
                    String qemail = "" + ds.child("email").getValue();
                    String qimage = "" + ds.child("image").getValue();

                    if(qname.length() > 0) { // 이름이 존재할시
                        name.setText(qname + "회원님 반갑습니다!");
                    }else { // 이름이 비어있을시
                        name.setText("익명회원님 반갑습니다!");
                    }
                    email.setText(qemail);
                    try { // 프로필 이미지 존재할시
                        if(qimage.toString().length() > 0) {
                            Picasso.get().load(qimage).into(icon);
                        }else { // 프로필 이미지 존재하지않을시
                            icon.setImageResource(R.drawable.ic_user);
                        }

                    } catch(Exception e) {
                        Picasso.get().load(R.drawable.ic_user).into(icon);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // 파이어베이스의 exercisetime을 이용해 해당 유저 랭킹 찾기
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query1 = userDbRef.orderByChild("exercisetime");
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int myrank = 0;
                for(DataSnapshot ds : snapshot.getChildren()) {
                    Log.d("SangHyun", ds.child("name").getValue() +" " + myName);
                    if(ds.child("name").getValue().equals(myName)) {
                        myRank = (int)snapshot.getChildrenCount() - (myrank);
                        SpannableStringBuilder ssb = new SpannableStringBuilder(myName + "님은 " + myRank + "등 입니다.");
                        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#FFD400")), myName.length()+3, myName.length()+6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        myRanking.setText(ssb);
                    }
                    myrank += 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // 1~10등까지의 랭킹 리스트뷰에 표기
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query2 = userDbRef.orderByChild("exercisetime").limitToLast(10);
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int rank = 0;
                rlist.clear();
                for(DataSnapshot ds : snapshot.getChildren()) { // 등수 추출
                    long ranking = snapshot.getChildrenCount() - rank++;
                    String name = "" + ds.child("name").getValue();
                    int time = Integer.parseInt("" + ds.child("exercisetime").getValue());

                    String name_time = ranking + "등! " + name + "님 : " + String.format("%02d시간 %02d분 %02d초", time/3600, time%3600/60, (time%3600)%60);
                    rlist.add(0, name_time);
                }
                rAdapter = new ArrayAdapter<String>(RankingActivity.this, android.R.layout.simple_list_item_1, rlist);
                listView.setAdapter(rAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // 뒤로가기
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    // 네비게이션 메뉴 설정
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_info:
                intent = new Intent(RankingActivity.this, ProfileActivity.class);
                startActivity(intent);

                break;
            case R.id.nav_home:
                intent = new Intent(RankingActivity.this, MainActivity.class);
                startActivity(intent);


                break;
            case R.id.nav_exercise:
                intent = new Intent(this, MakeActivity.class);
                startActivity(intent);

                finish();
                break;
            case R.id.nav_community:
                intent = new Intent(this, Community.class);
                startActivity(intent);

                break;

            case R.id.nav_rank:
                break;

            case R.id.nav_find:
                intent = new Intent(this, MapActivity.class);
                startActivity(intent);

                break;

            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();

                finish();
                break;

        }

        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}