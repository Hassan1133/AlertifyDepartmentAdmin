package com.example.alertify_department_admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.models.EmergencyServiceModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class EmergencyRequestsAdp extends RecyclerView.Adapter<EmergencyRequestsAdp.Holder> {

    private Context context;

    private List<EmergencyServiceModel> emergencyRequestsList;

    private DatabaseReference emergencyRequestsRef;

    public EmergencyRequestsAdp(Context context, List<EmergencyServiceModel> emergencyRequestsList) {
        this.context = context;
        this.emergencyRequestsList = emergencyRequestsList;
        emergencyRequestsRef = FirebaseDatabase.getInstance().getReference("AlertifyEmergencyRequests");
    }

    @NonNull
    @Override
    public EmergencyRequestsAdp.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.emergency_request_recycler_design, parent, false);
        EmergencyRequestsAdp.Holder holder = new EmergencyRequestsAdp.Holder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyRequestsAdp.Holder holder, int position) {

        EmergencyServiceModel emergencyServiceModel = emergencyRequestsList.get(position);
        if (emergencyServiceModel.getRequestStatus().toString().equals("unseen")) {
            holder.activeIcon.setVisibility(View.VISIBLE);
        }
        holder.userName.setText(emergencyServiceModel.getUserName());
        holder.phoneNo.setText(emergencyServiceModel.getUserPhoneNo());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emergencyServiceModel.getRequestStatus().toString().equals("unseen")) {
                    updateRequestStatus("seen", emergencyServiceModel);
                }
            }
        });

    }

    private void updateRequestStatus(String status, EmergencyServiceModel emergencyServiceModel) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("requestStatus", status);

        emergencyRequestsRef.child(emergencyServiceModel.getRequestId()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "seen", Toast.LENGTH_SHORT).show();
                }
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
        return emergencyRequestsList.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        private TextView userName, phoneNo;
        private ImageView activeIcon;

        public Holder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            phoneNo = itemView.findViewById(R.id.phoneNo);
            activeIcon = itemView.findViewById(R.id.active);
        }
    }
}
