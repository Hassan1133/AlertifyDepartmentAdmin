package com.example.alertify_department_admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.models.EmergencyServiceModel;

import java.util.List;

public class EmergencyRequestsAdp extends RecyclerView.Adapter<EmergencyRequestsAdp.Holder> {

    private Context context;

    private List<EmergencyServiceModel> emergencyRequestsList;

    public EmergencyRequestsAdp(Context context, List<EmergencyServiceModel> emergencyRequestsList) {
        this.context = context;
        this.emergencyRequestsList = emergencyRequestsList;
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
        holder.userName.setText(emergencyServiceModel.getUserName());
        holder.phoneNo.setText(emergencyServiceModel.getUserPhoneNo());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.activeIcon.setVisibility(View.GONE);
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
