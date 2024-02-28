package com.example.alertify_department_admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.adapters.UsersAdp;
import com.example.alertify_department_admin.model.Users_Model;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Users_Fragment extends Fragment {

    private ProgressBar fragmentProgressBar;

    private DatabaseReference usersRef;

    private StorageReference firebaseStorageReference;

    private List<Users_Model> users;

    private UsersAdp adp;

    private Users_Model usersModel;

    private RecyclerView recyclerView;

    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.users_fragment, container, false);
        init(view);
        fetchData();
        return view;
    }

    private void init(View view) {
        fragmentProgressBar = view.findViewById(R.id.fragmentProgressbar);

        firebaseStorageReference = FirebaseStorage.getInstance().getReference();

        usersRef = FirebaseDatabase.getInstance().getReference("AlertifyUser");

        users = new ArrayList<Users_Model>();

        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        searchView = view.findViewById(R.id.search_view);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    search(newText);
                    return true;
                }
            });

        }
    }

    private void search(String newText) {
        ArrayList<Users_Model> searchList = new ArrayList<Users_Model>();
        for (Users_Model i : users) {
            if (i.getName().toLowerCase().contains(newText.toLowerCase()) || i.getEmail().contains(newText.toLowerCase())) {
                searchList.add(i);
            }
        }
        adp = new UsersAdp(getActivity(), searchList);
        recyclerView.setAdapter(adp);
    }

    private void fetchData() {

        fragmentProgressBar.setVisibility(View.VISIBLE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                users.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    users.add(dataSnapshot.getValue(Users_Model.class));
                }

                fragmentProgressBar.setVisibility(View.INVISIBLE);

                setDataToRecycler(users);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataToRecycler(List<Users_Model> users) {
        adp = new UsersAdp(getActivity(), users);
        recyclerView.setAdapter(adp);
    }
}
