package com.example.hellking.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellking.R;
import com.example.hellking.models.ModelComment;
import com.example.hellking.ui.activities.ProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

// 댓글들을 위한 어뎁터
public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {

    // 콘텍스트
    Context context;

    // 리스트
    List<ModelComment> commentList;

    // 유저 Id, 게시물 Id
    String myUid, postId;

    // 생성자
    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        // 선택된(해당 포지션에 맞는) 각각의 정보들 로드
        String uid = commentList.get(position).getUid();
        String name = commentList.get(position).getuName();
        String email = commentList.get(position).getuEmail();
        String image = commentList.get(position).getuDp();
        String cid = commentList.get(position).getcId();
        String comment = commentList.get(position).getComment();
        String timestamp = commentList.get(position).getTimestamp();

        // 현재 시간 받아옴
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        // 뷰에 표시
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(pTime);

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_user).into(holder.avatarIv);
        } catch(Exception e) {

        }

        // 댓글 삭제 진행
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myUid.equals(uid)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setTitle("삭제");
                    builder.setMessage("정말로 삭제하시겠습니까?");
                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteComment(cid);
                            Toasty.custom(context, "댓글 삭제가 완료되었습니다.", R.drawable.ic_check, context.getResources().getColor(android.R.color.holo_blue_light), Toasty.LENGTH_SHORT, true, true).show();
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }
                else {
                    Toasty.error(context, "타인의 댓글은 지울 수 없습니다.", Toast.LENGTH_SHORT, true).show();
                }
            }
        });
    }

    // 댓글 삭제 메소드
    private void deleteComment(String cid) {
        // 파이어베이스에서 댓글 정보 삭제
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments = ""+snapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue(""+newCommentVal);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // 아이템 갯수 메소드
    @Override
    public int getItemCount() {
        return commentList.size();
    }

    // Holder 클래스
    class MyHolder extends RecyclerView.ViewHolder {

        ImageView avatarIv;
        TextView nameTv, commentTv, timeTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);



        }
    }

}
