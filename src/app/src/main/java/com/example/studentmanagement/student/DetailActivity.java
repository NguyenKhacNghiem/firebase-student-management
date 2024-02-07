package com.example.studentmanagement.student;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studentmanagement.R;
import com.example.studentmanagement.databinding.ActivityDetailBinding;
import com.example.studentmanagement.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private FirebaseFirestore firestore;
    private CollectionReference reference;
    private Intent intent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("students");
        
        intent = getIntent();
        String id = intent.getStringExtra("id");
        
        getStudentInformation(id);

        // Hiện back button để trở về ListStudentActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Đổi title của Navigation Bar
        getSupportActionBar().setTitle("Details of student " + id);
    }

    private void getStudentInformation(String id) {
        Query query = reference.whereEqualTo("id", id);
        query.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String name = "";
                    String phone = "";
                    String gender = "";
                    String major = "";
                    String certificates = "";
                    

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        name = document.get("name").toString();
                        phone = document.get("phone").toString();
                        gender = document.get("gender").toString();
                        major = document.get("major").toString();
                        certificates = document.get("certificates").toString();
                    }
                    
                    binding.tvId.setText("Student ID: " + id);
                    binding.tvName.setText("Name: " + name);
                    binding.tvPhone.setText("Phone: " + phone);
                    binding.tvGender.setText("Gender: " + gender);
                    binding.tvMajor.setText("Major: " + major);

                    LinearLayout llCertificates = findViewById(R.id.llCertificates);

                    // Khi student chưa có certificate nào
                    if(certificates.equals("[]")) {
                        binding.tvCertificates.setText("Certificates: 0");
                        return;
                    }

                    certificates = certificates.substring(1, certificates.length() - 1); // Loại bỏ 2 ký tự [ và ] ở đầu và cuối trong chuỗi
                    ArrayList<String> finalCertificates = new ArrayList<>(Arrays.asList(certificates.split(", ")));

                    binding.tvCertificates.setText("Certificates: " + finalCertificates.size());
                    for(String c : finalCertificates) {
                        TextView textView = new TextView(this);
                        textView.setText("- " + c);
                        textView.setTextSize(20);
                        llCertificates.addView(textView);
                    }
                }
            });
    }
}