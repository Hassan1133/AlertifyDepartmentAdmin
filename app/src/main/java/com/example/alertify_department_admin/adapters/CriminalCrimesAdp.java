package com.example.alertify_department_admin.adapters;

import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_CRIMES_REF;

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
import com.example.alertify_department_admin.models.CriminalCrimesModel;

import java.util.List;

public class CriminalCrimesAdp extends RecyclerView.Adapter<CriminalCrimesAdp.Holder> {

    private final Context context;

    private final List<CriminalCrimesModel> criminalCrimesModelList;


    public CriminalCrimesAdp(Context context, List<CriminalCrimesModel> criminalCrimesModelList) {
        this.context = context;
        this.criminalCrimesModelList = criminalCrimesModelList;

    }

    @NonNull
    @Override
    public CriminalCrimesAdp.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fir_recycler_design, parent, false);

        return new CriminalCrimesAdp.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CriminalCrimesAdp.Holder holder, int position) {

        CriminalCrimesModel criminalCrimesModel = criminalCrimesModelList.get(position);


        holder.district.setText(criminalCrimesModel.getDistrict());
        holder.policeStation.setText(criminalCrimesModel.getPoliceStation());
        holder.firNumber.setText(criminalCrimesModel.getFIRNumber());


    }
    @Override
    public int getItemCount() {
        return criminalCrimesModelList.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        private TextView district, policeStation, firNumber;
        public Holder(@NonNull View itemView) {
            super(itemView);
            district = itemView.findViewById(R.id.district);
            policeStation = itemView.findViewById(R.id.policeStation);
            firNumber = itemView.findViewById(R.id.firNumber);
        }
    }
}
