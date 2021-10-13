package com.example.hellking.models;

// 리사이클러 뷰를 위한 모델 클래스
public class Model {

    private String title, Description; // 루틴 이름, 설명
    private int img; // 루틴 이미지

    public Model(String title, String description, int img) {
        this.title = title;
        this.Description = description;
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }
}
