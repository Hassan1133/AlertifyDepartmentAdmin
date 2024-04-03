package com.example.alertify_department_admin.activities;

import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_DEP_ADMIN_REF;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alertify_department_admin.databinding.ActivityLoginBinding;
import com.example.alertify_department_admin.main_utils.AppSharedPreferences;
import com.example.alertify_department_admin.main_utils.LoadingDialog;
import com.example.alertify_department_admin.models.DepAdminModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private DatabaseReference depAdminRef;

    private DepAdminModel depAdmin;

    private ActivityLoginBinding binding;

    private Dialog loadingDialog;

    private AppSharedPreferences appSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

    }

    private void init() {

        appSharedPreferences = new AppSharedPreferences(LoginActivity.this);

        depAdmin = new DepAdminModel();

        firebaseAuth = FirebaseAuth.getInstance();

        depAdminRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_DEP_ADMIN_REF);

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    loadingDialog = LoadingDialog.showLoadingDialog(LoginActivity.this);
                    emailExistsOrNotForSignIn(binding.email.getText().toString());
                }
            }
        });

    }

    private void signIn(String emailText, String passwordText) {

        firebaseAuth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    getFCMToken();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    LoadingDialog.hideLoadingDialog(loadingDialog);
                    binding.password.setError("wrong password");
                    Toast.makeText(LoginActivity.this, "The Password is wrong", Toast.LENGTH_SHORT).show();
                } else {
                    signUp(binding.email.getText().toString().trim(), binding.password.getText().toString().trim());
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
                            depAdmin.setDepAdminPoliceStation(depAdminModel.getDepAdminPoliceStation());
                            signIn(binding.email.getText().toString().trim(), binding.password.getText().toString().trim());
                            return;
                        } else if (count == snapshot.getChildrenCount()) {
                            LoadingDialog.hideLoadingDialog(loadingDialog);
                            Toast.makeText(LoginActivity.this, "Account doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    LoadingDialog.hideLoadingDialog(loadingDialog);
                    Toast.makeText(LoginActivity.this, "Account doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signUp(String emailText, String passwordText) {
        firebaseAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    setUidToDb(task.getResult().getUser().getUid(), emailText, passwordText);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LoadingDialog.hideLoadingDialog(loadingDialog);
            }
        });
    }

    private void setUidToDb(String uId, String emailText, String passwordText) {

        depAdmin.setDepAdminUid(uId);

        HashMap<String, Object> map = new HashMap<>();

        map.put("depAdminUid", depAdmin.getDepAdminUid());

        depAdminRef.child(depAdmin.getDepAdminId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    signIn(emailText, passwordText);
                }
            }
        });
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    setFCMTokenToDb(task.getResult());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFCMTokenToDb(String token) {
        depAdmin.setDepAdminFCMToken(token);

        HashMap<String, Object> map = new HashMap<>();

        map.put("depAdminFCMToken", depAdmin.getDepAdminFCMToken());

        depAdminRef.child(depAdmin.getDepAdminId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    getProfileData();
                }
            }
        });
    }

    private boolean isValid() {
        boolean valid = true;

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText()).matches()) {
            binding.email.setError("Please enter valid email");
            valid = false;
        }
        if (binding.password.getText().length() < 6) {
            binding.password.setError("Please enter valid password");
            valid = false;
        }

        return valid;
    }

    private void goToMainActivity() {
       appSharedPreferences.put("depAdminLogin", true);

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void getProfileData() {
        if (depAdmin != null) {
            appSharedPreferences.put("depAdminId", depAdmin.getDepAdminId());
            appSharedPreferences.put("depAdminName", depAdmin.getDepAdminName());
            appSharedPreferences.put("depAdminEmail", depAdmin.getDepAdminEmail());
            appSharedPreferences.put("depAdminPoliceStation", depAdmin.getDepAdminPoliceStation());

            LoadingDialog.hideLoadingDialog(loadingDialog);
            Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
            goToMainActivity();
        }
    }
}