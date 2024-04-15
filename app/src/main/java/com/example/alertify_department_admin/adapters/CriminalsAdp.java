package com.example.alertify_department_admin.adapters;

import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_CRIMINALS_REF;

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
import com.example.alertify_department_admin.activities.CriminalDetailActivity;
import com.example.alertify_department_admin.models.CriminalsModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CriminalsAdp extends RecyclerView.Adapter<CriminalsAdp.Holder> {

    private Context context;

    private List<CriminalsModel> criminalsList;

    private final DatabaseReference criminalsRef;

    public CriminalsAdp(Context context, List<CriminalsModel> criminalsList) {
        this.context = context;
        this.criminalsList = criminalsList;
        criminalsRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_CRIMINALS_REF);

    }

    @NonNull
    @Override
    public CriminalsAdp.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.criminals_recycler_design, parent, false);

        return new CriminalsAdp.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CriminalsAdp.Holder holder, int position) {

        CriminalsModel criminalModel = criminalsList.get(position);

        holder.criminalName.setText(criminalModel.getCriminalName());
        holder.criminalCnic.setText(criminalModel.getCriminalCnic());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new MaterialAlertDialogBuilder(context)
                        .setMessage("Are you sure you want to delete criminal")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCriminal(criminalModel); // Perform logout
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
                Intent intent = new Intent(context, CriminalDetailActivity.class);
                intent.putExtra("criminalModel", criminalModel);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCriminal(CriminalsModel crimesModel)
    {
        criminalsRef.child(crimesModel.getCriminalCnic()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(context, "Criminal deleted successfully", Toast.LENGTH_SHORT).show();
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
        return criminalsList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        private TextView criminalName, criminalCnic;
        public Holder(@NonNull View itemView) {
            super(itemView);
            criminalName = itemView.findViewById(R.id.criminalName);
            criminalCnic = itemView.findViewById(R.id.criminalCnic);
        }
    }
}
