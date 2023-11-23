package com.example.alertify_department_admin.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.alertify_department_admin.R;
import com.example.alertify_department_admin.users.Users_Details_Activity;
import com.example.alertify_department_admin.model.Users_Model;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class UsersAdp extends RecyclerView.Adapter<UsersAdp.Holder> {

    private Context context;

    private List<Users_Model> usersList;

    public UsersAdp(Context context, List<Users_Model> users) {
        this.context = context;
        usersList = users;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.users_recycler_design, parent, false);
        UsersAdp.Holder holder = new UsersAdp.Holder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        Users_Model usersModel = usersList.get(position);

        if (usersModel.getUserStatus().equals("block")) {
            Glide.with(context.getApplicationContext()).load(usersModel.getImgUrl()).into(holder.userImg);
            holder.userName.setText(usersModel.getName());
            holder.userEmail.setText(usersModel.getEmail());
            holder.blockBtn.setVisibility(View.VISIBLE);
        } else {
            Glide.with(context.getApplicationContext()).load(usersModel.getImgUrl()).into(holder.userImg);
            holder.userName.setText(usersModel.getName());
            holder.userEmail.setText(usersModel.getEmail());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Users_Details_Activity.class);
                intent.putExtra("Users_Model", usersModel);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class Holder extends RecyclerView.ViewHolder {

        private ShapeableImageView userImg;

        private TextView userName, userEmail;

        private ImageView blockBtn;

        public Holder(@NonNull View itemView) {
            super(itemView);

            userImg = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            blockBtn = itemView.findViewById(R.id.block_btn);

        }
    }
}
