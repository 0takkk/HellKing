package com.example.hellking.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeCustomDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.ClosureEdit;
import com.example.hellking.R;
import com.example.hellking.models.ModelPost;
import com.example.hellking.utils.AdapterPosts;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {

    // 파이어베이스
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    StorageReference storageReference;
    String storagePath = "Users_Profile_Cover_Imgs/";

    // 뷰 설정
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;
    RecyclerView postsRecyclerView;

    //프로그래스바 다이얼로그
    AwesomeProgressDialog pd;

    // uri
    Uri image_uri = null;

    // 퍼미션
    String profileOrCoverPhoto;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] cameraPermissions;
    String[] storagePermissions;

    // 리스트
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 파이어베이스 유저 정보 로드
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        // 퍼미션 정보 설정
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // 게시물 리스트 설정
        postList = new ArrayList<>();

        // 뷰 설정
        avatarIv = findViewById(R.id.avartarIv);
        coverIv = findViewById(R.id.coverIv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        fab = findViewById(R.id.fab2);
        postsRecyclerView = findViewById(R.id.recyclerview_posts);

        // 프로그래스바 다이얼로그 설정
        pd = new AwesomeProgressDialog(this);

        // 파이어베이스를 통해 유저 정보 로드
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Log.d("SangHyun", "Count");
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        Picasso.get().load(image).into(avatarIv);
                    } catch(Exception e) {
                        Picasso.get().load(R.drawable.ic_user).into(avatarIv);
                    }

                    try {
                        Picasso.get().load(cover).into(coverIv);
                    } catch(Exception e) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });

        checkUserStatus();
        loadMyPosts();
    }

    // 검색 진행
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.community, menu) ;

        MenuItem item = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s)) {
                    searchMyPosts(s);
                }
                else {
                    loadMyPosts();;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s)) {
                    searchMyPosts(s);
                }
                else {
                    loadMyPosts();;
                }
                return false;
            }
        });

        return true ;
    }

    // 내가 쓴 게시물 로드
    private void loadMyPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        // 파이어베이스 uid 비교를 통해 게시물 로드
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    postList.add(myPosts);

                    adapterPosts = new AdapterPosts(ProfileActivity.this, postList);
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(ProfileActivity.this, "에러가 발생했습니다.", Toast.LENGTH_SHORT, true).show();
            }
        });
    }

    // 내 게시물 찾기
    private void searchMyPosts(String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        // uid 비교를 통해 내 게시물 파이어베이스에서 찾기
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if(myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                        myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        postList.add(myPosts);
                    }

                    adapterPosts = new AdapterPosts(ProfileActivity.this, postList);
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toasty.error(ProfileActivity.this, "에러가 발생했습니다.", Toast.LENGTH_SHORT, true).show();
            }
        });
    }

    // 현재 유저의 상태 체크 메소드
    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }
        else {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        }
    }

    // 편집 다이얼로그
    private void showEditProfileDialog() {

        String options[] = {"프로필 편집", "커버 사진 편집", "이름 편집", "핸드폰 번호 편집"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("선택");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0) {
                    pd.setMessage("프로필 사진 업데이트 중...").setTitle("프로필 업데이트").setColoredCircle(android.R.color.holo_blue_light);
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();
                }
                else if(i == 1) {
                    pd.setMessage("커버 사진 업데이트 중...").setTitle("프로필 업데이트").setColoredCircle(android.R.color.holo_blue_light);
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();
                }
                else if(i == 2) {
                    pd.setMessage("이름 업데이트 중...").setTitle("프로필 업데이트").setColoredCircle(android.R.color.holo_blue_light);
                    showNamePhoneUpdateDialog("name");

                }
                else if(i == 3) {
                    pd.setMessage("핸드폰 번호 업데이트 중...").setTitle("프로필 업데이트").setColoredCircle(android.R.color.holo_blue_light);
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });

        builder.create().show();
    }

    // 이름, 핸드폰 번호 업데이트
    private void showNamePhoneUpdateDialog(String key) {
        if(key == "name") { // 이름 업데이트
            new AwesomeCustomDialog(this)
                .setTitle("프로필 업데이트")
                .setMessage("변경할 이름을 입력해주세요.")
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
                        String value = editText.getText().toString().trim();

                        if(!TextUtils.isEmpty(value)) {
                            pd.show();
                            HashMap<String, Object> result = new HashMap<>();
                            result.put(key, value);

                            databaseReference.child(user.getUid()).updateChildren(result)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.hide();
                                            Toasty.custom(ProfileActivity.this, "이름 업데이트가 완료되었습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.hide();
                                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            if(key.equals("name")) { // 이름 변경 후 나머지 정보 업데이트

                                DatabaseReference ref = firebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds : snapshot.getChildren()) {
                                            String child = ds.getKey();
                                            snapshot.getRef().child(child).child("uName").setValue(value);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            String child = ds.getKey();
                                            if(snapshot.child(child).hasChild("Comments")) {
                                                String child1 = ""+snapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                                            String child = ds.getKey();
                                                            snapshot.getRef().child(child).child("uName").setValue(value);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                        else {
                            Toasty.warning(ProfileActivity.this, "이름을 입력해주세요.", Toast.LENGTH_SHORT, true).show();
                        }

                        return true;//return true; to hide the dialog
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
        } else { // 핸드폰 번호 업데이트
            new AwesomeCustomDialog(this)
                .setTitle("프로필 업데이트")
                .setMessage("변경할 핸드폰 번호를 입력해주세요.")
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
                        String value = editText.getText().toString().trim();

                        if(!TextUtils.isEmpty(value)) {
                            pd.show();
                            HashMap<String, Object> result = new HashMap<>();
                            result.put(key, value);

                            databaseReference.child(user.getUid()).updateChildren(result)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.hide();
                                            Toasty.custom(ProfileActivity.this, "핸드폰 번호 업데이트가 완료되었습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.hide();
                                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            if(key.equals("name")) {

                                DatabaseReference ref = firebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds : snapshot.getChildren()) {
                                            String child = ds.getKey();
                                            snapshot.getRef().child(child).child("uName").setValue(value);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            String child = ds.getKey();
                                            if(snapshot.child(child).hasChild("Comments")) {
                                                String child1 = ""+snapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                                            String child = ds.getKey();
                                                            snapshot.getRef().child(child).child("uName").setValue(value);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                        else {
                            Toasty.warning(ProfileActivity.this, "핸드폰 번호를 입력해주세요.", Toast.LENGTH_SHORT, true).show();
                        }

                        return true;//return true; to hide the dialog
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

    }

    // 이미지 받아올 장소
    private void showImagePicDialog() {

        String options[] = {"카메라", "갤러리"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이미지 로드 장소");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0) { // 카메라
                    if(!checkCameraPermission()) {
                        requestCameraPermission();
                    }
                    else { // 갤러리
                        pickFromCamera();
                    }
                }
                else if(i == 1) { // 퍼미션
                    if(!checkStoragePermission()) {
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });

        builder.create().show();

    }

    private boolean checkStoragePermission() { // 저장소 퍼미션 체크
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() { // 저장소 퍼미션 요구
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() { // 카메라 퍼미션 체크
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() { // 카메라 퍼미션 요구
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    // 퍼미션 관련
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toasty.warning(ProfileActivity.this, "카메라 및 저장소 권한이 필요합니다.", Toast.LENGTH_SHORT, true).show();
                    }
                } else {
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toasty.warning(ProfileActivity.this, "저장소 권한이 필요합니다.", Toast.LENGTH_SHORT, true).show();
                    }
                } else {

                }
            }
            break;
        }
    }

    // 갤러리에서 이미지 추출
    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    // 카메라에서 이미지 추출
    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    // 퍼미션 관련
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE) {

                uploadProfileCoverPhoto(image_uri);
            }
        }
    }

    // 커버사진, 프로필 사진 업로드
    private void uploadProfileCoverPhoto(Uri image_uri) {

        // 저장소 주소 설정
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();

        // 파이어베이스에 저장
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        Uri downloadUri = uriTask.getResult();

                        if(uriTask.isSuccessful()) {
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto, downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.hide();
                                            Toasty.custom(ProfileActivity.this, "이미지 업데이트가 완료되었습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                                        }
                                    }). addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.hide();
                                    Toasty.error(ProfileActivity.this, "이미지 업데이트에 실패했습니다.", Toast.LENGTH_SHORT, true).show();
                                }
                            });

                            if (profileOrCoverPhoto.equals("image")) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            String child = ds.getKey();
                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            String child = ds.getKey();
                                            if(snapshot.child(child).hasChild("Comments")) {
                                                String child1 = ""+snapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                                            String child = ds.getKey();
                                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }

                        }
                        else {
                            pd.hide();
                            Toasty.error(ProfileActivity.this, "에러가 발생했습니다.", Toast.LENGTH_SHORT, true).show();
                        }
                    }
                }). addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

}