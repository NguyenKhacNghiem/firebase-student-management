package com.example.studentmanagement.student;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.studentmanagement.R;
import com.example.studentmanagement.databinding.ActivityAddStudentBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.muddz.styleabletoast.StyleableToast;

public class AddStudentActivity extends AppCompatActivity {
    private ActivityAddStudentBinding binding;
    private FirebaseFirestore firestore;
    private CollectionReference reference;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Set dữ liệu cho các Spinner
        createSpinner();

        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("students");

        binding.btnAdd.setOnClickListener(view -> {
            addStudent();
        });

        // Hiện back button để trở về ListStudentActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Đổi title của Navigation Bar
        getSupportActionBar().setTitle("Add new student");
    }

    private void addStudent() {
        String id = binding.etId.getText().toString();
        String name = binding.etName.getText().toString();
        String phone = binding.etPhone.getText().toString();
        String gender = binding.spGender.getSelectedItem().toString();
        String major = binding.spMajor.getSelectedItem().toString();

        if(id.equals("") || name.equals("") || gender.equals("Gender") || major.equals("Major")) {
            StyleableToast.makeText(this, "Please enter full information", R.style.errorToast).show();
            return;
        }

        if(!isPhone(phone)) {
            StyleableToast.makeText(this, "This is not a valid phone", R.style.errorToast).show();
            return;
        }

        Map<String, Object> student = new HashMap<>();
        student.put("id", id);
        student.put("name", name);
        student.put("phone", phone);
        student.put("gender", gender);
        student.put("major", major);
        student.put("certificates", new ArrayList<String>());

        Query query = reference.whereEqualTo("id", id);
        query.get()
            .addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    if (task1.getResult().size() > 0) {
                        StyleableToast.makeText(this, "This student is already exists", R.style.errorToast).show();
                        return;
                    }

                    reference.add(student)
                            .addOnCompleteListener(task2 -> {
                                StyleableToast.makeText(this, "Add successfully", R.style.successToast).show();

                                new Handler().postDelayed(() -> {
                                    intent = new Intent(this, ListStudentActivity.class);
                                    startActivity(intent);
                                }, 2000);
                            })
                            .addOnFailureListener(e -> {
                                StyleableToast.makeText(this, "Something went wrong...", R.style.errorToast).show();
                            });
                }
                else
                    StyleableToast.makeText(this, "Something went wrong...", R.style.errorToast).show();
            });
    }

    private boolean isPhone(String phoneNumber) {
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