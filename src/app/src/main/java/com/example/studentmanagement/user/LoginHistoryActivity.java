package com.example.studentmanagement.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.studentmanagement.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.content.Intent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import android.widget.LinearLayout.LayoutParams;

public class LoginHistoryActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private CollectionReference reference;
    private Intent intent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_history);

        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("users");
        
        intent = getIntent();
        String email = intent.getStringExtra("email");
        getLoginHistory(email);
        
        // Hiện back button để trở về ListUserActivity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Đổi title của Navigation Bar
        getSupportActionBar().setTitle("Login history of " + email);
    }

    private void getLoginHistory(String email) {
        Query query = reference.whereEqualTo("email", email);
        query.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String loginHistory = "";

                    for (QueryDocumentSnapshot document : task.getResult())
                        loginHistory = document.get("loginHistory").toString();

                    LinearLayout llLoginHistory = findViewById(R.id.llLoginHistory);
                    
                    // Khi user chưa login lần nào
                    if(loginHistory.equals("[]")) {
                        TextView textView = new TextView(this);
                        textView.setText("This user has not logged in yet.");
                        llLoginHistory.addView(textView);
                        return;
                    }
                    
                    loginHistory = loginHistory.substring(1, loginHistory.length() - 1); // Loại bỏ 2 ký tự [ và ] ở đầu và cuối trong chuỗi
                    ArrayList<String> finalLoginHistory = new ArrayList<>(Arrays.asList(loginHistory.split(", ")));
                    
                    for(String history : finalLoginHistory) {
                        TextView textView = new TextView(this);
                        textView.setText(history);
                        llLoginHistory.addView(textView);
                    }
                }
            });
    }
}