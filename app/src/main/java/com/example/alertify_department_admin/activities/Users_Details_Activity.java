package com.example.alertify_department_admin.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.models.Users_Model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Users_Details_Activity extends AppCompatActivity {

    private CircleImageView userImage;

    private TextView userName, userEmail, userCnicNo, userPhoneNo;

    private Users_Model usersModel;

    private ImageView statusBtn;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_details);
        initialize();
        getUserDetails();
    }

    private void initialize() {
        userImage = findViewById(R.id.user_details_image);
        userName = findViewById(R.id.user_details_name);
        userEmail = findViewById(R.id.user_details_email);
        userCnicNo = findViewById(R.id.user_details_cnic);
        userPhoneNo = findViewById(R.id.user_details_phone);

        userRef = FirebaseDatabase.getInstance().getReference("AlertifyUser");

        statusBtn = findViewById(R.id.status_icon);
        statusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserStatus(usersModel);
            }
        });
    }

    private void updateUserStatus(Users_Model usersModel) {
        if (usersModel.getUserStatus().equals("unblock")) {

            usersModel.setUserStatus("block");

            HashMap<String, Object> map = new HashMap<>();

            map.put("userStatus", usersModel.getUserStatus());

            userRef.child(usersModel.getId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        statusBtn.setImageResource(R.drawable.lock);
                        Toast.makeText(Users_Details_Activity.this, "User Blocked Successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Users_Details_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if (usersModel.getUserStatus().equals("block")) {

            usersModel.setUserStatus("unblock");

            HashMap<String, Object> map = new HashMap<>();

            map.put("userStatus", usersModel.getUserStatus());

            userRef.child(usersModel.getId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        statusBtn.setImageResource(R.drawable.unlock);
                        Toast.makeText(Users_Details_Activity.this, "User UnBlocked Successfully!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Users_Details_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getUserDetails()
    {
        usersModel = (Users_Model) getIntent().getSerializableExtra("Users_Model");
        Glide.with(getApplicationContext()).load(usersModel.getImgUrl()).into(userImage);
        userName.setText(usersModel.getName());
        userEmail.setText(usersModel.getEmail());
        userCnicNo.setText(usersModel.getCnicNo());
        userPhoneNo.setText(usersModel.getPhoneNo());
        if(usersModel.getUserStatus().equals("unblock"))
        {
            statusBtn.setImageResource(R.drawable.unlock);
        }
        else if (usersModel.getUserStatus().equals("block"))
        {
            statusBtn.setImageResource(R.drawable.lock);
        }
    }
}