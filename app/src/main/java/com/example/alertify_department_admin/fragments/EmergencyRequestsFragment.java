package com.example.alertify_department_admin.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.alertify_department_admin.adapters.EmergencyRequestsAdp;
import com.example.alertify_department_admin.databinding.EmergencyRequestsFragmentBinding;
import com.example.alertify_department_admin.main_utils.LoadingDialog;
import com.example.alertify_department_admin.models.EmergencyServiceModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmergencyRequestsFragment extends Fragment {
    private List<EmergencyServiceModel> emergencyServiceList;
    private EmergencyRequestsAdp adp;
    private DatabaseReference emergencyRef;
    private EmergencyRequestsFragmentBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = EmergencyRequestsFragmentBinding.inflate(inflater, container, false);
        init();
        fetchData();
        return binding.getRoot();
    }

    private void init()
    {
        emergencyRef = FirebaseDatabase.getInstance().getReference("AlertifyEmergencyRequests");

        binding.emergencyRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        emergencyServiceList = new ArrayList<EmergencyServiceModel>();
    }

    private void fetchData() {

        LoadingDialog.showLoadingDialog(getActivity());

        emergencyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                emergencyServiceList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    emergencyServiceList.add(dataSnapshot.getValue(EmergencyServiceModel.class));
                }

                LoadingDialog.hideLoadingDialog();

                setDataToRecycler(emergencyServiceList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataToRecycler(List<EmergencyServiceModel> emergencyServices) {
        adp = new EmergencyRequestsAdp(getActivity(), emergencyServices);
        binding.emergencyRecycler.setAdapter(adp);
    }
}