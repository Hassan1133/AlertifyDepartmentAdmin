package com.example.alertify_department_admin.adapters;

import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_LAWS_REF;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.activities.LawsDetailsActivity;
import com.example.alertify_department_admin.models.LawsModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class LawsAdp extends RecyclerView.Adapter<LawsAdp.Holder> {

    private final Context context;

    private final List<LawsModel> crimesList;

    private final DatabaseReference lawsRef;

    public LawsAdp(Context context, List<LawsModel> crimes) {
        this.context = context;
        crimesList = crimes;
        lawsRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_LAWS_REF);

    }

    @NonNull
    @Override
    public LawsAdp.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.laws_recyler_design, parent, false);

        return new LawsAdp.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LawsAdp.Holder holder, int position) {

        LawsModel lawsModel = crimesList.get(position);

        holder.crime.setText(lawsModel.getCrimeType());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new MaterialAlertDialogBuilder(context)
                        .setMessage("Are you sure you want to delete crime")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCrimeLaw(lawsModel); // Perform logout
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LawsDetailsActivity.class);
                intent.putExtra("lawsModel", lawsModel);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCrimeLaw(LawsModel lawsModel) {
        lawsRef.child(lawsModel.getCrimeType()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(context, "Law Crime deleted successfully", Toast.LENGTH_SHORT).show();
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
