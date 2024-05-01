package com.example.alertify_department_admin.activities;

import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_CRIMES_REF;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.adapters.CrimesAdp;
import com.example.alertify_department_admin.databinding.ActivityCrimesBinding;
import com.example.alertify_department_admin.models.CrimesModel;
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

public class CrimesActivity extends AppCompatActivity {

    private ActivityCrimesBinding binding;

    private Dialog crimesDialog;

    private TextInputEditText crimeType, crimeDefinition;

    private DatabaseReference crimesRef;

    private ProgressBar crimesDialogProgressbar;

    private List<CrimesModel> crimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrimesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {

        binding.addCrimesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAddCrimeDialog();
            }
        });

        crimesRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_CRIMES_REF);

        binding.crimesRecycler.setLayoutManager(new LinearLayoutManager(CrimesActivity.this));

        crimes = new ArrayList<CrimesModel>();

        fetchData();
        binding.crimesSearchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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
        ArrayList<CrimesModel> searchList = new ArrayList<>();
        for (CrimesModel i : crimes) {
            if (i.getCrimeType().toLowerCase().contains(newText.toLowerCase())) {
                searchList.add(i);
            }
        }
        setDataToRecycler(searchList);
    }

    private void createAddCrimeDialog() {
        crimesDialog = new Dialog(this);
        crimesDialog.setContentView(R.layout.add_crime_dialog);
        crimesDialog.setCancelable(false);
        crimesDialog.show();
        crimesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        crimeType = crimesDialog.findViewById(R.id.crime_type);
        crimeDefinition = crimesDialog.findViewById(R.id.definition);
        crimesDialogProgressbar = crimesDialog.findViewById(R.id.crimes_dialog_progressbar);

        crimesDialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crimesDialog.dismiss();
            }
        });

        crimesDialog.findViewById(R.id.add_crime_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValid()) {
                    crimesDialogProgressbar.setVisibility(View.VISIBLE);

                    CrimesModel crimesModel = new CrimesModel();
                    crimesModel.setCrimeType(crimeType.getText().toString().trim());
                    crimesModel.setDefinition(crimeDefinition.getText().toString().trim());

                    checkCrimeAlreadyExistOrNot(crimesModel);

                }

            }
        });
    }

    private void checkCrimeAlreadyExistOrNot(CrimesModel crimesModel) {
        crimesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            int count = 0;
            boolean check = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot crimesSnapshot : snapshot.getChildren()) {

                        CrimesModel crime = crimesSnapshot.getValue(CrimesModel.class);

                        count++;

                        assert crime != null;
                        if (crime.getCrimeType().equalsIgnoreCase(crimesModel.getCrimeType())) {
                            crimesDialogProgressbar.setVisibility(View.INVISIBLE);
                            Toast.makeText(CrimesActivity.this, "Crime type already exists. Please enter a different one", Toast.LENGTH_SHORT).show();
                            crimeType.setError("Crime type exists. Please enter a different one");
                            check = true;
                            return;
                        } else if (count == snapshot.getChildrenCount()) {
                            if (!check) {
                                addToDb(crimesModel);
                            }
                        }
                    }
                } else {
                    addToDb(crimesModel);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CrimesActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchData() {

        binding.crimesProgressbar.setVisibility(View.VISIBLE);

        crimesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                crimes.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    crimes.add(dataSnapshot.getValue(CrimesModel.class));
                }

                binding.crimesProgressbar.setVisibility(View.GONE);

                setDataToRecycler(crimes);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CrimesActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addToDb(CrimesModel crimesModel) {
        crimesRef.child(crimesModel.getCrimeType()).setValue(crimesModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CrimesActivity.this, "Crime added successfully", Toast.LENGTH_SHORT).show();
                    crimesDialogProgressbar.setVisibility(View.INVISIBLE);
                    crimesDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CrimesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                crimesDialogProgressbar.setVisibility(View.INVISIBLE);
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

    private void setDataToRecycler(List<CrimesModel> crimes) {
        CrimesAdp adp = new CrimesAdp(CrimesActivity.this, crimes);
        binding.crimesRecycler.setAdapter(adp);
    }
}