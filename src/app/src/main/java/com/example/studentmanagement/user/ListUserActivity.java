package com.example.studentmanagement.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.studentmanagement.LoginActivity;
import com.example.studentmanagement.R;
import com.example.studentmanagement.databinding.ActivityAddUserBinding;
import com.example.studentmanagement.databinding.ActivityListUserBinding;
import com.example.studentmanagement.model.User;
import com.example.studentmanagement.student.AddStudentActivity;
import com.example.studentmanagement.student.ListStudentActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.muddz.styleabletoast.StyleableToast;

public class ListUserActivity extends AppCompatActivity {
    private ActivityListUserBinding binding;
    private RecyclerView rcvUser;
    private UserAdapter userAdapter;
    private Intent intent;
    private FirebaseFirestore firestore;
    private CollectionReference reference;
    private FirebaseAuth mAuth;
    private String email;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("users");
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        email = prefs.getString("email", null);

        userAdapter = new UserAdapter(this);
        rcvUser = findViewById(R.id.rcvUser);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rcvUser.setLayoutManager(linearLayoutManager);
        rcvUser.setAdapter(userAdapter);
        
        getUsers();

        // Đăng ký context menu cho các item trong RecyclerView
        registerForContextMenu(rcvUser);

        // Đổi title của Navigation Bar
        getSupportActionBar().setTitle("User list");
    }

    private void getUsers() {
        reference.get()
                .addOnCompleteListener(task -> {
                    List<User> users = new ArrayList<>();
                    
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String email = document.get("email").toString();
                            
                            if(email.equals("admin@gmail.com"))
                                continue;
                            
                            String password = document.get("password").toString();
                            String name = document.get("name").toString();
                            int age = Integer.parseInt(document.get("age").toString());
                            String phone = document.get("phone").toString();
                            boolean status = Boolean.parseBoolean(document.get("status").toString());
                            
                            users.add(new User(email, password, name, age, phone, status, new ArrayList<>()));
                        }
                    } 
                    else {
                        StyleableToast.makeText(this, "Error white fetching data...", R.style.errorToast).show();
                    }

                    userAdapter.setData(users);
                });
    }
    
    private void deleteUser(String email) {
        Query query = reference.whereEqualTo("email", email);
        query.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                for (QueryDocumentSnapshot document : task1.getResult()) {
                    // Lấy DocumentReference tới đối tượng cần xóa
                    DocumentReference documentReference = document.getReference();
                    
                    // Xóa user
                    String id = documentReference.getId();
                    reference.document(id)
                            .delete()
                            .addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful())
                                    getUsers();
                                else 
                                    StyleableToast.makeText(this, "Something went wrong...", R.style.errorToast).show();
                            });
                }
            }
        });
    }

    public void onClickShowAlert(String email) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Confirm delete");
        alertDialog.setMessage("Do you want to delete the user whose email is " + email + "?");

        alertDialog.setPositiveButton("YES", (dialog, which) -> {
            deleteUser(email);
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
        inflater.inflate(R.menu.menu_context_user, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.context_login_history) {
            String email = userAdapter.getSelectedEmail();

            intent = new Intent(this, LoginHistoryActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);

            return true;
        }
        
        if(item.getItemId() == R.id.context_edit) {
            String email = userAdapter.getSelectedEmail();
            
            intent = new Intent(this, UpdateUserActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            
            return true;
        }

        if(item.getItemId() == R.id.context_delete) {
            String email = userAdapter.getSelectedEmail();
            onClickShowAlert(email);
            
            return true;
        }

        return super.onContextItemSelected(item);
    }
}