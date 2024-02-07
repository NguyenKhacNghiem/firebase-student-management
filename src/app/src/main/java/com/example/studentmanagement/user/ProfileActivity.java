package com.example.studentmanagement.user;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.studentmanagement.LoginActivity;
import com.example.studentmanagement.R;
import com.example.studentmanagement.databinding.ActivityLoginBinding;
import com.example.studentmanagement.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;

import io.github.muddz.styleabletoast.StyleableToast;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private CollectionReference reference;
    private final int MY_REQUEST_CODE = 1;
    private Intent intent;
    
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
        if(o.getResultCode() == RESULT_OK) {
            Intent i = o.getData();
            
            if(i == null)
                return;
            
            Uri uri = i.getData();
            updateAvatar(uri);
        }
    });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("users");

        getUserInformation();
        
        binding.avatar.setOnClickListener(view -> {
            requestPermission();
        });
        
        binding.btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            
            intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            
            finishAffinity(); // xóa tất cả Activity trước đó khỏi Stack
        });

        // Đổi title của Navigation Bar
        getSupportActionBar().setTitle("Profile");
    }
    
    private void getUserInformation() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if(currentUser == null) 
            return;
        
        String email = currentUser.getEmail();
        Uri photoUri = currentUser.getPhotoUrl();
        
        Query query = reference.whereEqualTo("email", email);
        query.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String name = "";
                    int age = 0;
                    String phone = "";
                    
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        name = document.get("name").toString();
                        age = Integer.parseInt(document.get("age").toString());
                        phone = document.get("phone").toString();
                    }

                    Glide.with(this).load(photoUri).error(R.drawable.default_avatar).into(binding.avatar);
                    binding.tvEmail.setText("Email: " + email);
                    binding.tvName.setText("Name: " + name);
                    binding.tvAge.setText("Age: " + age);
                    binding.tvPhone.setText("Phone: " + phone);
                }
            });
    }
    
    private void updateAvatar(Uri uri) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
            return;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener((OnCompleteListener<Void>) task -> {
                        if (task.isSuccessful()) {
                            StyleableToast.makeText(this, "Upload avatar successfully", R.style.successToast).show();
                            getUserInformation();
                        }
                    });
    }

    private void openGallery() {
        intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        
        activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }
    
    private void requestPermission() {
        // Người dùng đã cấp quyền trước đó
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            openGallery();
        else {
            // Người dùng chưa cấp quyền trước đó
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissions, MY_REQUEST_CODE);
        }
    }
    
    // Xử lý kết quả cấp quyền của người dùng
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if(requestCode == MY_REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) 
                openGallery();
            else 
                StyleableToast.makeText(this, "Please allow the permission", R.style.errorToast).show();
        }
    }
}