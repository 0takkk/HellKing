package com.example.hellking.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellking.R;
import com.example.hellking.models.Model;
import com.example.hellking.utils.ReAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;

import static com.example.hellking.ui.activities.LoginActivity.models;


public class MakeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // 파이어베이스 설정
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    String uid;

    // 선택된 포지션
    public static int selected_position = -1;

    // 뷰 설정
    public static ListView listview;
    RecyclerView mRecyclerView;
    Toolbar toolbar;
    Button select;
    TextView name, email;
    ImageView icon;

    // 네비게이션 드로어
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    // 어뎁터
    public static ReAdapter reAdapter;

    // 인텐트
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make);

        // 뷰 설정
        select = findViewById(R.id.select);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 어뎁터 설정
        reAdapter = new ReAdapter(this, models);
        mRecyclerView.setAdapter(reAdapter);

        // 네비게이션 드로어 설정
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        listview = findViewById(R.id.listview);

        navigationView.bringToFront();
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        navigationView.setNavigationItemSelectedListener(this);

        // 운동 시작 버튼 누를시
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.select) {
                    if(selected_position == -1)
                        Toasty.error(MakeActivity.this, "루틴을 선택해주세요.", Toast.LENGTH_SHORT, true).show();
                    else {
                        new SweetAlertDialog(MakeActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                                .setCustomImage(R.drawable.ic_fitness)
                                .setTitleText("운동 시작")
                                .setContentText("운동을 시작하시겠습니까?")
                                .setConfirmText("확인")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) { // ExerciseActivity 이동

                                        intent = new Intent(MakeActivity.this, ExerciseActivity.class);
                                        startActivity(intent);
                                        sDialog.dismissWithAnimation();

                                    }
                                })
                                .setCancelButton("취소", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .show();
                    }
                }
            }
        });

        // 네비게이션 드로어 상단에 파이어베이스를 통해 유저 정보 얻어옴
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        checkUserStatus();


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

                    if(qname.length() > 0) {
                        name.setText(qname + "회원님 반갑습니다!");
                    }else {
                        name.setText("익명회원님 반갑습니다!");
                    }
                    email.setText(qemail);
                    try {
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

        // 운동 클릭시 해당 운동 정보 조회
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                intent = new Intent(MakeActivity.this, LookActivity.class);
                db.collection("exercise")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task){
                                if(task.isSuccessful()){
                                    for(QueryDocumentSnapshot document : task.getResult()){ // 운동 정보 전송
                                        if(document.getId().equals(MainActivity.mAdapter.getItem(position))) {
                                            intent.putExtra("name", document.getId());
                                            intent.putExtra("content", document.getData().toString());
                                            startActivity(intent);
                                            break;
                                        }
                                    }
                                }
                            }
                        });
            }
        });
    }

    // 뒤로가기 버튼시
    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    // 네비게이션 메뉴 클릭시
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_info:
                intent = new Intent(MakeActivity.this, ProfileActivity.class);
                startActivity(intent);

                break;
            case R.id.nav_home:
                intent = new Intent(MakeActivity.this, MainActivity.class);
                startActivity(intent);


                break;
            case R.id.nav_exercise:
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
                intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();

                finish();
                break;

        }


        return true;
    }
    //
//    private ArrayList<Model> getMyList() {
//        ArrayList<Model> models = new ArrayList<>();
//        models.add(new Model("초급자 등 운동", "초급자를 위한 등 부수기!", R.drawable.ic_color_chat));
//        models.add(new Model("초급자 가슴 운동", "초급자를 위한 가슴 부수기!", R.drawable.ic_color_chat));
//        models.add(new Model("초급자 하체 운동", "초급자를 위한 하체 부수기!", R.drawable.ic_color_chat));
//        models.add(new Model("추가하기", "새로운 루틴을 생성해보세요!", R.drawable.ic_add));
//
//
//        return models;
//    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int index = item.getOrder();
        switch(item.getItemId()) {
            case 121:
                if(index == LoginActivity.all_routine.size()) {
                    Toasty.error(this, "추가하기 버튼은 변경할 수 없습니다.", Toast.LENGTH_SHORT, true).show();
                }else if(index <= LoginActivity.all_routine.size()-1 && index >= LoginActivity.all_routine.size()-8){
                    Toasty.error(this, "기존 루틴은 변경할 수 없습니다.", Toast.LENGTH_SHORT, true).show();
                }
                else {
                    intent = new Intent(MakeActivity.this, ChangeActivity.class);
                    intent.putExtra("change_position", index);
                    startActivity(intent);
                }
                return true;
            case 122:
                if(index == LoginActivity.all_routine.size() ) {
                    Toasty.error(this, "추가하기 버튼은 지울 수 없습니다.", Toast.LENGTH_SHORT, true).show();
                }else if(index <= LoginActivity.all_routine.size()-1 && index >= LoginActivity.all_routine.size()-8){
                    Toasty.error(this, "기존 버튼은 지울 수 없습니다.", Toast.LENGTH_SHORT, true).show();
                }else {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Routines");
                    ref.child(MainActivity.routineList.get(index).getrTimeStamp()).removeValue();
                    Log.d("sanggg", MainActivity.routineList.get(index).getrTimeStamp());

                    LoginActivity.all_routine.remove(index);
                    LoginActivity.routine_name.remove(index);
                    LoginActivity.models.remove(index);
                    reAdapter.notifyDataSetChanged();
                    MainActivity.mAdapter.clear();

                }
                return true;
            default:
                return super.onContextItemSelected(item);
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