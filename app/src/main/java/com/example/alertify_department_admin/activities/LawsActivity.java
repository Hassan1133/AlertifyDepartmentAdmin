package com.example.alertify_department_admin.activities;

import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_LAWS_REF;

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
import com.example.alertify_department_admin.adapters.LawsAdp;
import com.example.alertify_department_admin.databinding.ActivityLawsBinding;
import com.example.alertify_department_admin.models.LawsModel;
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
import java.util.List;

public class LawsActivity extends AppCompatActivity {
    private ActivityLawsBinding binding;

    private Dialog lawsDialog;

    private TextInputEditText crimeType;

    private DatabaseReference lawsRef;

    private ProgressBar lawsDialogProgressbar;

    private List<LawsModel> lawCrimes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLawsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        binding.addLawsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAddCrimeDialog();
            }
        });

        lawsRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_LAWS_REF);

        binding.crimesRecycler.setLayoutManager(new LinearLayoutManager(LawsActivity.this));

        lawCrimes = new ArrayList<LawsModel>();

        fetchData();
        binding.lawsSearchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });
    }

    private void search(String newText) {
        ArrayList<LawsModel> searchList = new ArrayList<>();
        for (LawsModel i : lawCrimes) {
            if (i.getCrimeType().toLowerCase().contains(newText.toLowerCase())) {
                searchList.add(i);
            }
        }
        setDataToRecycler(searchList);
    }

    private void createAddCrimeDialog() {
        lawsDialog = new Dialog(this);
        lawsDialog.setContentView(R.layout.add_crime_dialog);
        lawsDialog.setCancelable(false);
        lawsDialog.show();
        lawsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        crimeType = lawsDialog.findViewById(R.id.crime_type);
        lawsDialogProgressbar = lawsDialog.findViewById(R.id.crimes_dialog_progressbar);

        lawsDialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lawsDialog.dismiss();
            }
        });

        lawsDialog.findViewById(R.id.add_crime_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValid()) {
                    lawsDialogProgressbar.setVisibility(View.VISIBLE);

                    LawsModel lawsModel = new LawsModel();
                    lawsModel.setCrimeType(crimeType.getText().toString().trim());

                    checkCrimeAlreadyExistOrNot(lawsModel);

                }

            }
        });
    }

    private void checkCrimeAlreadyExistOrNot(LawsModel lawsModel) {
        lawsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            int count = 0;
            boolean check = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot crimesSnapshot : snapshot.getChildren()) {

                        LawsModel lawsModel = crimesSnapshot.getValue(LawsModel.class);

                        count++;

                        assert lawsModel != null;
                        if (lawsModel.getCrimeType().equalsIgnoreCase(lawsModel.getCrimeType())) {
                            lawsDialogProgressbar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LawsActivity.this, "Crime type already exists. Please enter a different one", Toast.LENGTH_SHORT).show();
                            crimeType.setError("Crime type exists. Please enter a different one");
                            check = true;
                            return;
                        } else if (count == snapshot.getChildrenCount()) {
                            if (!check) {
                                addToDb(lawsModel);
                            }
                        }
                    }
                } else {
                    addToDb(lawsModel);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LawsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchData() {

        binding.lawsProgressbar.setVisibility(View.VISIBLE);
        lawsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                lawCrimes.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    lawCrimes.add(dataSnapshot.getValue(LawsModel.class));
                }

                binding.lawsProgressbar.setVisibility(View.GONE);

                setDataToRecycler(lawCrimes);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LawsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToDb(LawsModel lawsModel) {
        lawsRef.child(lawsModel.getCrimeType()).setValue(lawsModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LawsActivity.this, "Crime added successfully", Toast.LENGTH_SHORT).show();
                    lawsDialogProgressbar.setVisibility(View.INVISIBLE);
                    lawsDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LawsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                lawsDialogProgressbar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private boolean isValid() {
        boolean valid = true;

        if (crimeType.getText().length() < 3) {
            crimeType.setError("Please enter valid Crime");
            valid = false;
        }
        return valid;
    }

    private void setDataToRecycler(List<LawsModel> lawCrimes) {
        LawsAdp adp = new LawsAdp(LawsActivity.this, lawCrimes);
        binding.crimesRecycler.setAdapter(adp);
    }
}