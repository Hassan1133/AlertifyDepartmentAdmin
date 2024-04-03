package com.example.alertify_department_admin.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.databinding.ActivityProfileBinding;
import com.example.alertify_department_admin.main_utils.AppSharedPreferences;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private AppSharedPreferences appSharedPreferences;

    private Dialog editPasswordDialog;

    private TextInputEditText depAdminCurrentPassword, depAdminNewPassword;

    private ProgressBar passwordDialogProgressBar;

    private FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {

        appSharedPreferences = new AppSharedPreferences(ProfileActivity.this);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        binding.passwordEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPasswordDialog();
            }
        });

        getProfileData();
    }

    private void getProfileData() {
        binding.depAdminName.setText(appSharedPreferences.getString("depAdminName"));

        binding.depAdminEmail.setText(appSharedPreferences.getString("depAdminEmail"));

        binding.depAdminPoliceStation.setText(appSharedPreferences.getString("depAdminPoliceStation"));
    }

    private void createPasswordDialog() {
        editPasswordDialog = new Dialog(ProfileActivity.this);
        editPasswordDialog.setContentView(R.layout.dep_admin_edit_password_dialog);
        editPasswordDialog.show();
        editPasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        depAdminCurrentPassword = editPasswordDialog.findViewById(R.id.user_current_password);
        depAdminNewPassword = editPasswordDialog.findViewById(R.id.user_new_password);
        passwordDialogProgressBar = editPasswordDialog.findViewById(R.id.dep_admin_password_progressbar);

        editPasswordDialog.findViewById(R.id.dep_admin_close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPasswordDialog.dismiss();
            }
        });

        editPasswordDialog.findViewById(R.id.dep_admin_update_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidPassword()) {
                    passwordDialogProgressBar.setVisibility(View.VISIBLE);
                    verifyHighAuthorityCurrentPassword(firebaseUser.getEmail(), depAdminCurrentPassword.getText().toString().trim());
                }
            }
        });
    }

    private void verifyHighAuthorityCurrentPassword(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateUserPassword(depAdminNewPassword.getText().toString().trim());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        passwordDialogProgressBar.setVisibility(View.INVISIBLE);
                        depAdminCurrentPassword.setError("password is invalid");
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserPassword(String newPassword) {
        firebaseUser.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                            passwordDialogProgressBar.setVisibility(View.INVISIBLE);
                            editPasswordDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        passwordDialogProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidPassword() {
        boolean valid = true;

        if (depAdminCurrentPassword.getText().length() < 6) {
            depAdminCurrentPassword.setError("enter valid password");
            valid = false;
        }

        if (depAdminNewPassword.getText().length() < 6) {
            depAdminNewPassword.setError("enter valid password");
            valid = false;
        }

        return valid;
    }

}