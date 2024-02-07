package com.example.studentmanagement.student;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;

import com.example.studentmanagement.R;
import com.example.studentmanagement.databinding.ActivityUpdateStudentBinding;
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

public class UpdateStudentActivity extends AppCompatActivity {
    private ActivityUpdateStudentBinding binding;
    private FirebaseFirestore firestore;
    private CollectionReference reference;
    private Intent intent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set dữ liệu cho các Spinner
        createSpinner();

        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("students");

        intent = getIntent();
        String id = intent.getStringExtra("id");

        getStudentInformation(id);

        binding.btnUpdate.setOnClickListener(view -> {
            updateStudent(id);
        });

        // Hiện back button để trở về ListUserActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Đổi title của Navigation Bar
        getSupportActionBar().setTitle("Update student " + id);
    }

    private void updateStudent(String id) {
        String name = binding.etName.getText().toString();
        String phone = binding.etPhone.getText().toString();
        String gender = binding.spGender.getSelectedItem().toString();
        String major = binding.spMajor.getSelectedItem().toString();

        if(name.equals("") || gender.equals("Gender") || major.equals("Major")) {
            StyleableToast.makeText(this, "Please enter full information", R.style.errorToast).show();
            return;
        }

        if(!isPhone(phone)) {
            StyleableToast.makeText(this, "This is not a valid phone", R.style.errorToast).show();
            return;
        }

        Query query = reference.whereEqualTo("id", id);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Lấy DocumentReference tới đối tượng cần cập nhật
                    DocumentReference documentReference = document.getReference();

                    // Cập nhật dữ liệu mới
                    Map<String, Object> newData = new HashMap<>();
                    newData.put("name", name);
                    newData.put("phone", phone);
                    newData.put("gender", gender);
                    newData.put("major", major);

                    documentReference.update(newData)
                            .addOnSuccessListener(aVoid -> {
                                StyleableToast.makeText(this, "Update successfully", R.style.successToast).show();

                                new Handler().postDelayed(() -> {
                                    intent = new Intent(this, ListStudentActivity.class);
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

    private void getStudentInformation(String id) {
        Query query = reference.whereEqualTo("id", id);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String name = "";
                        String phone = "";
                        String gender = "";
                        String major = "";

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            name = document.get("name").toString();
                            phone = document.get("phone").toString();
                            gender = document.get("gender").toString();
                            major = document.get("major").toString();
                        }

                        binding.etName.setText(name);
                        binding.etPhone.setText(phone);
                        
                        // Hiển thị gender hiện tại trong spinner gender
                        for(int i=0; i<3;i++)
                            if(binding.spGender.getItemAtPosition(i).toString().equals(gender))
                                binding.spGender.setSelection(i);

                        // Hiển thị major hiện tại trong spinner major
                        for(int i=0; i<5;i++)
                            if(binding.spMajor.getItemAtPosition(i).toString().equals(major))
                                binding.spMajor.setSelection(i);
                    }
                });
    }

    private static boolean isPhone(String phoneNumber) {
        String regex = "^0[0-9]{9}$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    private void createSpinner() {
        ArrayAdapter<CharSequence> adapter;

        // Spinner gender
        adapter = ArrayAdapter.createFromResource(this, R.array.genders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spGender.setAdapter(adapter);

        // Spinner major
        adapter = ArrayAdapter.createFromResource(this, R.array.majors, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spMajor.setAdapter(adapter);
    }
}