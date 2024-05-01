package com.example.alertify_department_admin.activities;

import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_DEP_ADMIN_REF;
import static com.example.alertify_department_admin.constants.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.fragments.AnalyticsFragment;
import com.example.alertify_department_admin.fragments.Complaints_Fragment;
import com.example.alertify_department_admin.fragments.EmergencyRequestsFragment;
import com.example.alertify_department_admin.fragments.Records_Fragment;
import com.example.alertify_department_admin.main_utils.AppSharedPreferences;
import com.example.alertify_department_admin.main_utils.LocationPermissionUtils;
import com.example.alertify_department_admin.models.DepAdminModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawerLayout drawer;
    private NavigationView navigationView;

    private TextView depAdminName, depAdminEmail;

    private String depAdminId;

    private DatabaseReference depAdminRef;

    private BottomNavigationView bottom_navigation;

    private AppSharedPreferences appSharedPreferences;

    private LocationPermissionUtils locationPermissionUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        init(); // initialization method for initializing variables
    }

    private void init() {

        appSharedPreferences = new AppSharedPreferences(MainActivity.this);

        locationPermissionUtils = new LocationPermissionUtils(this);
        locationPermissionUtils.checkAndRequestPermissions();
        locationPermissionUtils.getLocationPermission();


        ImageView toolBarBtn = findViewById(R.id.tool_bar_menu);
        toolBarBtn.setOnClickListener(this);

        drawer = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigation);
        navigationView.setItemIconTintList(null);
        View headerView = navigationView.getHeaderView(0);
        depAdminName = headerView.findViewById(R.id.user_name);
        depAdminEmail = headerView.findViewById(R.id.user_email);
        bottom_navigation = findViewById(R.id.bottom_navigation);
        depAdminRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_DEP_ADMIN_REF);

        setProfileData();
        navigationSelection(); // selection method for navigation items
        bottomNavigationSelection();
        checkDepAdminBlockOrNot();
        keepSharedPreferencesUpToDate();
        loadFragmentOnNotificationOrOnCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed with your action
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show rationale to the user, then request permission using launcher
                showPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Request permission directly using launcher
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }
    }

    private void showPermissionRationale(String permission) {
        // Explain why the app needs permission
        new MaterialAlertDialogBuilder(this).setMessage("This app needs notification permission to...") // Provide reason
                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Launch permission request
                        launcher.launch(permission);
                    }
                }).setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss dialog if user denies permission
                        dialog.dismiss();
                    }
                }).show();
    }

    ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean isGranted) {
            if (!isGranted) {
                // Handle permission denial
                // Consider showing a message or taking appropriate action
            }
        }
    });


    private void loadFragmentOnNotificationOrOnCreate() {
        if (getIntent().hasExtra("notificationFragment")) {
            Toast.makeText(this, "out", Toast.LENGTH_SHORT).show();
            String fragmentName = getIntent().getStringExtra("notificationFragment");
            assert fragmentName != null;
            if (fragmentName.equals("EmergencyRequestsFragment")) {
                bottom_navigation.setSelectedItemId(R.id.emergency);
            }
        } else {
            bottom_navigation.setSelectedItemId(R.id.complaints);
        }
    }

    private void setProfileData() {

        depAdminName.setText(appSharedPreferences.getString("depAdminName"));

        depAdminEmail.setText(appSharedPreferences.getString("depAdminEmail"));

        depAdminId = appSharedPreferences.getString("depAdminId");
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tool_bar_menu) {
            startDrawer(); // start drawer method for open or close navigation drawer
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
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.complaints:
                        loadFragment(new Complaints_Fragment());
                        return true;
                    case R.id.records:
                        loadFragment(new Records_Fragment());
                        return true;
                    case R.id.emergency:
                        loadFragment(new EmergencyRequestsFragment());
                        return true;
                    case R.id.analytics:
                        loadFragment(new AnalyticsFragment());
                        return true;
                }
                return false;
            }
        });
    }

    private void navigationSelection() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.logout:
                        signOut();
                        break;

                    case R.id.profile:
                        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(profileIntent);
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

    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        appSharedPreferences.put("depAdminLogin", false);

        appSharedPreferences.clear();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkDepAdminBlockOrNot() {

        depAdminRef.child(depAdminId).child("depAdminStatus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(String.class).equals("block")) {

                    appSharedPreferences.put("depAdminLogin", false);
                    appSharedPreferences.clear();

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
                    DepAdminModel depAdmin = snapshot.getValue(DepAdminModel.class);


                    if (depAdmin != null) {
                        appSharedPreferences.put("depAdminId", depAdmin.getDepAdminId());
                        appSharedPreferences.put("depAdminName", depAdmin.getDepAdminName());
                        appSharedPreferences.put("depAdminEmail", depAdmin.getDepAdminEmail());
                        appSharedPreferences.put("depAdminPoliceStation", depAdmin.getDepAdminPoliceStation());


                        setProfileData();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (!locationPermissionUtils.locationPermission) {
                    locationPermissionUtils.getLocationPermission();
                }
            }
        }
    }
}