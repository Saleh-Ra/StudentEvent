package com.example.studentevent.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentevent.R;
import com.example.studentevent.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Purpose: Admin screen for managing users in Firestore.
 * Input: Users from Firestore users collection.
 * Output: View, promote/demote roles, and delete users.
 */
public class UserManagementActivity extends AppCompatActivity implements UserAdapter.UserActionListener {

    private RecyclerView recyclerUsers;
    private UserAdapter userAdapter;
    private ArrayList<User> usersList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (!prefs.contains("user_email") || FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        if (!getString(R.string.role_admin).equals(prefs.getString("user_role", getString(R.string.role_student)))) {
            Toast.makeText(this, R.string.access_denied_admin, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_user_management);
        firestore = FirebaseFirestore.getInstance();

        recyclerUsers = findViewById(R.id.recyclerUsers);
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadUsers();
    }

    private void loadUsers() {
        firestore.collection("users").get()
                .addOnSuccessListener(snapshot -> {
                    usersList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            if (user.getUid() == null) {
                                user.setUid(doc.getId());
                            }
                            usersList.add(user);
                        }
                    }
                    userAdapter = new UserAdapter(this, usersList, this);
                    recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
                    recyclerUsers.setAdapter(userAdapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_firestore, e.getMessage()), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onChangeRole(User user) {
        boolean isAdmin = getString(R.string.role_admin).equals(user.getRole());
        int message = isAdmin ? R.string.confirm_demote_student : R.string.confirm_promote_admin;

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                    String newRole = isAdmin ? getString(R.string.role_student) : getString(R.string.role_admin);
                    firestore.collection("users").document(user.getUid())
                            .update("role", newRole)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, R.string.success_role_updated, Toast.LENGTH_SHORT).show();
                                loadUsers();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, getString(R.string.error_firestore, e.getMessage()), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    @Override
    public void onDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete_user)
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                    firestore.collection("users").document(user.getUid())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, R.string.success_user_deleted, Toast.LENGTH_SHORT).show();
                                loadUsers();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, getString(R.string.error_firestore, e.getMessage()), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }
}
