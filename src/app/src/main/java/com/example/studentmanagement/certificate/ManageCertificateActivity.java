package com.example.studentmanagement.certificate;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studentmanagement.LoginActivity;
import com.example.studentmanagement.R;
import com.example.studentmanagement.databinding.ActivityManageCertificateBinding;
import com.example.studentmanagement.model.Student;
import com.example.studentmanagement.student.AddStudentActivity;
import com.example.studentmanagement.student.ListStudentActivity;
import com.example.studentmanagement.user.AddUserActivity;
import com.example.studentmanagement.user.ListUserActivity;
import com.example.studentmanagement.user.LoginHistoryActivity;
import com.example.studentmanagement.user.UpdateUserActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.github.muddz.styleabletoast.StyleableToast;

public class ManageCertificateActivity extends AppCompatActivity {
    private ActivityManageCertificateBinding binding;
    private RecyclerView rcvCertificate;
    private CertificateAdapter certificateAdapter;
    private Intent intent;
    private FirebaseFirestore firestore;
    private CollectionReference reference;
    private ArrayList<String> certificates;
    private String id;
    private final int MY_REQUEST_CODE = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageCertificateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("students");

        intent = getIntent();
        id = intent.getStringExtra("id");

        certificateAdapter = new CertificateAdapter(this);
        rcvCertificate = findViewById(R.id.rcvCertificate);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rcvCertificate.setLayoutManager(linearLayoutManager);
        rcvCertificate.setAdapter(certificateAdapter);

        certificates = new ArrayList<>();
        getCertificates();
        
        // Đăng ký sự kiện onClick cho button add
        binding.btnAdd.setOnClickListener(view -> {
            addCertificate();
        });
        
        // Đăng ký sự kiện onClick cho button export
        binding.btnExport.setOnClickListener(view -> {
            requestPermission();
        });

        // Đăng ký context menu cho các item trong RecyclerView
        registerForContextMenu(rcvCertificate);

        // Hiện back button để trở về ListStudentActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Đổi title của Navigation Bar
        getSupportActionBar().setTitle("Manage certificates");
    }
    
    // Update lại các thay đổi lên Firestore
    @Override
    protected void onStop() {
        super.onStop();

        Query query = reference.whereEqualTo("id", id);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Lấy DocumentReference tới đối tượng cần cập nhật
                    DocumentReference documentReference = document.getReference();

                    // Cập nhật dữ liệu mới
                    Map<String, Object> newData = new HashMap<>();
                    newData.put("certificates", certificates);

                    documentReference.update(newData);
                }
            }
        });
    }

    private void getCertificates() {
        Query query = reference.whereEqualTo("id", id);
        query.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String temp = "";
                    
                    for (QueryDocumentSnapshot document : task.getResult())
                        temp = document.get("certificates").toString();
                    
                    // Sinh viên hiện tại không có certificate nào
                    if(temp.equals("[]")) {
                        certificateAdapter.setData(new ArrayList<>());
                        return;
                    }
                    
                    temp = temp.substring(1, temp.length() - 1); // Loại bỏ 2 ký tự [ và ] ở đầu và cuối trong chuỗi
                    certificates = new ArrayList<>(Arrays.asList(temp.split(", ")));

                    certificateAdapter.setData(certificates);
                }
            });
    }

    private void addCertificate() {
        String certificate = binding.etCertificate.getText().toString();
        
        if(certificate.equals("")) {
            StyleableToast.makeText(this, "Please enter full information", R.style.errorToast).show();
            return;
        }

        if(certificates.contains(certificate)) {
            StyleableToast.makeText(this, "This certificate is already exists", R.style.errorToast).show();
            return;
        }
       
        certificates.add(certificate);
        certificateAdapter.setData(certificates); 
        binding.etCertificate.setText(""); // xóa nội dung hiện tại trong Edit Text sau khi add thành công
        StyleableToast.makeText(this, "Add successfully", R.style.successToast).show();
    }

    public void onClickShowUpdateAlert(String oldCertificate) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Update certificate");
        
        EditText etSelectedCertificate = new EditText(ManageCertificateActivity.this);
        etSelectedCertificate.setText(oldCertificate);
        alertDialog.setView(etSelectedCertificate);

        alertDialog.setPositiveButton("YES", (dialog, which) -> {
            String newCertificate = etSelectedCertificate.getText().toString();
            updateCertificate(newCertificate, oldCertificate);
        });

        alertDialog.setNegativeButton("NO", (dialog, which) -> {

        });

        alertDialog.show();
    }

    public void onClickShowDeleteAlert(String certificate) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Confirm delete");
        alertDialog.setMessage("Do you want to delete " + certificate + "?");

        alertDialog.setPositiveButton("YES", (dialog, which) -> {
            deleteCertificate(certificate);
        });

        alertDialog.setNegativeButton("NO", (dialog, which) -> {

        });

        alertDialog.show();
    }

    private void updateCertificate(String newCertificate, String oldCertificate) {
        if(newCertificate.equals("")) {
            StyleableToast.makeText(this, "Please enter full information", R.style.errorToast).show();
            return;
        }

        if(certificates.contains(newCertificate)) {
            StyleableToast.makeText(this, "This certificate is already exists", R.style.errorToast).show();
            return;
        }
        
        for(int i=0; i<certificates.size(); i++)
            if(certificates.get(i).equals(oldCertificate))
                certificates.set(i, newCertificate);
        
        certificateAdapter.setData(certificates);
        StyleableToast.makeText(this, "Update successfully", R.style.successToast).show();
    }
    
    private void deleteCertificate(String certificate) {
        for(int i=0; i<certificates.size(); i++)
            if(certificates.get(i).equals(certificate))
                certificates.remove(i);
        
        certificateAdapter.setData(certificates);
        StyleableToast.makeText(this, "Delete successfully", R.style.successToast).show();
    }

    // Context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context_certificate, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.context_edit) {
            String certificate = certificateAdapter.getSelectedCertificate();
            onClickShowUpdateAlert(certificate);
            
            return true;
        }

        if(item.getItemId() == R.id.context_delete) {
            String certificate = certificateAdapter.getSelectedCertificate();
            onClickShowDeleteAlert(certificate);

            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void exportCertificateList() {
        File directory = new File(getExternalFilesDir(null), "MidtermFolder");

        if (!directory.exists())
            directory.mkdirs();

        File file = new File(directory, "CertificateList-" + id + ".csv");

        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            for (String certificate : certificates) {
                String[] data = {certificate};
                writer.writeNext(data);
            }

            StyleableToast.makeText(this, "Export certificate list successfully", R.style.successToast).show();
            Log.d("SUCCESS", "Exported to path: " + file.getAbsolutePath());
        } catch (IOException e) {
            StyleableToast.makeText(this, "Export certificate list fail", R.style.errorToast).show();
            Log.d("ERROR", e.getMessage());
        }
    }
    
    private void requestPermission() {
        // Người dùng đã cấp quyền trước đó
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            exportCertificateList();
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
                exportCertificateList();
                return;
            }

            StyleableToast.makeText(this, "Please allow the permission", R.style.errorToast).show();
        }
    }
}