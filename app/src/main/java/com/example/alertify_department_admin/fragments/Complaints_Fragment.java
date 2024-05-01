package com.example.alertify_department_admin.fragments;

import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_DEP_ADMIN_REF;
import static com.example.alertify_department_admin.constants.Constants.ALERTIFY_HIGH_AUTHORITY_REF;
import static com.example.alertify_department_admin.constants.Constants.USERS_COMPLAINTS_REF;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.alertify_department_admin.adapters.ComplaintsAdp;
import com.example.alertify_department_admin.databinding.ComplaintsFragmentBinding;
import com.example.alertify_department_admin.main_utils.AppSharedPreferences;
import com.example.alertify_department_admin.models.ComplaintModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Complaints_Fragment extends Fragment {

    private ComplaintsFragmentBinding binding;
    private List<ComplaintModel> complaints;
    private DatabaseReference depAdminRef, complaintsRef, highAuthorityRef;
    private AppSharedPreferences appSharedPreferences;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = ComplaintsFragmentBinding.inflate(inflater, container, false);
        init();
        Log.d("TAGDataCri", "onCreateView Complaints");
        return binding.getRoot();
    }

    private void init() {
        depAdminRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_DEP_ADMIN_REF);
        highAuthorityRef = FirebaseDatabase.getInstance().getReference(ALERTIFY_HIGH_AUTHORITY_REF);
        complaintsRef = FirebaseDatabase.getInstance().getReference(USERS_COMPLAINTS_REF); // firebase initialization
        complaints = new ArrayList<ComplaintModel>();
        appSharedPreferences = new AppSharedPreferences(requireActivity());
        binding.complaintsRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 2));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isAdded()) {
            return;
        }
        fetchComplaintsData();
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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

    private void search(String newText) {
        ArrayList<ComplaintModel> searchList = new ArrayList<>();
        for (ComplaintModel i : complaints) {
            if (i.getCrimeType().toLowerCase().contains(newText.toLowerCase()) || i.getComplaintDateTime().toLowerCase().contains(newText.toLowerCase()) || i.getCrimeDate().toLowerCase().contains(newText.toLowerCase()) || i.getCrimeTime().toLowerCase().contains(newText.toLowerCase()) || i.getPoliceStation().toLowerCase().contains(newText.toLowerCase()) || i.getCrimeLocation().toLowerCase().contains(newText.toLowerCase()) || i.getInvestigationStatus().toLowerCase().contains(newText.toLowerCase())) {
                searchList.add(i);
            }
        }
        setDataToRecycler(searchList);
    }

    private void fetchComplaintsData() {
        binding.complaintsProgressbar.setVisibility(View.VISIBLE);
        complaints.clear();

        // Listen for changes in complaintList
        depAdminRef.child(appSharedPreferences.getString("depAdminId")).child("complaintList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    complaints.clear(); // Clear complaints to avoid duplication
                    for (DataSnapshot snapshotData : snapshot.getChildren()) {
                        String complaintID = snapshotData.getValue(String.class);
                        listenForComplaintUpdates(complaintID);
                    }
                } else {
                    binding.complaintsProgressbar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.complaintsProgressbar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForComplaintUpdates(String complaintID) {
        complaintsRef.child(complaintID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ComplaintModel complaint = snapshot.getValue(ComplaintModel.class);
                    if (complaint != null) {
                        int index = findComplaintIndex(complaintID);
                        if (index == -1) {
                            // New complaint
                            complaints.add(complaint);
                        } else {
                            // Existing complaint, update it
                            complaints.set(index, complaint);
                        }

                        complaints.sort((complaint1, complaint2) -> {
                            return complaint2.getComplaintDateTime().compareTo(complaint1.getComplaintDateTime()); // Descending order
                        });
                        setDataToRecycler(complaints);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.complaintsProgressbar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int findComplaintIndex(String complaintID) {
        for (int i = 0; i < complaints.size(); i++) {
            if (complaints.get(i).getComplaintId().equals(complaintID)) {
                return i;
            }
        }
        return -1;
    }

    private long getHourDifferenceOfComplaints(String dateString) {
        try {
            Date date = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault()).parse(dateString);
            long diffInMillis = Calendar.getInstance().getTimeInMillis() - date.getTime();
            return TimeUnit.MILLISECONDS.toHours(diffInMillis);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0; // Return 0 if there's an error parsing the date
        }
    }

    private void getPendingComplaintsForHighAuthority() {
        if (!complaints.isEmpty()) {
            List<String> pendingComplaintList = new ArrayList<>();

            for (ComplaintModel complaint : complaints) {
                if (complaint.getInvestigationStatus().equals("Pending")) {
                    long hoursDifference = getHourDifferenceOfComplaints(complaint.getComplaintDateTime());
                    if (hoursDifference >= 48 && !complaint.getSendToHighAuthority()) {
                        pendingComplaintList.add(complaint.getComplaintId());
                    }
                }
            }

            if (!pendingComplaintList.isEmpty()) {
                getHighAuthorityUserComplaintsID(pendingComplaintList);
            }
        }
    }

    private void getHighAuthorityUserComplaintsID(List<String> pendingComplaintList) {
        highAuthorityRef.child(appSharedPreferences.getString("depAdminHighAuthorityId")).child("complaintList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> complaints = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    complaints.add(dataSnapshot.getValue(String.class));
                }

                Set<String> mergedComplaints = new HashSet<>(complaints);
                mergedComplaints.addAll(pendingComplaintList);

                sendPendingComplaintsToHighAuthority(new ArrayList<>(mergedComplaints), pendingComplaintList, appSharedPreferences.getString("depAdminHighAuthorityId"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPendingComplaintsToHighAuthority(List<String> updatedComplaintsList, List<String> pendingComplaintList, String depAdminHighAuthorityId) {
        highAuthorityRef.child(depAdminHighAuthorityId).child("complaintList").setValue(updatedComplaintsList)
                .addOnSuccessListener(aVoid -> {
                    updateComplaintSendHighAuthorityStatus(pendingComplaintList);
                    getHighAuthorityFCMToken(depAdminHighAuthorityId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateComplaintSendHighAuthorityStatus(List<String> pendingComplaintList) {
        for (String complaint : pendingComplaintList) {;
            complaintsRef.child(complaint).child("sendToHighAuthority").setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void getHighAuthorityFCMToken(String depAdminHighAuthorityId) {
        highAuthorityRef.child(depAdminHighAuthorityId).child("highAuthorityFCMToken").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                sendNotification(task.getResult().getValue().toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String token) {
        try {
            JSONObject jsonObject = new JSONObject();

            JSONObject dataObj = new JSONObject();
            dataObj.put("title", appSharedPreferences.getString("depAdminName"));
            dataObj.put("body", "has pending complaints.");

            jsonObject.put("data", dataObj);
            jsonObject.put("to", token);

            callApi(jsonObject);

        } catch (Exception e) {

        }
    }

    void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder().url(url).post(body).header("Authorization", "Bearer AAAA-aYabrI:APA91bHATVQVDYwB1qVX2_O8D1wWhVy0weiIPNJ5-G76w7WMSqcyqVs3HkOqJw8qYXlEl5YvG_62HgIyURoeNPpJN5n3v3jVeNtsGTKmle7tw7tuxxhrtpyCd0zcniEjIgb9aldbIG0l").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }
        });
    }

    private void setDataToRecycler(List<ComplaintModel> complaints) {
        ComplaintsAdp complaintsAdapter = new ComplaintsAdp(getActivity(), complaints);
        binding.complaintsRecycler.setAdapter(complaintsAdapter);
        binding.complaintsProgressbar.setVisibility(View.GONE);

        getPendingComplaintsForHighAuthority();
    }
}
