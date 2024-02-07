package com.example.studentmanagement.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class Student {
    private String id;
    private String name;
    private String phone;
    private String gender;
    private String major;
    private List<String> certificates;

    public Student(String id, String name, String phone, String gender, String major, List<String> certificates) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.gender = gender;
        this.major = major;
        this.certificates = certificates;
    }
}
