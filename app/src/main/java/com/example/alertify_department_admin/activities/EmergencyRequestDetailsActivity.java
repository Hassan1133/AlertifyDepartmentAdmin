package com.example.alertify_department_admin.activities;

import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_EMERGENCY_REQUESTS_REF;
import static com.example.alertify_department_admin.constants.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.example.alertify_department_admin.constants.Constants.USERS_REF;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.alertify_department_admin.databinding.ActivityEmergencyRequestDetailsBinding;
import com.example.alertify_department_admin.main_utils.LoadingDialog;
import com.example.alertify_department_admin.main_utils.LocationPermissionUtils;
import com.example.alertify_department_admin.models.EmergencyRequestModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EmergencyRequestDetailsActivity extends AppCompatActivity {

    private ActivityEmergencyRequestDetailsBinding binding;
    private DatabaseReference emergencyRequestsRef, usersRef;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double userLatitude, userLongitude, depAdminCurrentLatitude, depAdminCurrentLongitude;
    private LocationPermissionUtils locationPermissionUtils;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyRequestDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        locationPermissionUtils = new LocationPermissionUtils(this);
        locationPermissionUtils.checkAndRequestPermissions();
        locationPermissionUtils.getLocationPermission();
        emergencyRequestsRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_EMERGENCY_REQUESTS_REF);
        usersRef = FirebaseDatabase.getInstance().getReference(USERS_REF);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(EmergencyRequestDetailsActivity.this);
        getDataFromIntent();
        binding.directionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastLocation();
            }
        });
    }

    private void getDataFromIntent() {

        EmergencyRequestModel emergencyRequestModel = (EmergencyRequestModel) getIntent().getSerializableExtra("emergencyRequestModel");
        assert emergencyRequestModel != null;

        loadingDialog = LoadingDialog.showLoadingDialog(EmergencyRequestDetailsActivity.this);

        getUserData(emergencyRequestModel.getUserId());

        if (emergencyRequestModel.getRequestStatus().equals("unseen")) {
            updateRequestStatus(emergencyRequestModel);
        }

        binding.requestDateTime.setText(emergencyRequestModel.getRequestDateTime());
        binding.requestPoliceStation.setText(emergencyRequestModel.getPoliceStation());
        userLatitude = emergencyRequestModel.getUserCurrentLatitude();
        userLongitude = emergencyRequestModel.getUserCurrentLongitude();
    }

    private void getUserData(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setUserData(snapshot.child("name").getValue().toString(),
                        snapshot.child("email").getValue().toString(),
                        snapshot.child("cnicNo").getValue().toString(),
                        snapshot.child("phoneNo").getValue().toString()
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                LoadingDialog.hideLoadingDialog(loadingDialog);
                Toast.makeText(EmergencyRequestDetailsActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserData(String name, String email, String cnicNo, String phoneNo) {
        binding.userName.setText(name);
        binding.userPhoneNo.setText(phoneNo);
        binding.userCnicNo.setText(cnicNo);
        binding.userEmail.setText(email);
        LoadingDialog.hideLoadingDialog(loadingDialog);
    }

    private void updateRequestStatus(EmergencyRequestModel emergencyServiceModel) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("requestStatus", "seen");

        emergencyRequestsRef.child(emergencyServiceModel.getRequestId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EmergencyRequestDetailsActivity.this, "seen", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EmergencyRequestDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(EmergencyRequestDetailsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(EmergencyRequestDetailsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    depAdminCurrentLatitude = location.getLatitude();
                    depAdminCurrentLongitude = location.getLongitude();
                    if (depAdminCurrentLatitude != 0 && depAdminCurrentLongitude != 0 && userLatitude != 0 && userLongitude != 0) {
                        openGoogleMapsForDirections(depAdminCurrentLatitude, depAdminCurrentLongitude, userLatitude, userLongitude);
                    } else {
                        Toast.makeText(EmergencyRequestDetailsActivity.this, "Please select your location again", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EmergencyRequestDetailsActivity.this, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openGoogleMapsForDirections(double startLat, double startLon, double destLat, double destLon) {
        //Uri with the destination coordinates
        // http://maps.google.com/maps?saddr=37.7749,-122.4194&daddr=34.0522,-118.2437
        Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?saddr=" + startLat + "," + startLon + "&daddr=" + destLat + "," + destLon);

        //Intent with the action to view and set the data to the Uri
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        //package to ensure only the Google Maps app is used
        mapIntent.setPackage("com.google.android.apps.maps");

        // Check if there is an app available to handle the Intent before starting it
        if (mapIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(EmergencyRequestDetailsActivity.this, "Google Maps not installed in this device", Toast.LENGTH_SHORT).show();
        }
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