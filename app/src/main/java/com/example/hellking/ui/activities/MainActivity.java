package com.example.hellking.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.hellking.R;
import com.example.hellking.models.Model;
import com.example.hellking.models.ModelComment;
import com.example.hellking.models.ModelRoutine;
import com.example.hellking.utils.AdapterComments;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // 파이어베이스
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    GoogleSignInAccount signInAccount;
    String uid;

    // 인텐트
    Intent intent;

    // 네비게이션 드로어
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    // 뷰 설정
    Toolbar toolbar;
    TextView name, email;
    ImageView icon;
    View v;

    // 루틴 리스트
    public static List<ModelRoutine> routineList;

    // 어뎁터
    public static ArrayAdapter<String> mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 파이어베이스 설정
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        // 뷰 설정
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        // 네비게이션 드로어 설정
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
                    String qname = "" + ds.child("name").getValue();
                    String qemail = "" + ds.child("email").getValue();
                    String qimage = "" + ds.child("image").getValue();

                    if(qname.length() > 0) { // 이름 지정 안되있을시 익명회원으로 표기
                        name.setText(qname + "회원님 반갑습니다!");
                    }else {
                        name.setText("익명회원님 반갑습니다!");
                    }
                    email.setText(qemail);
                    try { // 이름 없을시 기본 유저 이미지 적용
                        if(qimage.toString().length() > 0) {
                            Picasso.get().load(qimage).into(icon);
                        }else {
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

        loadUserRoutine(); // 유저 루틴 정보 로드
    }

    // 뒤로가기 버튼
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    // 루틴 정보 로드 메소드
    public void loadUserRoutine() {
        routineList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Routines");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    String rId = "" + ds.child("rId").getValue();
                    String name = "" + ds.child("rName").getValue();
                    String desc = "" + ds.child("rDesc").getValue();
                    routineList.add(new ModelRoutine(rId));
                    LoginActivity.routine_name.add(0, name);
                    LoginActivity.models.add(0, new Model(name, desc, R.drawable.ic_user));

                    // 유저 Id에 맞는 루틴 정보 받아옴
                    ArrayList<String> user_routine = new ArrayList<>();
                    for(DataSnapshot ds2 : ds.child("kind").getChildren()) {
                        user_routine.add("" + ds2.getValue());
                        Log.d("SangHyun!", "" + ds2.getValue());
                    }
                    LoginActivity.all_routine.add(0, user_routine);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // navigation drawer select listener
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_info:
                intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);

                break;
            case R.id.nav_home:
                break;
            case R.id.nav_exercise:
                intent = new Intent(this, MakeActivity.class);
                startActivity(intent);

                finish();
                break;
            case R.id.nav_community:
                intent = new Intent(this, Community.class);
                startActivity(intent);

                finish();
                break;

            case R.id.nav_rank:
                intent = new Intent(getApplicationContext(), RankingActivity.class);
                startActivity(intent);

                finish();
                break;

            case R.id.nav_find:
                break;

            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();

                finish();
                break;

        }


        return true;
    }

    // card view click listener
    public void home_click(View v) {
        switch(v.getId()) {
            case R.id.card1:
                intent = new Intent(getApplicationContext(),MakeActivity.class);
                startActivity(intent);//액티비티 띄우기

                break;
            case R.id.card3:
                intent = new Intent(getApplicationContext(),Community.class);
                startActivity(intent);//액티비티 띄우기

                break;
            case R.id.card4:
                intent = new Intent(getApplicationContext(), RankingActivity.class);
                startActivity(intent);

                break;
            case R.id.card5:
                intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);

                break;
        }
    }

    // 현재 유저의 상태 체크 메소드
    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}