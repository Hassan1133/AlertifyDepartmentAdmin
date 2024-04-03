package com.example.alertify_department_admin.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.models.CrimesModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CrimesAdp extends RecyclerView.Adapter<CrimesAdp.Holder> {

    private Context context;

    private List<CrimesModel> crimesList;

    private DatabaseReference crimesRef;

    public CrimesAdp(Context context, List<CrimesModel> crimes) {
        this.context = context;
        crimesList = crimes;
        crimesRef = FirebaseDatabase.getInstance().getReference("AlertifyCrimes");

    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.crimes_recycler_design, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        CrimesModel crimesModel = crimesList.get(position);


        holder.crime.setText(crimesModel.getCrimeType());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new MaterialAlertDialogBuilder(context)
                        .setMessage("Are you sure you want to delete crime")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCrime(crimesModel); // Perform logout
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();


                return false;
            }
        });
    }

    private void deleteCrime(CrimesModel crimesModel)
    {
        crimesRef.child(crimesModel.getCrimeType()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(context, "Crime deleted successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return crimesList.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        private TextView crime;
        public Holder(@NonNull View itemView) {
            super(itemView);
            crime = itemView.findViewById(R.id.crime);
        }
    }
}