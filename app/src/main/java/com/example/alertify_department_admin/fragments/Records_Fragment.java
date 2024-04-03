package com.example.alertify_department_admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.activities.CrimesActivity;
import com.example.alertify_department_admin.activities.CriminalsActivity;
import com.example.alertify_department_admin.activities.LawsActivity;
import com.example.alertify_department_admin.databinding.RecordsFragmentBinding;

public class Records_Fragment extends Fragment implements View.OnClickListener {

    private Intent intent;

    private RecordsFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = RecordsFragmentBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {
        binding.crimes.setOnClickListener(this);
        binding.criminals.setOnClickListener(this);
        binding.laws.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.crimes) {
            intent = new Intent(getActivity(), CrimesActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.criminals) {
            intent = new Intent(getActivity(), CriminalsActivity.class);
            startActivity(intent);
        }
        else if (v.getId() == R.id.laws) {
            intent = new Intent(getActivity(), LawsActivity.class);
            startActivity(intent);
        }
    }
}
