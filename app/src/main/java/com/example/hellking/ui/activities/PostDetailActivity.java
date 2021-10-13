package com.example.hellking.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hellking.R;
import com.example.hellking.models.ModelComment;
import com.example.hellking.utils.AdapterComments;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class PostDetailActivity extends AppCompatActivity {

    // 유저들 정보
    String hisUid, myUid, myEmail, myName, myDp, postId, pLikes, hisDp, hisName, pImage;

    // 뷰 설정
    ImageView uPictureIv, pImageIv;
    TextView nameTv, pTimeTiv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    ImageButton likeBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    // 리스트
    List<ModelComment> commentList;
    AdapterComments adapterComments;

    // 프로그래스바 다이얼로그
    ProgressDialog pd;

    // 상태 설정
    boolean mProcessComment = false;
    boolean mProcessLike = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // 인텐트를 통해 게시물 Id 수신
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        // 뷰 설정
        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        nameTv = findViewById(R.id.uNameTv);
        pTimeTiv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        pCommentsTv = findViewById(R.id.pCommentTv);
        recyclerView = findViewById(R.id.recyclerView_comment);
        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);


        loadPostInfo(); // 게시물 정보 로드

        checkUserStatus(); // 유저 상태 로드

        loadUserInfo(); // 유저 정보 로드

        setLikes(); // 좋아요 상태 세팅

        loadComments(); // 댓글 로드

        // 각각의 버튼에 맞는 이벤트 진행
        sendBtn.setOnClickListener(new View.OnClickListener() { // 댓글 작성
            @Override
            public void onClick(View view) {
                postComment();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() { // 좋아요 추가

            @Override
            public void onClick(View view) {
                likePost();
            }
        });
    }

    // 댓글 작성
    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        commentList = new ArrayList<>();

        // 파이어베이스에 댓글 관련 정보 추가
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot ds : snapshot.getChildren()) {
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    adapterComments = new AdapterComments(getApplicationContext(), commentList, myUid, postId);
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // 좋아요 추가
    private void setLikes() {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        // 파이어베이스에 좋아요 관련 정보 추가
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postId).hasChild(myUid)) { // 좋아요 추가
                    likeBtn.setImageResource(R.drawable.ic_heart_red);
                }
                else { // 좋아요 취소
                    likeBtn.setImageResource(R.drawable.ic_heart_white);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions();
            }
        });
    }

    // 상세정보보기
    private void showMoreOptions() {
        // 팝업 메뉴 설정
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        // 팝업메뉴 디테일내용
        if(hisUid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "삭제");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "편집");
        }

        // 클릭시 진행
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == 0) { // 삭제
                    beginDelete();
                }
                else if(id == 1) { // 편집
                    Intent intent = new Intent(PostDetailActivity.this, PostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);

                }
                return false;
            }
        });
        popupMenu.show();
    }

    // 삭제 진행
    private void beginDelete() {
        if(pImage.equals("noImage")) { // 이미지 미포함
            deleteWithoutImage();
        }
        else { // 이미지 포함
            deleteWithImage();
        }
    }

    // 이미지 포함 제거
    private void deleteWithImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("삭제중...");

        // 파이어베이스에서 정보 제거
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()) {
                                    ds.getRef().removeValue();
                                }
                                Toasty.custom(PostDetailActivity.this, "게시물 삭제가 완료되었습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 이미지 미포함 제거
    private void deleteWithoutImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("삭제중...");

        // 파이어베이스에서 정보 제거
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                Toasty.custom(PostDetailActivity.this, "게시물 삭제가 완료되었습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // 좋아요 게시
    private void likePost() {

        // 파이어베이스에 게시글에 좋아요 정보와 따로 좋아요 정보 추가
        mProcessLike = true;
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessLike) {
                    if(snapshot.child(postId).hasChild(myUid)) { // 좋아요 제거
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;


                    }
                    else { // 좋아요 추가
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLike = false;


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // 댓글 작성
    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("댓글 작성중...");

        // 파이어베이스에 댓글 정보 추가
        String comment = commentEt.getText().toString().trim();

        if(TextUtils.isEmpty(comment)) {
            Toasty.warning(PostDetailActivity.this, "댓글을 입력해주세요.", Toast.LENGTH_SHORT, true).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toasty.custom(PostDetailActivity.this, "댓글 작성이 완료되었습니다.", R.drawable.ic_check, getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                        commentEt.setText("");
                        updateCommentCount();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 댓글 개수 갱신
    private void updateCommentCount() {
        mProcessComment = true;

        // 파이어베이스에 댓글 개수 저장
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = ""+snapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) + 1;
                ref.child("pComments").setValue(""+newCommentVal);
                mProcessComment = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // 유저 정보 로드
    private void loadUserInfo() {

        // 파이어베이스로 유저 정보 로드
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    myName = "" + ds.child("name").getValue();
                    myDp = "" + ds.child("image").getValue();

                    try {
                        if(myDp.length() > 0) {
                            Picasso.get().load(myDp).placeholder(R.drawable.ic_user).into(cAvatarIv);
                        } else {
                            Picasso.get().load(R.drawable.ic_user).placeholder(R.drawable.ic_user).into(cAvatarIv);
                        }
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_user).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // 게시글 정보 로드
    private void loadPostInfo() {
        //파이어베이스로 게시글 정보 로드
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDescr = ""+ds.child("pDescr").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String commentCount = ""+ds.child("pComments").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText("좋아요 " + pLikes + " 개");
                    pTimeTiv.setText(pTime);
                    pCommentsTv.setText("댓글 " + commentCount + "개");

                    nameTv.setText(hisName);

                    if (pImage.equals("noImage")) { // 이미지 미포함
                        pImageIv.setVisibility(View.GONE);
                    }
                    else { // 이미지 포함
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        } catch (Exception e) {

                        }
                    }

                    try {
                        if(hisDp.length() > 0) { // 이미지 정보 포함
                            Picasso.get().load(hisDp).placeholder(R.drawable.ic_user).into(uPictureIv);
                        } else { // 이미지정보 미포함
                            Picasso.get().load(R.drawable.ic_user).placeholder(R.drawable.ic_user).into(uPictureIv);
                        }
                    }catch(Exception e) {
                        Picasso.get().load(R.drawable.ic_user).into(uPictureIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // 현재 유저의 상태 체크 메소드
    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            myEmail = user.getEmail();
            myUid = user.getUid();

        }else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}