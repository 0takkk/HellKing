package com.example.hellking.ui.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeCustomDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.ClosureEdit;
import com.example.hellking.R;
import com.example.hellking.models.Model;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {
    // 루틴의 종류들 설정
    public static ArrayList<String> routine_1 = new ArrayList<>();
    public static ArrayList<String> routine_2 = new ArrayList<>();
    public static ArrayList<String> routine_3 = new ArrayList<>();
    public static ArrayList<String> routine_4 = new ArrayList<>();
    public static ArrayList<String> routine_5 = new ArrayList<>();
    public static ArrayList<String> routine_6 = new ArrayList<>();
    public static ArrayList<String> routine_7 = new ArrayList<>();
    public static ArrayList<String> routine_8 = new ArrayList<>();
    public static ArrayList<ArrayList<String>> all_routine = new ArrayList<>();// 모든 루틴의 정보를 담고 있는 리스트
    public static ArrayList<String> routine_name = new ArrayList<>(); // 모든 루틴의 이름을 담고 있는 리스트
    public static ArrayList<Model> models = new ArrayList<>(); // 리사이클러뷰에 적용할 모델 클래스의 정보

    // 파이어베이스 구글 로그인
    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    SignInButton mGoogleLoginBtn;

    // 뷰 설정
    EditText mEmailEt, mPasswordEt;
    Button mLoginBtn;
    TextView mRecoverPassTv;

    // 파이어베이스 설정
    private FirebaseAuth mAuth;

    // 프로그래스다이얼로그
    AwesomeProgressDialog pd;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 구글 로그인 진행
        routine_initialize();
        getHashKey();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        // 뷰 설정
        mEmailEt = findViewById(R.id.editTextTextEmailAddress);
        mPasswordEt = findViewById(R.id.editTextTextPassword);
        mLoginBtn = findViewById(R.id.button_login);
        mRecoverPassTv = findViewById(R.id.find_pw);
        mGoogleLoginBtn = findViewById(R.id.btn_google);

        // 비밀번호 찾기 진행
        mRecoverPassTv.setPaintFlags(mRecoverPassTv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // 단순 밑줄 디자인
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });

        // 버튼 클릭 리스너
        mLoginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {  // 이메일, 패스워드 입력 후 로그인
                String email = mEmailEt.getText().toString();
                String passw = mPasswordEt.getText().toString();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // 이메일 형식 잘못되었을 시
                    mEmailEt.setError("잘못된 이메일 형식입니다.");
                    mEmailEt.setFocusable(true);
                }
                else { // 로그인
                    loginUser(email, passw);
                }
            }
        });

        // 구글 로그인
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) { // 구글 로그인 인텐트 전송 후 결과 확인 후 로그인 진행
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // 회원가입 진행
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    // 비밀번호 찾는 메소드
    private void showRecoverPasswordDialog() {

        new AwesomeCustomDialog(this)
                .setTitle("비밀번호 찾기")
                .setMessage("회원가입에 사용한 이메일주소를 입력해주세요.")
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
                        //click
                        String email = editText.getText().toString().trim(); // 이메일 확인 후
                        beginRecovery(email); // 이메일을 활용한 비밀번호 찾기
                        return true;
                    }
                })
                .setDoneButtonText("취소")
                .setDoneButtonClick(new Closure() {
                    @Override
                    public void exec() {
                        //click

                    }
                })
                .show();
    }

    // 비밀번호를 찾기 위한 이메일 발송
    private void beginRecovery(String email) {
        pd = new AwesomeProgressDialog(this);
        pd.setMessage("이메일발송중...")
                .setTitle("비밀번호 찾기").setColoredCircle(android.R.color.holo_blue_light).show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.hide();
                Toasty.custom(LoginActivity.this, "이메일 발송이 완료되었습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.hide();
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 로그인 진행
    private void loginUser(String email, String passw) {
        pd = new AwesomeProgressDialog(this);
        pd.setMessage("로그인중...")
            .setTitle("로그인").setColoredCircle(android.R.color.holo_blue_light).show();
        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.hide();
                            // 파이어베이스를 활용한 로그인 성공
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));

                            Toasty.custom(LoginActivity.this, "로그인에 성공했습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                        } else {
                            pd.hide();
                            // 로그인 실패
                            Toasty.error(LoginActivity.this, "로그인에 실패했습니다.", Toast.LENGTH_SHORT, true).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.hide();

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    // 파이어베이스 구글 로그인
    private void firebaseAuthWithGoogle(String idToken) {
        pd = new AwesomeProgressDialog(this);
        pd.setMessage("로그인중...")
                .setTitle("로그인").setColoredCircle(android.R.color.holo_blue_light).show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 구글 로그인에 관해 유저 정보들 저장
                            FirebaseUser user = mAuth.getCurrentUser();

                            pd.hide();
                            if(task.getResult().getAdditionalUserInfo().isNewUser()) {
                                String email = user.getEmail();
                                String uid = user.getUid();
                                String name = user.getDisplayName();
                                String img_url = user.getPhotoUrl().toString();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("email", email); // 이메일
                                hashMap.put("uid", uid); // 유저 Id
                                hashMap.put("name", name); // 이름
                                hashMap.put("phone", ""); // 휴대폰번호
                                hashMap.put("image", img_url); // 프로필사진
                                hashMap.put("cover", ""); // 커버사진
                                hashMap.put("exercisetime", 0); // 운동시간

                                FirebaseDatabase database = FirebaseDatabase.getInstance();

                                DatabaseReference reference = database.getReference("Users");

                                reference.child(uid).setValue(hashMap);

                            }

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));


                            Toasty.custom(LoginActivity.this, "로그인에 성공했습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toasty.error(LoginActivity.this, "로그인에 실패했습니다.", Toast.LENGTH_SHORT, true).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    // 루틴 정보들 초기화 시작
    public void routine_initialize() {
        // 모델 초기화
        models.add(new Model("초급자 등 운동", "초급자를 위한 등 부수기!", R.drawable.ic_backs));
        models.add(new Model("초급자 가슴 운동", "초급자를 위한 가슴 부수기!", R.drawable.ic_chest));
        models.add(new Model("초급자 하체 운동", "초급자를 위한 하체 부수기!", R.drawable.ic_calves));
        models.add(new Model("중급자 등 운동", "중급자를 위한 등 부수기!", R.drawable.ic_backs));
        models.add(new Model("중급자 가슴 운동", "중급자를 위한 가슴 부수기!", R.drawable.ic_chest));
        models.add(new Model("중급자 하체 운동", "중급자를 위한 하체 부수기!", R.drawable.ic_calves));
        models.add(new Model("중급자 어깨 운동", "중급자를 위한 어깨 부수기!", R.drawable.ic_shoulder));
        models.add(new Model("중급자 팔 운동", "중급자를 위한 팔 부수기!", R.drawable.ic_strong));

        models.add(new Model("추가하기", "새로운 루틴을 생성해보세요!", R.drawable.ic_add));

        // 루틴 초기화
        routine_name.add("초급자 등 운동"); routine_name.add("초급자 가슴 운동"); routine_name.add("초급자 하체 운동");
        routine_1.add("렛 풀 다운"); routine_1.add("케이블 암 풀 다운"); routine_1.add("바벨 로우"); routine_1.add("T바 로우"); routine_1.add("케이블 로우"); routine_1.add("풀업");
        routine_2.add("푸쉬업"); routine_2.add("플랫 벤치프레스"); routine_2.add("인클라인 벤치프레스"); routine_2.add("케이블 크로스오버"); routine_2.add("체스트 프레스"); routine_2.add("딥스");
        routine_3.add("레그 익스텐션"); routine_3.add("스쿼트"); routine_3.add("와이드 스쿼트"); routine_3.add("런지"); routine_3.add("라잉 레그컬"); routine_3.add("시티드 레그컬");
        routine_4.add("렛 풀 다운"); routine_4.add("케이블 암 풀 다운"); routine_4.add("바벨 로우"); routine_4.add("T바 로우"); routine_4.add("케이블 로우"); routine_4.add("풀업");
        routine_5.add("푸쉬업"); routine_5.add("플랫 벤치프레스"); routine_5.add("인클라인 벤치프레스"); routine_5.add("케이블 크로스오버"); routine_5.add("체스트 프레스");
        routine_6.add("레그 익스텐션"); routine_6.add("스쿼트"); routine_6.add("와이드 스쿼트"); routine_6.add("런지"); routine_6.add("라잉 레그컬"); routine_6.add("시티드 레그컬");
        routine_7.add("밀리터리 프레스"); routine_7.add("덤벨 숄더 프레스"); routine_7.add("오버 헤드 프레스"); routine_7.add("비하인드 넥 프레스"); routine_7.add("사이드 레터럴 레이즈"); routine_7.add("프론트 레이즈");
        routine_8.add("바벨컬"); routine_8.add("덤벨컬"); routine_8.add("해머컬"); routine_8.add("라잉 바벨 트라이셉스 익스텐션"); routine_8.add("시티드 덤벨 트라이셉스 익스텐션"); routine_8.add("시티드 바벨 트라이셉스 익스텐션");


        all_routine.add(routine_1); all_routine.add(routine_2); all_routine.add(routine_3); all_routine.add(routine_4); all_routine.add(routine_5); all_routine.add(routine_6); all_routine.add(routine_7); all_routine.add(routine_8);
    }
    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }
}
