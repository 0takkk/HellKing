package com.example.hellking.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeCustomDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.ClosureEdit;
import com.dev.materialspinner.MaterialSpinner;
import com.example.hellking.R;
import com.example.hellking.models.Model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.view.Change;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

import static com.example.hellking.ui.activities.LoginActivity.all_routine;
import static com.example.hellking.ui.activities.LoginActivity.models;
import static com.example.hellking.ui.activities.LoginActivity.routine_name;
import static com.example.hellking.ui.activities.MakeActivity.reAdapter;

public class ChangeActivity extends AppCompatActivity {
    ArrayList<String> choose_list = new ArrayList<>();
    Intent intent;
    // 리스트뷰 설정
    ListView listview;

    // 파이어베이스 설정
    String uid;
    FirebaseAuth firebaseAuth;

    // 운동 아이템 리스트
    ArrayList<String> chest_items = new ArrayList<>();
    ArrayList<String> back_items = new ArrayList<>();
    ArrayList<String> leg_items = new ArrayList<>();
    ArrayList<String> arm_items = new ArrayList<>();
    ArrayList<String> sh_items = new ArrayList<>();
    MaterialSpinner cspinner, lspinner, bspinner, aspinner, shspinner;
    ArrayAdapter cAdapter, lAdapter, bAdapter, aAdapter, shAdapter, list_Adapter;

    // 뷰 설정
    EditText edittext_ch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        // 선택된 운동의 포지션
        int position = getIntent().getExtras().getInt("change_position");

        Button btn_add = findViewById(R.id.select3);
        edittext_ch = findViewById(R.id.editText);

