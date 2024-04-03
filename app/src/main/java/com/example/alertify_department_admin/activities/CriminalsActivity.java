package com.example.alertify_department_admin.activities;

import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_CRIMES_REF;
import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_CRIMINALS_REF;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.databinding.ActivityCriminalsBinding;
import com.example.alertify_department_admin.models.CrimesModel;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class CriminalsActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityCriminalsBinding binding;

    private Dialog criminalsDialog;

    private ProgressBar criminalDialogProgressbar;

    private TextInputEditText criminalCnic, criminalName;

    private DatabaseReference criminalsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCriminalsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        binding.addBtn.setOnClickListener(this);
        criminalsRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_CRIMINALS_REF);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addBtn) {
            createAddCriminalDialog();
        }
    }

    private void createAddCriminalDialog() {
        criminalsDialog = new Dialog(this);
        criminalsDialog.setContentView(R.layout.add_criminal_dialog);
        criminalsDialog.setCancelable(false);
        criminalsDialog.show();
        criminalsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        criminalDialogProgressbar = criminalsDialog.findViewById(R.id.dialogProgressbar);
        criminalCnic = criminalsDialog.findViewById(R.id.criminalCnic);
        criminalName = criminalsDialog.findViewById(R.id.criminalName);

        criminalsDialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                criminalsDialog.dismiss();
            }
        });

        criminalsDialog.findViewById(R.id.addCriminalBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValid()) {
                    criminalDialogProgressbar.setVisibility(View.VISIBLE);

                    List<CriminalCrimesModel> criminalCrimesList = new ArrayList<>();

                    CriminalsModel criminalsModel = new CriminalsModel();
                    criminalsModel.setCriminalCnic(criminalCnic.getText().toString());
                    criminalsModel.setCriminalName(criminalName.getText().toString());
                    criminalsModel.setCriminalCrimesList(criminalCrimesList);
                    checkCriminalAlreadyExistOrNot(criminalsModel);

                }

            }
        });
    }

    private void checkCriminalAlreadyExistOrNot(CriminalsModel criminalsModel) {
        criminalsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            int count = 0;
            boolean check = false;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot criminalSnapshot : snapshot.getChildren()) {

                        CriminalsModel criminal = criminalSnapshot.getValue(CriminalsModel.class);

                        count++;

                        assert criminal != null;
                        if (criminal.getCriminalCnic().equalsIgnoreCase(criminalsModel.getCriminalCnic())) {
                            criminalDialogProgressbar.setVisibility(View.INVISIBLE);
                            Toast.makeText(CriminalsActivity.this, "Criminal already exists. Please enter a different one", Toast.LENGTH_SHORT).show();
                            criminalCnic.setError("Criminal exists. Please enter a different one");
                            check = true;
                            return;
                        } else if (count == snapshot.getChildrenCount()) {
                            if (!check) {
                                addToDb(criminalsModel);
                            }
                        }
                    }
                } else {
                    addToDb(criminalsModel);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CriminalsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void addToDb(CriminalsModel criminalsModel) {
        criminalsRef.child(criminalsModel.getCriminalCnic()).setValue(criminalsModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CriminalsActivity.this, "Criminal added successfully", Toast.LENGTH_SHORT).show();
                    criminalDialogProgressbar.setVisibility(View.INVISIBLE);
                    criminalsDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CriminalsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                criminalDialogProgressbar.setVisibility(View.INVISIBLE);
                criminalsDialog.dismiss();
            }
        });
    }

    private boolean isValid() {
        boolean valid = true;

        if (criminalCnic.getText().length() < 13) {
            criminalCnic.setError("Please enter valid CNIC");
            valid = false;
        }
        if (criminalName.getText().length() < 3) {
            criminalCnic.setError("Please enter valid name");
            valid = false;
        }
        return valid;
    }
}