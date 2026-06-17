package com.example.studentevent.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentevent.R;
import com.example.studentevent.model.User;

import java.util.ArrayList;

/**
 * Purpose: Displays users in a RecyclerView for admin management.
 * Input: List of users and action listener.
 * Output: User cards with role and delete actions.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface UserActionListener {
        void onChangeRole(User user);
        void onDeleteUser(User user);
    }

    private Context context;
    private ArrayList<User> users;
    private UserActionListener listener;
    private String adminRole;
    private String studentRole;

    public UserAdapter(Context context, ArrayList<User> users, UserActionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
        this.adminRole = context.getString(R.string.role_admin);
        this.studentRole = context.getString(R.string.role_student);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        holder.txtUserName.setText(context.getString(R.string.lbl_user_name, user.getFullName()));
        holder.txtUserEmail.setText(context.getString(R.string.lbl_user_email, user.getEmail()));
        holder.txtUserRole.setText(context.getString(R.string.lbl_user_role, user.getRole()));

        if (adminRole.equals(user.getRole())) {
            holder.btnChangeRole.setText(R.string.btn_demote_student);
        } else {
            holder.btnChangeRole.setText(R.string.btn_promote_admin);
        }

        holder.btnChangeRole.setOnClickListener(v -> listener.onChangeRole(user));
        holder.btnDeleteUser.setOnClickListener(v -> listener.onDeleteUser(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateList(ArrayList<User> newUsers) {
        users = newUsers;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, txtUserEmail, txtUserRole;
        Button btnChangeRole, btnDeleteUser;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserEmail = itemView.findViewById(R.id.txtUserEmail);
            txtUserRole = itemView.findViewById(R.id.txtUserRole);
            btnChangeRole = itemView.findViewById(R.id.btnChangeRole);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