        btn_add.setOnClickListener(new View.OnClickListener(){ // 버튼 클릭 리스너
            public void onClick(View v){
                String text = edittext_ch.getText().toString();
                if(list_Adapter.getCount() == 0){ // 운동 리스트 비어있을시
                    Toasty.warning(ChangeActivity.this, "운동 리스트를 선택해주세요.", Toasty.LENGTH_SHORT, true).show();
                }
                else if(!routine_name.get(position).equals(text) && routine_name.contains(text)){   // 중복 이름일시
                    Toasty.warning(ChangeActivity.this, "중복된 이름이 존재합니다.", Toasty.LENGTH_SHORT, true).show();
                }
                else if(text.replace(" ","").equals("")){  // 이름 공백일시
                    Toasty.warning(ChangeActivity.this, "이름을 설정해주세요.", Toasty.LENGTH_SHORT, true).show();
                }
                else { // 예외의 경우가 아닌 경우 생성
                    new AwesomeCustomDialog(ChangeActivity.this)
                            .setTitle("루틴 설명 변경")
                            .setMessage("루틴에 대한 설명을 변경해주세요.")
                            .setColoredCircle(android.R.color.holo_blue_light)
                            .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
                            .setCancelable(true)
                            .setEditTextUnderlineColor(android.R.color.holo_blue_light)
                            .setEditTextColor(android.R.color.black)
                            .setCustomButtonText("완료")
                            .setCustomButtonbackgroundColor(android.R.color.holo_blue_light)
                            .setCustomButtonClick(new ClosureEdit() {
                                @Override
                                public Boolean exec(EditText editText) {
                                    // 리스트뷰에 있는 아이템 담기
                                    reAdapter.notifyDataSetChanged();
                                    for (int i = 0; i < list_Adapter.getCount(); i++)
                                        choose_list.add(list_Adapter.getItem(i).toString());

                                    // 해당 이미지 받아옴
                                    int img = models.get(position).getImg();
                                    // 지우고 갱신위함
                                    models.remove(position);
                                    // 리사이클러뷰에 표시하기 위해 모델에 추가(루틴 설명 비어있을시 '-'로 표시)
                                    if(editText.getText().toString().equals(""))
                                        models.add(position, new Model(text, "-", img));
                                    else
                                        models.add(position, new Model(text, editText.getText().toString(), img));

                                    // 파이어베이스에 만든 루틴 정보 추가
                                    String timeStamp = MainActivity.routineList.get(position).getrTimeStamp();
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Routines");
                                    ref.child(timeStamp).removeValue();

                                    // 루틴의 이름, 설명 추가
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("rId", timeStamp);
                                    hashMap.put("rName", text);
                                    if(editText.getText().toString().equals(""))
                                        hashMap.put("rDesc", "-");
                                    else
                                        hashMap.put("rDesc", editText.getText().toString());

                                    ref.child(timeStamp).setValue(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });

                                    // 루틴 안의 운동 저장
                                    HashMap<String, Object> hashMap2 = new HashMap<>();
                                    for (int i = 0; i < list_Adapter.getCount(); i++)
                                        hashMap2.put("routine"+i, list_Adapter.getItem(i).toString());

                                    ref.child(timeStamp).child("kind").setValue(hashMap2)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });


                                    // 지우고 다시 갱신작업 진행
                                    all_routine.remove(position);
                                    all_routine.add(position, choose_list); // add routines
                                    routine_name.remove(position);
                                    routine_name.add(position, text); // add names
                                    Toasty.custom(ChangeActivity.this, "루틴 변경이 완료되었습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                                    reAdapter.notifyDataSetChanged();
                                    MainActivity.mAdapter.clear();

                                    finish();
                                    return true;//return true; to hide the dialog
                                }
                            })
                            .setDoneButtonText("취소")
                            .setDoneButtonClick(new Closure() {
                                @Override
                                public void exec() {
                                    //click

                                }
                            }).show();
                }
            }
        });

        // 루틴 운동 아이템 받아옴
        ArrayList<String> ch_items = all_routine.get(position);

        // 루틴 이름 받아옴
        edittext_ch.setText(routine_name.get(position));

        // 리스트뷰 설정
        listview = (ListView)findViewById(R.id.listview_add);
        list_Adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, ch_items);
        listview.setAdapter(list_Adapter);


        // 운동정보 리스트에 담고 스피너에 추가
        chest_items.add("가슴 운동을 선택해주세요."); chest_items.add("플랫 벤치프레스"); chest_items.add("인클라인 벤치프레스"); chest_items.add("디클라인 벤치프레스");
        chest_items.add("플랫 덤벨프레스"); chest_items.add("인클라인 덤벨프레스");
        chest_items.add("딥스"); chest_items.add("케이블 크로스오버");
        chest_items.add("덤벨 플라이"); chest_items.add("체스트 프레스"); chest_items.add("펙 덱 플라이");
        chest_items.add("푸쉬업");
        cAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, chest_items);
        cspinner = findViewById(R.id.chest_spinner);
        cspinner.setAdapter(cAdapter);
        cspinner.setLabel("가슴");

        leg_items.add("하체 운동을 선택해주세요."); leg_items.add("스쿼트"); leg_items.add("와이드 스쿼트"); leg_items.add("내로우 스쿼트");
        leg_items.add("런지"); leg_items.add("핵 스쿼트"); leg_items.add("레그 익스텐션"); leg_items.add("라잉 레그컬");
        leg_items.add("시티드 레그컬"); leg_items.add("아웃타이"); leg_items.add("이너타이"); leg_items.add("스티프 데드리프트");
        lAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, leg_items);
        lspinner = findViewById(R.id.leg_spinner);
        lspinner.setAdapter(lAdapter);
        lspinner.setLabel("하체");

        sh_items.add("어깨 운동을 선택해주세요."); sh_items.add("밀리터리 프레스"); sh_items.add("덤벨 숄더 프레스"); sh_items.add("오버 헤드 프레스");
        sh_items.add("비하인드 넥 프레스"); sh_items.add("사이드 레터럴 레이즈"); sh_items.add("케이블 사이드 레터럴 레이즈");
        sh_items.add("프론트 레이즈"); sh_items.add("업라이트 로우"); sh_items.add("페이스풀"); sh_items.add("벤트오버레이즈");
        shAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, sh_items);
        shspinner = findViewById(R.id.sh_spinner);
        shspinner.setAdapter(shAdapter);
        shspinner.setLabel("어깨");

        back_items.add("등 운동을 선택해주세요."); back_items.add("렛 풀 다운"); back_items.add("케이블 암 풀 다운"); back_items.add("바벨 로우");
        back_items.add("T바 로우"); back_items.add("케이블 로우"); back_items.add("풀업");
        back_items.add("원암 덤벨 로우"); back_items.add("루마니안 데드리프트");
        bAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, back_items);
        bspinner = findViewById(R.id.back_spinner);
        bspinner.setAdapter(bAdapter);
        bspinner.setLabel("등");

        arm_items.add("팔 운동을 선택해주세요."); arm_items.add("바벨컬"); arm_items.add("덤벨컬");
        arm_items.add("해머컬"); arm_items.add("EZ바 리버스컬"); arm_items.add("라잉 바벨 트라이셉스 익스텐션"); arm_items.add("시티드 덤벨 트라이셉스 익스텐션");
        arm_items.add("시티드 바벨 트라이셉스 익스텐션"); arm_items.add("원암 덤벨 킥 백");
        aAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arm_items);
        aspinner = findViewById(R.id.arm_spinner);
        aspinner.setAdapter(aAdapter);
        aspinner.setLabel("팔");

        // 각각의 운동이 선택되면 리스트뷰에 추가
        cspinner.setItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position != 0) {
                    list_Adapter.add((String) adapterView.getSelectedItem());
                }
                adapterView.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        shspinner.setItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position != 0) {
                    list_Adapter.add((String) adapterView.getSelectedItem());
                }
                adapterView.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        bspinner.setItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position != 0) {
                    list_Adapter.add((String) adapterView.getSelectedItem());
                }
                adapterView.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        aspinner.setItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position != 0) {
                    list_Adapter.add((String) adapterView.getSelectedItem());
                }
                adapterView.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        lspinner.setItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position != 0) {
                    list_Adapter.add((String) adapterView.getSelectedItem());
                }
                adapterView.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                list_Adapter.remove(list_Adapter.getItem(position));
                list_Adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    // 리스트뷰에 담긴 운동 클릭시 운동정보 확인을 위해 LookActivity로 이동
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            intent = new Intent(ChangeActivity.this, LookActivity.class);
            db.collection("exercise")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task){
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    if(document.getId().equals(list_Adapter.getItem(position))) {
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
    };

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