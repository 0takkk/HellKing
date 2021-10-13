package com.example.hellking.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellking.R;
import com.example.hellking.models.ModelPost;
import com.example.hellking.ui.activities.PostActivity;
import com.example.hellking.ui.activities.PostDetailActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>{

    // 콘텍스트
    Context context;

    // 리스트
    List<ModelPost> postList;

    // 유저 Id
    String myUid;

    // 좋아요와 게시물 정보
    private DatabaseReference likesRef;
    private DatabaseReference postsRef;

    // 좋아요 상태
    boolean mProcessLike = false;

    // 생성자
    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }
    class MyHolder extends RecyclerView.ViewHolder {

        // 뷰 설정
        ImageView uPictureIv, pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikeTv, pCommentsTv;
        ImageButton moreBtn;
        ImageButton likeBtn, commentBtn;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // 뷰 설정
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikeTv = itemView.findViewById(R.id.pLikesTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentTv);

        }
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        // 선택된(해당 포지션에 맞는) 각각의 정보들 로드
        String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescription = postList.get(position).getpDescr();
        String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes();
        String pComments = postList.get(position).getpComments();

        // 현재 시간 설정
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        // 뷰에 표시
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikeTv.setText("좋아요 " + pLikes + "개");
        holder.pCommentsTv.setText("댓글 " + pComments + "개");

        // 좋아요 설정
        setLikes(holder, pId);

        try { // 유저 Dp 표시
            Picasso.get().load(uDp).placeholder(R.drawable.ic_user).into(holder.uPictureIv);
        } catch (Exception e) {

        }

        if (pImage.equals("noImage")) { // 이미지 미포함
            holder.pImageIv.setVisibility(View.GONE);
        }
        else { // 이미지 포함
            holder.pImageIv.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(pImage).into(holder.pImageIv);
            } catch (Exception e) {

            }
        }


        // 상세정보 클릭시 이벤트
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showMoreOptions(holder.moreBtn, uid, myUid, pId, pImage);
            }
        });

        // 좋아요 버튼 클릭시 이벤트
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;
                String postIde = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProcessLike) {
                            if(snapshot.child(postIde).hasChild(myUid)) { // 좋아요 취소시
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            }
                            else { // 좋아요 추가시
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked");
                                mProcessLike = false;

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        // 댓글 버튼 클릭시
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) { // 상세정보창으로 이동
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId);
                context.startActivity(intent);
            }
        });

    }

    // 좋아요 설정
    private void setLikes(MyHolder holder, String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postKey).hasChild(myUid)) { // 좋아요시
                    holder.likeBtn.setImageResource(R.drawable.ic_heart_red);
                }
                else { // 좋아요아닐시
                    holder.likeBtn.setImageResource(R.drawable.ic_heart_white);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // 상세정보 메소드
    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {

        // 팝업 메뉴  설정
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        // 팝업 메뉴 디테일 정보
        if(uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "삭제");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "편집");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "상세보기");

        // 팝업 메뉴 선택시
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == 0) { // 삭제
                    beginDelete(pId, pImage);
                }
                else if(id == 1) { // 편집
                    Intent intent = new Intent(context, PostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);

                }
                else if(id == 2) { // 상세정보보기
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    // 삭제 진행
    private void beginDelete(String pId, String pImage) {
        if(pImage.equals("noImage")) { // 이미지 미포함
            deleteWithoutImage(pId);
        }
        else { // 이미지 포함
            deleteWithImage(pId, pImage);
        }
    }

    // 이미지포함게시물삭제
    private void deleteWithImage(String pId, String pImage) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("삭제중...");

        // 파이어베이스에서 게시물 정보 삭제
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()) {
                                    ds.getRef().removeValue();
                                }
                                Toasty.custom(context, "게시글 삭제가 완료되었습니다.", R.drawable.ic_check, context.getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
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
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 이미지 미포함 게시물 삭제
    private void deleteWithoutImage(String pId) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("삭제중...");

        // 파이어베이스에서 게시물 삭제
        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                Toasty.custom(context, "게시글 삭제가 완료되었습니다.", R.drawable.ic_check, context.getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }


}
