package com.example.studentmanagement.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.util.Log;

import com.example.studentmanagement.LoginActivity;
import com.example.studentmanagement.R;
import com.example.studentmanagement.databinding.ActivityAddUserBinding;
import com.example.studentmanagement.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.muddz.styleabletoast.StyleableToast;

public class AddUserActivity extends AppCompatActivity {
    private ActivityAddUserBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private CollectionReference reference;
    private Intent intent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("users");
        
        binding.btnAdd.setOnClickListener(view -> {
            addUser();
        });

        // Hiện back button để trở về ListUserActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Đổi title của Navigation Bar
        getSupportActionBar().setTitle("Add new user");
    }
    
    private void addUser() {
        String email = binding.etEmail.getText().toString();
        String name = binding.etName.getText().toString();
        String age = binding.etAge.getText().toString();
        String phone = binding.etPhone.getText().toString();

        if(name.equals("") || age.equals("")) {
            StyleableToast.makeText(this, "Please enter full information", R.style.errorToast).show();
            return;
        }

        if(!isEmail(email)) {
            StyleableToast.makeText(this, "This is not a valid email", R.style.errorToast).show();
            return;
        }

        if(!isPhone(phone)) {
            StyleableToast.makeText(this, "This is not a valid phone", R.style.errorToast).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, phone)
            .addOnCompleteListener(this, task1 -> {
                if (task1.isSuccessful()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    user.put("password", phone); // mật khẩu mặc định
                    user.put("name", name);
                    user.put("age", Integer.parseInt(age));
                    user.put("phone", phone);
                    user.put("status", true);
                    user.put("loginHistory", new ArrayList<String>());

                    reference.add(user)
                            .addOnCompleteListener(task2 -> {
                                StyleableToast.makeText(this, "Add successfully", R.style.successToast).show();

                                new Handler().postDelayed(() -> {
                                    intent = new Intent(this, ListUserActivity.class);
                                    startActivity(intent);
                                }, 2000);
                            })
                            .addOnFailureListener(e -> {
                                StyleableToast.makeText(this, "Something went wrong...", R.style.errorToast).show();
                            });
                } 
                else
                    StyleableToast.makeText(this, "This email is already exists", R.style.errorToast).show();
            });
    }

    private boolean isPhone(String phoneNumber) {
        String regex = "^0[0-9]{9}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    private boolean isEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}