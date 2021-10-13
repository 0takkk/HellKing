package com.example.hellking.models;

// 루틴을 위한 클래스
public class ModelRoutine {
    String rId; // 루틴 Id

    // 생성자
    public ModelRoutine(String rTimeStamp) {
        this.rId = rTimeStamp;
    }

    // Getter, Setter
    public String getrTimeStamp() {
        return rId;
    }

    public void setrTimeStamp(String rTimeStamp) {
        this.rId = rTimeStamp;
    }
}
