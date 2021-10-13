package com.example.hellking.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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

public class Community extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // 인텐트
    Intent intent;

    // 뷰 정보
    TextView name, email;
    ImageView icon;
    View v;

    // 파이어베이스
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    // 네비게이션 뷰
    NavigationView navigationView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 파이어베이스설정
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        // 프레그먼트 설정
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment())
                .commit();

        // 네비게이션 드로어 설정
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // 뷰 설정
        View v = navigationView.getHeaderView(0);
        name = (TextView)v.findViewById(R.id.text_name);
        email = (TextView)v.findViewById(R.id.text_id);
        icon = (ImageView)v.findViewById(R.id.person_img);

        // 파이어베이스를 통해 유저 정보 얻어옴
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

    }


    // 뒤로가기버튼누를시
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    // 각각의 네비게이션드로어 메뉴 클릭시 이동
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_info:
                intent = new Intent(Community.this, ProfileActivity.class);
                startActivity(intent);

                break;
            case R.id.nav_home:
                intent = new Intent(Community.this, MainActivity.class);
                startActivity(intent);


                break;
            case R.id.nav_exercise:
                intent = new Intent(this, MakeActivity.class);
                startActivity(intent);

                finish();
                break;
            case R.id.nav_community:
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
}
