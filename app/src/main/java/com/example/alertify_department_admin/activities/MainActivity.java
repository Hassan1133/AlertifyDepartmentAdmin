package com.example.alertify_department_admin.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.model.DepAdminModel;
import com.example.alertify_department_admin.fragments.Complaints_Fragment;
import com.example.alertify_department_admin.fragments.Records_Fragment;
import com.example.alertify_department_admin.fragments.Users_Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private View headerView;
    private ImageView toolBarBtn;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private CircleImageView depAdminImage;
    private TextView depAdminName, depAdminEmail;

    private String depAdminId;

    private DatabaseReference depAdminRef;

    private BottomNavigationView bottom_navigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        init(); // initialization method for initializing variables
        navigationSelection(); // selection method for navigation items
        bottomNavigationSelection();
        checkDepAdminBlockOrNot();
        keepSharedPreferencesUpToDate();
        loadFragment(new Complaints_Fragment());
    }

    private void init() {
        toolBarBtn = findViewById(R.id.tool_bar_menu);
        toolBarBtn.setOnClickListener(this);

        drawer = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigation);
        navigationView.setItemIconTintList(null);
        headerView = navigationView.getHeaderView(0);
        depAdminImage = headerView.findViewById(R.id.circle_img);
        depAdminName = headerView.findViewById(R.id.user_name);
        depAdminEmail = headerView.findViewById(R.id.user_email);

        bottom_navigation = findViewById(R.id.bottom_navigation);

        setProfileData();

        depAdminRef = FirebaseDatabase.getInstance().getReference("AlertifyDepAdmin");
    }

    private void setProfileData() {
        SharedPreferences depAdminData = getSharedPreferences("depAdminProfileData", Context.MODE_PRIVATE);

        depAdminName.setText(depAdminData.getString("depAdminName", ""));

        depAdminEmail.setText(depAdminData.getString("depAdminEmail", ""));

        Glide.with(getApplicationContext()).load(depAdminData.getString("depAdminImageUrl", "")).into(depAdminImage);

        depAdminId = depAdminData.getString("depAdminId", "");
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tool_bar_menu:
                startDrawer(); // start drawer method for open or close navigation drawer
                break;
        }
    }

    private void startDrawer() {
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void bottomNavigationSelection() {

        bottom_navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.complaints:
                        loadFragment(new Complaints_Fragment());
                        return true;
                    case R.id.users:
                        loadFragment(new Users_Fragment());
                        return true;
                    case R.id.records:
//                        if (isMapsEnabled()) {
//                            getLocationPermission();
                        loadFragment(new Records_Fragment());
//                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void navigationSelection() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.logout:

                        SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
                        SharedPreferences.Editor loginEditor = pref.edit();
                        loginEditor.putBoolean("flag", false);
                        loginEditor.apply();

                        SharedPreferences depAdminData = getSharedPreferences("depAdminProfileData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor profileEditor = depAdminData.edit();
                        profileEditor.clear();

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        break;

                    case R.id.profile:
                        intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        drawer.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.home:
                        loadFragment(new Complaints_Fragment());
                        bottom_navigation.setSelectedItemId(R.id.complaints);
                        drawer.closeDrawer(GravityCompat.START);
                        break;

                }
                return false;
            }
        });
    }

    private void checkDepAdminBlockOrNot() {

        depAdminRef.child(depAdminId).child("depAdminStatus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(String.class).equals("block")) {
                    SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("flag", false);
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void keepSharedPreferencesUpToDate() {
        depAdminRef.child(depAdminId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DepAdminModel depAdminModel = snapshot.getValue(DepAdminModel.class);

                    SharedPreferences depAdminData = getSharedPreferences("depAdminProfileData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = depAdminData.edit();

                    if (depAdminModel != null) {
                        editor.putString("depAdminId", depAdminModel.getDepAdminId());
                        editor.putString("depAdminName", depAdminModel.getDepAdminName());
                        editor.putString("depAdminEmail", depAdminModel.getDepAdminEmail());
                        editor.putString("depAdminImageUrl", depAdminModel.getDepAdminImageUrl());
                        editor.putString("depAdminPoliceStation", depAdminModel.getDepAdminPoliceStation());
                        editor.apply();

                        setProfileData();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}