package com.example.studentmanagement.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class User {
    private String email;
    private String password;
    private String name;
    private int age;
    private String phone;
    private boolean status;
    private List<String> loginHistory;
    
    public User(String email, String password, String name, int age, String phone, boolean status, List<String> loginHistory) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.status = status;
        this.loginHistory = loginHistory;
    }
}
