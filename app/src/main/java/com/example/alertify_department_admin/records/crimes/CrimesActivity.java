package com.example.alertify_department_admin.records.crimes;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.adapters.CrimesAdp;
import com.example.alertify_department_admin.model.CrimesModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CrimesActivity extends AppCompatActivity {

    private FloatingActionButton addCrimesBtn;

    private Dialog crimesDialog;

    private EditText crimeType;

    private DatabaseReference crimesRef;

    private CrimesModel crimesModel;

    private ProgressBar crimesDialogProgressbar;

    private List<CrimesModel> crimes;

    private CrimesAdp adp;

    private RecyclerView recyclerView;

    private SearchView searchView;

    private ProgressBar crimesActivityProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crimes);
        init();
        fetchData();
    }

    private void init() {
        addCrimesBtn = findViewById(R.id.add_crimes_btn);
        addCrimesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAddCrimeDialog();
            }
        });

        crimesRef = FirebaseDatabase.getInstance().getReference("AlertifyCrimes");

        recyclerView = findViewById(R.id.crimes_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(CrimesActivity.this));

        crimes = new ArrayList<CrimesModel>();

        crimesActivityProgressBar = findViewById(R.id.crimes_progressbar);
    }

    private void createAddCrimeDialog() {
        crimesDialog = new Dialog(this);
        crimesDialog.setContentView(R.layout.add_crime_dialog);
        crimesDialog.setCancelable(false);
        crimesDialog.show();
        crimesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        crimeType = crimesDialog.findViewById(R.id.crime_type);
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

                    crimesModel = new CrimesModel();
                    crimesModel.setCrimeType(crimeType.getText().toString().trim());

                    addToDb(crimesModel);

                }

            }
        });
    }

    private void fetchData() {

        crimesActivityProgressBar.setVisibility(View.VISIBLE);

        crimesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                crimes.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    crimes.add(dataSnapshot.getValue(CrimesModel.class));
                }

                crimesActivityProgressBar.setVisibility(View.INVISIBLE);

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
            crimeType.setError("Please enter valid title");
            valid = false;
        }
        return valid;
    }

    private void setDataToRecycler(List<CrimesModel> crimes) {
        adp = new CrimesAdp(CrimesActivity.this, crimes);
        recyclerView.setAdapter(adp);
    }
}