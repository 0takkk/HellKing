package com.example.hellking.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog;
import com.example.hellking.R;
import com.example.hellking.models.Model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;

public class ExerciseActivity extends AppCompatActivity {

    TextView myOutput;
    TextView myRec;
    TextView exerNow;
    TextView exerNowProg;
    TextView exerBefo;
    TextView exerNex;
    TextView timerTotal;
    TextView exerRoutName;
    TextView exerTotalTimeText;

    Button myBtnStart;
    Button myBtnRec;

    ProgressBar prog;
    ProgressBar progTotal;

    long totalTime; // 10800초 = 3시간 , 지정된 운동의 총 시간
    long totalExer; // 현재 사용자 총 운동시간
    long exerTime; // 300초 = 5분 , 지정된 운동시간

    final static int Init = 0;
    final static int Run = 1;
    final static int Pause = 2;

    int cur_Status = Init; //현재의 상태를 저장할변수를 초기화함.
    long weekTime;
    long myPauseTime;
    long myTime;
    String exerciseTime = "";
    int index = 0;
    String routName;

    ArrayList<String[]> exercise = new ArrayList<String[]>();
    HashMap<String, String> allExercise = new HashMap<>();

    int value2;
    long record;

    AwesomeProgressDialog pd;

    String name, email, uid, dp, phone, cover;
    int exercisetime;

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_acitivty);

        // 파이어베이스 연동
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        allExercise_initialize();

        // 파이어베이스에 저장된 운동과 안드로이드에 저장된 운동시간을 조합
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        int size = LoginActivity.all_routine.get(MakeActivity.selected_position).size();
        for (int i = 0; i < size; i++) {
            Iterator<String> keys = allExercise.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.equals(LoginActivity.all_routine.get(MakeActivity.selected_position).get(i))) {
                    exerciseTime = allExercise.get(key);
                    exercise.add(new String[]{LoginActivity.all_routine.get(MakeActivity.selected_position).get(i), exerciseTime});
                    break;
                }
            }
            if (i != size - 1) exercise.add(new String[]{"휴식", "40"}); // 운동 중간에 휴식시간(40초) 세팅
        }


        exerNow = findViewById(R.id.textview_ex_name); // 지금운동

        exerBefo = findViewById(R.id.textview_ex_before); // 전의 운동
        exerNex = findViewById(R.id.textview_ex_next); // 후의 운동

        exerNowProg = findViewById(R.id.textview_ex_time); // 지금운동 - 프로그레스바 주제
        exerRoutName = findViewById(R.id.textview_ex_total); // 루틴제목

        timerTotal = findViewById(R.id.exerTimeText); // 지금운동 시간
        exerTotalTimeText = findViewById(R.id.exerTotalTimeText); // 총 운동 시간


        myOutput = (TextView) findViewById(R.id.textview_timer_now);
        myRec = (TextView) findViewById(R.id.textview_timer_total);
        myBtnStart = (Button) findViewById(R.id.imageView3);
        myBtnRec = (Button) findViewById(R.id.imageView4);

        prog = findViewById(R.id.textview_now_prog);
        progTotal = findViewById(R.id.textview_total_prog);

        // 파이어베이스에 들어갈 데이터 세팅
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("image").getValue();
                    cover = "" + ds.child("cover").getValue();
                    phone = "" + ds.child("phone").getValue();
                    exercisetime = Integer.parseInt("" + ds.child("exercisetime").getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // 초기 세팅
        routName = "사용자 루틴 지정";
        exerRoutName.setText(routName + " 운동 시간");
        for (int i = 0; i < exercise.size(); i++) {
            totalTime += Long.parseLong(exercise.get(i)[1]);
        }

        exerTotalTimeText.setText(String.format("%02d:%02d:%02d", totalTime / 3600, totalTime % 3600 / 60, (totalTime % 3600) % 60));

        exerTime = Long.parseLong(exercise.get(0)[1]);

        exerChange();

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    // 파이어베이스에 저장된 유저 상태
    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();
            name = user.getDisplayName();

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    // 타이머 일시정지 함수
    public void pause() {
        myTimer.removeMessages(0); //핸들러 메세지 제거
        myPauseTime = SystemClock.elapsedRealtime();
        myBtnStart.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_playbutton));
    }

    public void myOnClick(View v) {
        switch (v.getId()) {
            case R.id.imageView3: //시작버튼을 클릭했을때 현재 상태값에 따라 다른 동작을 할수있게끔 구현.
                switch (cur_Status) {
                    case Init:
                        weekTime = SystemClock.elapsedRealtime();
                        System.out.println(weekTime);
                        //myTimer이라는 핸들러를 빈 메세지를 보내서 호출
                        myTimer.sendEmptyMessage(0);
                        myBtnStart.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_pause)); //버튼의 문자"시작"을 "멈춤"으로 변경
                        myBtnRec.setEnabled(false); //기록버튼 비활성
                        cur_Status = Run; //현재상태를 런상태로 변경
                        break;
                    case Run:
                        pause();
                        cur_Status = Pause;
                        myBtnRec.setEnabled(true); // 기록버튼 활성
                        break;
                    case Pause:
                        long now = SystemClock.elapsedRealtime();
                        myTimer.sendEmptyMessage(0);
                        myBtnStart.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_pause));
                        weekTime += (now - myPauseTime);
                        cur_Status = Run;
                        break;
                }
                break;
            case R.id.imageView4: // 정지버튼
                switch (cur_Status) {
                    case Pause:
                        pause(); // 일시정지 후 다이얼로그 띄우기
                        setDialog();
                        break;

                    /*case Pause: // 리셋
                        //핸들러를 멈춤
                        myTimer.removeMessages(0);
                        myOutput.setText("00:00:00");
                        myRec.setText("00:00:00");
                        prog.setProgress(0);
                        progTotal.setProgress(0);
                        cur_Status = Init;
                        myBtnRec.setEnabled(false);
                        break;*/
                }
                break;

        }
    }

    Handler myTimer = new Handler() {
        public void handleMessage(Message msg) {
            myOutput.setText(getTimeOut());
            if (value2 == 100) { // 최종 종료시 일시정지
                prog.setProgress(100);
                progTotal.setProgress(100);
                pause();
                cur_Status = Pause;
                myBtnStart.setEnabled(false);
                myBtnRec.setEnabled(true);
            } else {
                myTimer.sendEmptyMessage(0);
            }
        }
    };

    //현재시간을 계속 구해서 출력하는 메소드
    String getTimeOut() {
        long now = SystemClock.elapsedRealtime(); //애플리케이션이 실행되고나서 실제로 경과된 시간
        long outTime = now - weekTime;
        System.out.println(now - now);
        System.out.println(now);

        System.out.println(outTime);
        myTime = outTime / 1000; // 초단위
        setProgressBar();

        String easy_outTime = String.format("%02d:%02d:%02d", (outTime / 1000) / 3600, ((outTime / 1000) % 3600) / 60, ((outTime / 1000) % 3600) % 60);
        return easy_outTime;
    }

    public void setProgressBar() {
        int value = (int) ((myTime * 100 / exerTime)); // 현재 운동의 프로그레스 바

        record = totalExer + myTime;
        value2 = (int) ((record * 100 / totalTime)); // 총 운동들의 프로그레스 바
        myRec.setText(String.format("%02d:%02d:%02d", record / 3600, (record % 3600) / 60, (record % 3600) % 60));

        if (value < 100) {
            prog.setProgress(value);
            progTotal.setProgress(value2);
        } else if (value == 100 && value2 != 100) { // 100% 달성했을 경우 = 다음 운동 교체과정
            totalExer += myTime; // 운동시간 합치기
            progTotal.setProgress((int) (totalExer * 100 / totalTime));

            weekTime = SystemClock.elapsedRealtime(); // 시간 초기화

            exerChange(); // 이전, 현재, 다음 운동 바꾸기
        }
    }

    public void exerChange() {
        // 운동바꾸기 함수
        if (exercise.size() != 1) {
            if (index == 0) { // 처음 운동
                exerBefo.setText("정보 없음");
                exerNex.setText(exercise.get(index + 1)[0]);
            } else if (index == exercise.size() - 1) {
                exerBefo.setText(exercise.get(index - 1)[0]);
                exerNex.setText("정보 없음");
            } else { // 마지막 운동
                exerBefo.setText(exercise.get(index - 1)[0]);
                exerNex.setText(exercise.get(index + 1)[0]);
            }
        } else { // 운동을 하나만 선택할 경우
            exerBefo.setText("정보 없음");
            exerNex.setText("정보 없음");
        }

        exerNow.setText(exercise.get(index)[0]);

        // 휴식시간 표시
        if (exercise.get(index)[0].equals("휴식"))
            exerNowProg.setText(exercise.get(index)[0] + " 시간");
        else exerNowProg.setText(exercise.get(index)[0] + " 운동 시간");
        exerTime = Integer.parseInt(exercise.get(index)[1]);

        timerTotal.setText(String.format("%02d:%02d:%02d", exerTime / 3600, exerTime % 3600 / 60, (exerTime % 3600) % 60));

        index++;
    }

    // 기록 다이얼로그 설정
    public void setDialog() {
        new SweetAlertDialog(ExerciseActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setCustomImage(R.drawable.ic_stopwatch)
                .setTitleText("기록 : " + String.format("%02d:%02d:%02d", record / 3600, (record % 3600) / 60, (record % 3600) % 60))
                .setContentText("기록을 저장하시겠습니까?")
                // 확인 버튼
                .setConfirmText("확인")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        pd = new AwesomeProgressDialog(ExerciseActivity.this);
                        pd.setMessage("업데이트중...")
                                .setTitle("기록 저장").setColoredCircle(android.R.color.holo_blue_light).show();
                        updateUser();

                        sDialog.dismissWithAnimation();
                    }
                })
                // 취소 버튼
                .setCancelButton("취소", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    // 파이어베이스 연동 사용자 운동기록 저장
    public void updateUser() {
        exercisetime += record;
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("uid", uid);
        hashMap.put("name", name);
        hashMap.put("email", email);
        hashMap.put("dp", dp);
        hashMap.put("phone", phone);
        hashMap.put("cover", cover);
        hashMap.put("exercisetime", exercisetime);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) { // 기록을 파이어베이스에 저장
                        pd.hide();
                        Toasty.custom(ExerciseActivity.this, "기록저장이 완료되었습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.hide();
                Toast.makeText(ExerciseActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 각 운동에 대한 운동시간 세팅
    public void allExercise_initialize() {
        allExercise.put("플랫 벤치프레스", "592");
        allExercise.put("인클라인 벤치프레스", "485");
        allExercise.put("디클라인 벤치프레스", "472");
        allExercise.put("플랫 덤벨프레스", "420");
        allExercise.put("인클라인 덤벨프레스", "530");
        allExercise.put("딥스", "410");
        allExercise.put("케이블 크로스오버", "500");
        allExercise.put("덤벨 플라이", "600");
        allExercise.put("체스트 프레스", "580");
        allExercise.put("펙 덱 플라이", "490");
        allExercise.put("푸쉬업", "530");
        allExercise.put("스쿼트", "540");
        allExercise.put("와이드 스쿼트", "540");
        allExercise.put("내로우 스쿼트", "540");
        allExercise.put("런지", "580");
        allExercise.put("핵 스쿼트", "510");
        allExercise.put("레그 익스텐션", "480");
        allExercise.put("라잉 레그컬", "520");
        allExercise.put("시티드 레그컬", "435");
        allExercise.put("아웃타이", "410");
        allExercise.put("이너타이", "410");
        allExercise.put("스티프 데드리프트", "570");
        allExercise.put("밀리터리 프레스", "550");
        allExercise.put("덤벨 숄더 프레스", "550");
        allExercise.put("오버 헤드 프레스", "510");
        allExercise.put("비하인드 넥 프레스 ", "560");
        allExercise.put("사이드 레터럴 레이즈", "560");
        allExercise.put("케이블 사이드 레터럴 레이즈", "490");
        allExercise.put("프론트 레이즈", "530");
        allExercise.put("업라이트 로우", "520");
        allExercise.put("페이스풀", "400");
        allExercise.put("벤트오버레이즈", "430");
        allExercise.put("렛 풀 다운", "560");
        allExercise.put("케이블 암 풀 다운", "600");
        allExercise.put("바벨 로우", "560");
        allExercise.put("T바 로우", "560");
        allExercise.put("케이블 로우", "550");
        allExercise.put("풀업", "530");
        allExercise.put("원암 덜벨 로우", "530");
        allExercise.put("루마니안 데드리프트", "510");
        allExercise.put("바벨컬", "480");
        allExercise.put("덤벨컬", "470");
        allExercise.put("해머컬", "450");
        allExercise.put("EZ바 리버스컬", "440");
        allExercise.put("라잉 바벨 트라이셉스 익스텐션", "530");
        allExercise.put("시티드 덤벨 트라이셉스 익스텐션", "530");
        allExercise.put("시티드 바벨 트라이셉스 익스텐션", "560");
        allExercise.put("원암 덤벨 킥 백", "10"); // 시연 영상 찍기 위해 짧게 설정함

    }
}