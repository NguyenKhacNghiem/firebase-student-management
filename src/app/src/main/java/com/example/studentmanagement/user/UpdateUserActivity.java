package com.example.studentmanagement.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.studentmanagement.R;
import com.example.studentmanagement.databinding.ActivityUpdateUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.muddz.styleabletoast.StyleableToast;

public class UpdateUserActivity extends AppCompatActivity {
    private ActivityUpdateUserBinding binding;
    private FirebaseFirestore firestore;
    private CollectionReference reference;
    private Intent intent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("users");
        
        intent = getIntent();
        String email = intent.getStringExtra("email");
        
        getUserInformation(email);
        
        binding.btnUpdate.setOnClickListener(view -> {
            updateUser(email);
        });
        
        // Hiện back button để trở về ListUserActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Đổi title của Navigation Bar
        getSupportActionBar().setTitle("Update user " + email);
    }

    private void updateUser(String email) {
        String name = binding.etName.getText().toString();
        String age = binding.etAge.getText().toString();
        String phone = binding.etPhone.getText().toString();
        boolean status = binding.sStatus.isChecked();

        if(name.equals("") || age.equals("")) {
            StyleableToast.makeText(this, "Please enter full information", R.style.errorToast).show();
            return;
        }

        if(!isPhone(phone)) {
            StyleableToast.makeText(this, "This is not a valid phone", R.style.errorToast).show();
            return;
        }

        Query query = reference.whereEqualTo("email", email);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Lấy DocumentReference tới đối tượng cần cập nhật
                    DocumentReference documentReference = document.getReference();

                    // Cập nhật dữ liệu mới
                    Map<String, Object> newData = new HashMap<>();
                    newData.put("name", name);  
                    newData.put("age", Integer.parseInt(age)); 
                    newData.put("phone", phone);
                    newData.put("status", status); 
                    
                    documentReference.update(newData)
                            .addOnSuccessListener(aVoid -> {
                                StyleableToast.makeText(this, "Update successfully", R.style.successToast).show();

                                new Handler().postDelayed(() -> {
                                    intent = new Intent(this, ListUserActivity.class);
                                    startActivity(intent);
                                }, 2000);
                            })
                            .addOnFailureListener(e -> {
                                StyleableToast.makeText(this, "Something went wrong...", R.style.errorToast).show();
                            });
                }
            }
        });
    }

    private void getUserInformation(String email) {
        Query query = reference.whereEqualTo("email", email);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String name = "";
                        int age = 0;
                        String phone = "";
                        boolean status = false;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            name = document.get("name").toString();
                            age = Integer.parseInt(document.get("age").toString());
                            phone = document.get("phone").toString();
                            status = Boolean.parseBoolean(document.get("status").toString());
                        }
                        
                        binding.etName.setText(name);
                        binding.etAge.setText(String.valueOf(age));
                        binding.etPhone.setText(phone);
                        binding.sStatus.setChecked(status);
                    }
                });
    }

    private static boolean isPhone(String phoneNumber) {
        String regex = "^0[0-9]{9}$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }
}