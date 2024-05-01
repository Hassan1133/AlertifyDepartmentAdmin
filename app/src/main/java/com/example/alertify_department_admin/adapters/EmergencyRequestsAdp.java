package com.example.alertify_department_admin.adapters;

import static com.example.alertify_department_admin.constants.Constants.USERS_REF;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.activities.EmergencyRequestDetailsActivity;
import com.example.alertify_department_admin.models.EmergencyRequestModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class EmergencyRequestsAdp extends RecyclerView.Adapter<EmergencyRequestsAdp.Holder> {

    private final Context context;
    private final List<EmergencyRequestModel> emergencyRequestsList;
    private final DatabaseReference usersRef;

    public EmergencyRequestsAdp(Context context, List<EmergencyRequestModel> emergencyRequestsList) {
        this.context = context;
        this.emergencyRequestsList = emergencyRequestsList;
        usersRef = FirebaseDatabase.getInstance().getReference(USERS_REF);
    }

    @NonNull
    @Override
    public EmergencyRequestsAdp.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.emergency_request_recycler_design, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyRequestsAdp.Holder holder, int position) {

        EmergencyRequestModel emergencyRequestModel = emergencyRequestsList.get(position);
        usersRef.child(emergencyRequestModel.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.userName.setText(snapshot.child("name").getValue().toString());
                holder.phoneNo.setText(snapshot.child("phoneNo").getValue().toString());
                if (emergencyRequestModel.getRequestStatus().equals("unseen")) {
                    holder.activeIcon.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EmergencyRequestDetailsActivity.class);
                intent.putExtra("emergencyRequestModel", emergencyRequestModel);
                context.startActivity(intent);
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
