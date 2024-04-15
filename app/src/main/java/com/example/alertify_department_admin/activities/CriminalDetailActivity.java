package com.example.alertify_department_admin.activities;

import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_CRIMINALS_REF;
import static com.example.alertify_department_admin.constants.Constants.FIR_REF;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.adapters.CriminalCrimesAdp;
import com.example.alertify_department_admin.adapters.CriminalsAdp;
import com.example.alertify_department_admin.databinding.ActivityCriminalDetailBinding;
import com.example.alertify_department_admin.models.CriminalCrimesModel;
import com.example.alertify_department_admin.models.CriminalsModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CriminalDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityCriminalDetailBinding binding;

    private CriminalsModel criminalsModel;

    private Dialog addFirDialog;

    private TextInputEditText districtField, policeStationField, firNumberField;

    private DatabaseReference criminalsRef;

    private ProgressBar progressBar;

    private List<CriminalCrimesModel> criminalCrimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCriminalDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        getDataFromIntent();
        binding.addFirBtn.setOnClickListener(this);
        criminalsRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_CRIMINALS_REF);
        criminalCrimes = new ArrayList<>();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(CriminalDetailActivity.this));
        fetchFIR();
    }

    private void fetchFIR() {
        criminalsRef.child(criminalsModel.getCriminalCnic()).child(FIR_REF).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    criminalCrimes.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        criminalCrimes.add(dataSnapshot.getValue(CriminalCrimesModel.class));
                    }

                    setDataToRecycler(criminalCrimes);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CriminalDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataToRecycler(List<CriminalCrimesModel> criminalCrimes) {
        CriminalCrimesAdp adp = new CriminalCrimesAdp(CriminalDetailActivity.this, criminalCrimes);
        binding.recyclerView.setAdapter(adp);
    }

    private void getDataFromIntent() {
        criminalsModel = (CriminalsModel) getIntent().getSerializableExtra("criminalModel");
        assert criminalsModel != null;
        binding.criminalName.setText(criminalsModel.getCriminalName());
        binding.criminalCnic.setText(criminalsModel.getCriminalCnic());

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addFirBtn) {
            createFirDialog();
        }
    }

    private void createFirDialog() {
        addFirDialog = new Dialog(CriminalDetailActivity.this);
        addFirDialog.setContentView(R.layout.criminal_fir_dialog_design);
        addFirDialog.show();
        addFirDialog.setCancelable(false);
        addFirDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        districtField = addFirDialog.findViewById(R.id.district);
        policeStationField = addFirDialog.findViewById(R.id.policeStation);
        firNumberField = addFirDialog.findViewById(R.id.firNumber);
        progressBar = addFirDialog.findViewById(R.id.fir_dialog_progressbar);

        addFirDialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFirDialog.dismiss();
            }
        });

        addFirDialog.findViewById(R.id.addFirDetailBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    progressBar.setVisibility(View.VISIBLE);
                    addFirDataToModel(districtField.getText().toString(), policeStationField.getText().toString(), firNumberField.getText().toString());
                }
            }
        });

    }

    private void addFirDataToModel(String district, String policeStation, String firNumber) {
        CriminalCrimesModel crimesModel = new CriminalCrimesModel();
        crimesModel.setDistrict(district);
        crimesModel.setPoliceStation(policeStation);
        crimesModel.setFIRNumber(firNumber);

        addDataToDb(crimesModel);
    }

    private void addDataToDb(CriminalCrimesModel criminalCrimesModel) {

        String id = criminalsRef.push().getKey();

        criminalCrimesModel.setId(id);

        assert id != null;
        criminalsRef.child(criminalsModel.getCriminalCnic()).child(FIR_REF).child(id).setValue(criminalCrimesModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CriminalDetailActivity.this, "Data Added", Toast.LENGTH_SHORT).show();
                    addFirDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CriminalDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValid() {
        boolean valid = true;

        if (districtField.getText().length() < 3) {
            districtField.setError("enter valid district");
            valid = false;
        }
        if (policeStationField.getText().length() < 3) {
            policeStationField.setError("enter valid police station");
            valid = false;
        }
        if (firNumberField.getText().length() < 1) {
            policeStationField.setError("enter valid number");
            valid = false;
        }
        return valid;
    }
}