package com.example.alertify_department_admin.records;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.records.crimes.CrimesActivity;

public class Records_Fragment extends Fragment implements View.OnClickListener {

    private CardView crimesCard;

    private Intent intent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.records_fragment, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        crimesCard = view.findViewById(R.id.crimes);
        crimesCard.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.crimes:
                intent = new Intent(getActivity(), CrimesActivity.class);
                startActivity(intent);
                break;

        }
    }
}
