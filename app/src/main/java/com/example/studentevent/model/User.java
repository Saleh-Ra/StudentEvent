package com.example.studentevent.model;

/**
 * Purpose: Represents a User in the system.
 * Input: Data fields for user details.
 * Output: User object containing details like uid, name, email, and role.
 */
public class User {
    private String uid;
    private String fullName;
    private String email;
    private String role; // "Student" or "Admin"

    public User() {
        // Required for Firestore
    }

    public User(String uid, String fullName, String email, String role) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}