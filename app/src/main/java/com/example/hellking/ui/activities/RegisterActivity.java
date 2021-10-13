package com.example.hellking.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hellking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    // 뷰 설정
    EditText mEmailEt, mPasswordEt, mNameEt, mPhoneEt;
    String name, phone;
    Button mRegisterBtn;

    // 프로그래스바 다이얼로그
    ProgressDialog progressDialog;

    // 파이어베이스 설정
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 뷰 설정
        mNameEt = findViewById(R.id.name_text);
        mPhoneEt = findViewById(R.id.phone_text);
        mEmailEt = findViewById(R.id.editTextTextEmailAddress2);
        mPasswordEt = findViewById(R.id.editTextTextPassword2);
        mRegisterBtn = findViewById(R.id.button_register);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("회원가입중...");

        // 파이어베이스 설정
        mAuth = FirebaseAuth.getInstance();

        // 가입 버튼 클릭시 이벤트
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                name = mNameEt.getText().toString();
                phone = mPhoneEt.getText().toString();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { // 이메일 형식 잘못됐을시
                    mEmailEt.setError("잘못된 형식의 이메일입니다.");
                    mEmailEt.setFocusable(true);
                }
                else if (password.length() < 6) { // 패스워드 형식 잘못됐을시
                    mPasswordEt.setError("패스워드가 너무 짧습니다.");
                    mPasswordEt.setFocusable(true);
                }
                else {
                    registerUser(email, password);
                }
            }
        });

    }

    // 회원가입 진행
    private void registerUser(String email, String password) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            // 유저 정보 맵에 저장 후 파이어베이스에 저장
                            String email = user.getEmail();
                            String uid = user.getUid();
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", name);
                            hashMap.put("phone", phone);
                            hashMap.put("image", "");
                            hashMap.put("cover", "");
                            hashMap.put("exercisetime", 0);

                            FirebaseDatabase database = FirebaseDatabase.getInstance();

                            DatabaseReference reference = database.getReference("Users");

                            reference.child(uid).setValue(hashMap);
                            Toasty.custom(RegisterActivity.this, "회원가입이 완료되었습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toasty.error(RegisterActivity.this, "인증에 실패했습니다.", Toast.LENGTH_SHORT, true).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
            }
        });
    }
}