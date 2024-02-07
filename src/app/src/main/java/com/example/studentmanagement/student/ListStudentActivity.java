package com.example.studentmanagement.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.studentmanagement.LoginActivity;
import com.example.studentmanagement.R;
import com.example.studentmanagement.certificate.ManageCertificateActivity;
import com.example.studentmanagement.databinding.ActivityListStudentBinding;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.user.AddUserActivity;
import com.example.studentmanagement.user.ListUserActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import io.github.muddz.styleabletoast.StyleableToast;

public class ListStudentActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private ActivityListStudentBinding binding;
    private RecyclerView rcvStudent;
    private StudentAdapter studentAdapter;
    private Intent intent;
    private FirebaseFirestore firestore;
    private CollectionReference reference;
    private FirebaseAuth mAuth;
    private ArrayList<Student> students;
    private final int MY_REQUEST_CODE = 1;
    private String email;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set dữ liệu cho Spinner sorting
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sorting, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spSorting.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("students");
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        email = prefs.getString("email", null);

        studentAdapter = new StudentAdapter(this);
        rcvStudent = findViewById(R.id.rcvStudent);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rcvStudent.setLayoutManager(linearLayoutManager);
        rcvStudent.setAdapter(studentAdapter);
        
        students = new ArrayList<>();
        getStudents();
        
        // Set sự kiện OnItemSelectedListener cho spinner sorting
        binding.spSorting.setOnItemSelectedListener(this);

        // Set sự kiện TextChangedListener cho EditText searching
        binding.etSearching.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String key = charSequence.toString();
                searching(key.toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Đăng ký context menu cho các item trong RecyclerView
        registerForContextMenu(rcvStudent);
        
        // Đổi title của Navigation Bar
        getSupportActionBar().setTitle("Student list");
    }

    private void getStudents() {
        reference.get()
                .addOnCompleteListener(task -> {
                    students = new ArrayList<>();

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.get("id").toString();
                            String name = document.get("name").toString();
                            String phone = document.get("phone").toString();
                            String gender = document.get("gender").toString();
                            String major = document.get("major").toString();

                            students.add(new Student(id, name, phone, gender, major, new ArrayList<>()));
                        }
                    }
                    else 
                        StyleableToast.makeText(this, "Error white fetching data...", R.style.errorToast).show();

                    studentAdapter.setData(students);
                });
    }

    private void deleteStudent(String id) {
        Query query = reference.whereEqualTo("id", id);
        query.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                for (QueryDocumentSnapshot document : task1.getResult()) {
                    // Lấy DocumentReference tới đối tượng cần xóa
                    DocumentReference documentReference = document.getReference();

                    // Xóa student
                    String documentReferenceId = documentReference.getId();
                    reference.document(documentReferenceId)
                            .delete()
                            .addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful())
                                    getStudents();
                                else
                                    StyleableToast.makeText(this, "Something went wrong...", R.style.errorToast).show();
                            });
                }
            }
        });
    }
    
    // Chức năng sorting
    private void sorting(int position) {
        switch (position) {
            case 0: // Default
                getStudents();
                break;
            case 1: // Name A-Z
                Collections.sort(students, Comparator.comparing(Student::getName));
                studentAdapter.setData(students);
                break;
            case 2: // Name Z-A
                Collections.sort(students, Comparator.comparing(Student::getName, Comparator.reverseOrder()));
                studentAdapter.setData(students);
                break;
            case 3: // Male first
                Collections.sort(students, Comparator.comparing(Student::getGender, Comparator.reverseOrder()));
                studentAdapter.setData(students);
                break;
            case 4: // Female first
                Collections.sort(students, Comparator.comparing(Student::getGender));
                studentAdapter.setData(students);
                break;
            default:
                StyleableToast.makeText(this, "This sorting type is invalid", R.style.errorToast).show();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        sorting(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    
    // Chức năng searching
    private void searching(String key) {
        ArrayList<Student> result = (ArrayList<Student>) students.stream()
                                                                .filter(s -> s.getId().toLowerCase().contains(key) 
                                                                        || s.getName().toLowerCase().contains(key) 
                                                                        || s.getPhone().toLowerCase().contains(key) 
                                                                        || s.getGender().toLowerCase().contains(key) 
                                                                        || s.getMajor().toLowerCase().contains(key))
                                                                .collect(Collectors.toList());
        studentAdapter.setData(result);
    }
    
    public void onClickShowAlert(String id) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Confirm delete");
        alertDialog.setMessage("Do you want to delete the student whose id is " + id + "?");

        alertDialog.setPositiveButton("YES", (dialog, which) -> {
            deleteStudent(id);
        });

        alertDialog.setNegativeButton("NO", (dialog, which) -> {

        });

        alertDialog.show();
    }

    // Option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.option_user) {
            if(!email.equals("admin@gmail.com"))
                StyleableToast.makeText(this, "You don't have the permission!", R.style.errorToast).show();
            else {
                intent = new Intent(this, ListUserActivity.class);
                startActivity(intent);
            }
            
            return true;
        }

        if(item.getItemId() == R.id.option_student) {
            intent = new Intent(this, ListStudentActivity.class);
            startActivity(intent);
            return true;
        }

        if(item.getItemId() == R.id.option_add_user) {
            if(!email.equals("admin@gmail.com"))
                StyleableToast.makeText(this, "You don't have the permission!", R.style.errorToast).show();
            else {
                intent = new Intent(this, AddUserActivity.class);
                startActivity(intent);
            }
        }

        if(item.getItemId() == R.id.option_add_student) {
            intent = new Intent(this, AddStudentActivity.class);
            startActivity(intent);
            return true;
        }

        if(item.getItemId() == R.id.option_export_student) {
            requestPermission();
            return true;
        }

        if(item.getItemId() == R.id.option_logout) {
            SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
            editor.remove("email");
            editor.apply();
            
            mAuth.signOut();

            intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

            finishAffinity(); // xóa tất cả Activity trước đó khỏi Stack

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context_student, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.context_detail) {
            String id = studentAdapter.getSelectedId();

            intent = new Intent(this, DetailActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);

            return true;
        }
        
        if(item.getItemId() == R.id.context_manage_certificates) {
            String id = studentAdapter.getSelectedId();

            intent = new Intent(this, ManageCertificateActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);

            return true;
        }

        if(item.getItemId() == R.id.context_edit) {
            String id = studentAdapter.getSelectedId();

            intent = new Intent(this, UpdateStudentActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);

            return true;
        }

        if(item.getItemId() == R.id.context_delete) {
            String id = studentAdapter.getSelectedId();
            onClickShowAlert(id);

            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void exportStudentList() {
        File directory = new File(getExternalFilesDir(null), "MidtermFolder");

        if (!directory.exists())
            directory.mkdirs();

        File file = new File(directory, "StudentList.csv");

        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            String[] header = {"ID", "Name", "Phone", "Gender", "Major"};
            writer.writeNext(header);

            for (Student student : students) {
                String[] data = {student.getId(), student.getName(), student.getPhone(), student.getGender(), student.getMajor()};
                writer.writeNext(data);
            }

            StyleableToast.makeText(this, "Export student list successfully", R.style.successToast).show();
            Log.d("SUCCESS", "Exported to path: " + file.getAbsolutePath());
        } catch (IOException e) {

            StyleableToast.makeText(this, "Export student list fail", R.style.errorToast).show();
            Log.d("ERROR", e.getMessage());
        }
    }
    
    private void requestPermission() {
        // Người dùng đã cấp quyền trước đó
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            exportStudentList();
            return;
        }
        
        // Người dùng chưa cấp quyền trước đó
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        requestPermissions(permissions, MY_REQUEST_CODE);
    }

    // Xử lý kết quả cấp quyền của người dùng
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportStudentList();
                return;
            }
            
            StyleableToast.makeText(this, "Please allow the permission", R.style.errorToast).show();
        }
    }
}