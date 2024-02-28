package com.example.alertify_department_admin.activities;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.alertify_department_admin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView depAdminName, depAdminEmail, depAdminPoliceStation;
    private CircleImageView depAdminImage;

    private Dialog editPasswordDialog;

    private TextInputEditText depAdminCurrentPassword, depAdminNewPassword;

    private ProgressBar passwordDialogProgressBar;

    private FirebaseUser firebaseUser;

    private ImageView passwordEditBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();
    }

    private void init() {
        depAdminName = findViewById(R.id.dep_admin_name);
        depAdminEmail = findViewById(R.id.dep_admin_email);
        depAdminPoliceStation = findViewById(R.id.dep_admin_police_station);
        depAdminImage = findViewById(R.id.dep_admin_image);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        passwordEditBtn = findViewById(R.id.password_edit_btn);
        passwordEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPasswordDialog();
            }
        });

        getProfileData();
    }

    private void getProfileData() {
        SharedPreferences depAdminData = getSharedPreferences("depAdminProfileData", MODE_PRIVATE);
        depAdminName.setText(depAdminData.getString("depAdminName", ""));

        depAdminEmail.setText(depAdminData.getString("depAdminEmail", ""));

        depAdminPoliceStation.setText(depAdminData.getString("depAdminPoliceStation", ""));

        Glide.with(getApplicationContext()).load(depAdminData.getString("depAdminImageUrl", "")).into(depAdminImage);
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