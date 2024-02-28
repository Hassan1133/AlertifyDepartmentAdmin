package com.example.alertify_department_admin.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.model.DepAdminModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn;

    private TextInputEditText email, password;

    private FirebaseAuth firebaseAuth;

    private DatabaseReference depAdminRef;

    private DepAdminModel depAdmin;

    private ProgressBar loadingProgressBar;

    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

    }

    private void init() {
        loginBtn = findViewById(R.id.login_btn);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);


        depAdmin = new DepAdminModel();

        firebaseAuth = FirebaseAuth.getInstance();

        depAdminRef = FirebaseDatabase.getInstance().getReference("AlertifyDepAdmin");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    createLoadingDialog();
                    emailExistsOrNotForSignIn(email.getText().toString());
                }
            }
        });

    }

    private void createLoadingDialog() {
        loadingDialog = new Dialog(LoginActivity.this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.show();
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView loadingTxt = loadingDialog.findViewById(R.id.loading);
        loadingTxt.setText("Signing in....");

        loadingProgressBar = loadingDialog.findViewById(R.id.profile_progressbar);

        loadingProgressBar.setVisibility(View.VISIBLE);

        loadingDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    dialog.dismiss();
                    finish();
                }
                return true;
            }
        });
    }

    private void signIn(String emailText, String passwordText) {

        firebaseAuth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    getProfileData();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    loadingDialog.dismiss();
                    password.setError("wrong password");
                    Toast.makeText(LoginActivity.this, "The Password is wrong", Toast.LENGTH_SHORT).show();
                } else {
                    signUp(email.getText().toString().trim(), password.getText().toString().trim());
                }
            }
        });
    }

    private void emailExistsOrNotForSignIn(String emailText) {
        depAdminRef.addListenerForSingleValueEvent(new ValueEventListener() {

            int count = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot depAdminSnapShot : snapshot.getChildren()) {
                        DepAdminModel depAdminModel = depAdminSnapShot.getValue(DepAdminModel.class);

                        count++;

                        if (depAdminModel.getDepAdminEmail().equals(emailText) && depAdminModel.getDepAdminStatus().equals("unblock")) {
                            depAdmin.setDepAdminId(depAdminModel.getDepAdminId());
                            depAdmin.setDepAdminEmail(depAdminModel.getDepAdminEmail());
                            depAdmin.setDepAdminName(depAdminModel.getDepAdminName());
                            depAdmin.setDepAdminImageUrl(depAdminModel.getDepAdminImageUrl());
                            depAdmin.setDepAdminPoliceStation(depAdminModel.getDepAdminPoliceStation());
                            signIn(email.getText().toString().trim(), password.getText().toString().trim());
                            return;
                        } else if (count == snapshot.getChildrenCount()) {
                            loadingDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Account doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Account doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void signUp(String emailText, String passwordText) {
        firebaseAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    setUidToDb(firebaseAuth.getUid(), emailText, passwordText);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialog.dismiss();
            }
        });
    }

    private void setUidToDb(String uId, String emailText, String passwordText) {

        depAdmin.setuId(uId);

        HashMap<String, Object> map = new HashMap<>();

        map.put("uid", depAdmin.getuId());

        depAdminRef.child(depAdmin.getDepAdminId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    signIn(emailText, passwordText);
                }
            }
        });
    }

    private boolean isValid() {
        boolean valid = true;

        if (!Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches()) {
            email.setError("Please enter valid email");
            valid = false;
        }
        if (password.getText().length() < 6) {
            password.setError("Please enter valid password");
            valid = false;
        }

        return valid;
    }

    private void goToMainActivity() {
        SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("flag", true);
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void getProfileData() {
        SharedPreferences depAdminData = getSharedPreferences("depAdminProfileData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = depAdminData.edit();
        if(depAdmin != null)
        {
            editor.putString("depAdminId", depAdmin.getDepAdminId());
            editor.putString("depAdminName", depAdmin.getDepAdminName());
            editor.putString("depAdminEmail", depAdmin.getDepAdminEmail());
            editor.putString("depAdminImageUrl", depAdmin.getDepAdminImageUrl());
            editor.putString("depAdminPoliceStation", depAdmin.getDepAdminPoliceStation());
            editor.apply();

            loadingDialog.dismiss();
            Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
            goToMainActivity();
        }
    }
}