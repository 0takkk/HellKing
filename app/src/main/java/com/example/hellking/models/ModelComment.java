package com.example.hellking.models;

// 댓글을 위한 클래스
public class ModelComment {
    // 댓글 Id, 댓글 내용, 작성시간, 유저 Id, 유저 Email, 유저 사진, 유저 이름
    String cId, comment, timestamp, uid, uEmail, uDp, uName;

    // 생성자들
    public ModelComment() {
    }

    public ModelComment(String cId, String comment, String timestamp, String uid, String uEmail, String uDp, String uName) {
        this.cId = cId;
        this.comment = comment;
        this.timestamp = timestamp;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uName = uName;
    }

    // Getter, Setter
    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }
}
