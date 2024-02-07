package com.example.studentmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bumptech.glide.Glide;
import com.example.studentmanagement.databinding.ActivityLoginBinding;
import com.example.studentmanagement.student.ListStudentActivity;
import com.example.studentmanagement.user.AddUserActivity;
import com.example.studentmanagement.user.ListUserActivity;
import com.example.studentmanagement.user.ProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import io.github.muddz.styleabletoast.StyleableToast;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private Intent intent;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private CollectionReference reference;
    DocumentReference documentReference;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("users");
        
        // Login
        binding.btnLogin.setOnClickListener(view -> {
            if(!isConnectedToInternet()) {
                StyleableToast.makeText(this, "Please connect to the internet", R.style.errorToast).show();
                return;
            }
                
            login();
        });

        // Hiện mật khẩu khi click Checkbox
        binding.cbShowPassword.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if(!isChecked)
                binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance()); // ẩn
            else
                binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); // hiện
        });

        // Đổi title của Navigation Bar
        getSupportActionBar().setTitle("Login");
    }
    
    private void login() {
        String email = binding.etEmail.getText().toString();
        String password = binding.etPassword.getText().toString();

        if(!isEmail(email)) {
            StyleableToast.makeText(this, "This is not a valid email", R.style.errorToast).show();
            return;
        }

        Query query = reference.whereEqualTo("email", email);
        query.get()
            .addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    // Kiểm tra xem tài khoản user có bị xóa không
                    if(task1.getResult().size() <= 0) {
                        StyleableToast.makeText(this, "Your account has been deleted", R.style.errorToast).show();
                        return;
                    }
                    
                    // Kiểm tra xem tài khoản user có bị khóa không
                    boolean status = false;
                    String loginHistory = "";
                    
                    for (QueryDocumentSnapshot document : task1.getResult()) {
                        status = Boolean.parseBoolean(document.get("status").toString());
                        
                        // Phục vụ cho chức năng lưu lại lịch sử đăng nhập phía dưới
                        documentReference = document.getReference();
                        loginHistory = document.get("loginHistory").toString();
                    }
                    
                    if(!status) {
                        StyleableToast.makeText(this, "Your account has been locked", R.style.errorToast).show();
                        return;
                    }

                    ArrayList<String> finalLoginHistory;
                    if(loginHistory.equals("[]")) {
                        // loginHistory rỗng
                        finalLoginHistory = new ArrayList<>();
                    }
                    else {
                        loginHistory = loginHistory.substring(1, loginHistory.length() - 1); // Loại bỏ 2 ký tự [ và ] ở đầu và cuối trong chuỗi
                        finalLoginHistory = new ArrayList<>(Arrays.asList(loginHistory.split(", ")));
                    }
                    
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, task2 -> {
                                if (task2.isSuccessful()) {
                                    // Lưu lại lịch sử đăng nhập
                                    Date currentDate = new Date();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                    String formattedDate = dateFormat.format(currentDate);
                                    finalLoginHistory.add(formattedDate); // thêm lịch sử mới
                                    
                                    Map<String, Object> newData = new HashMap<>();
                                    newData.put("loginHistory", finalLoginHistory);

                                    documentReference.update(newData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        if(email.equals("admin@gmail.com")) // admin role
                                                            intent = new Intent(LoginActivity.this, ListUserActivity.class);
                                                        else if(email.indexOf("@manager.com") != -1) // manager role
                                                            intent = new Intent(LoginActivity.this, ListStudentActivity.class);
                                                        else  // employee role
                                                            intent = new Intent(LoginActivity.this, ProfileActivity.class);

                                                        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                                                        editor.putString("email", email);
                                                        editor.apply();

                                                        startActivity(intent);
                                                    });
                                }
                                else
                                    StyleableToast.makeText(this, "Your email or password is not correct", R.style.errorToast).show();
                            });
                }
            });
    }

    private boolean isEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

                return capabilities != null
                        && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            } 
            else {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        }

        return false;
    }
}