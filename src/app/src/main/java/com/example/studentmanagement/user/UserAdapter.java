package com.example.studentmanagement.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagement.R;
import com.example.studentmanagement.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ItemViewHolder>{
    private Context context;
    private List<User> users;
    private String selectedEmail;

    public UserAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public String getSelectedEmail() {
        return selectedEmail;
    }
    
    @NonNull
    @Override
    public UserAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.user, parent, false);

        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ItemViewHolder holder, int position) {
        User user = users.get(position);

        holder.tvEmail.setText("Email: " + user.getEmail());
        holder.tvName.setText("Name: " + user.getName());
        holder.tvAge.setText("Age: " + user.getAge());
        holder.tvPhone.setText("Phone: " + user.getPhone());
        
        // User nào mà status = false thì background sẽ có màu đỏ
        if(!user.isStatus())
            ((CardView) holder.itemView).setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));
        else
            ((CardView) holder.itemView).setCardBackgroundColor(ContextCompat.getColor(context, R.color.lightGray));

        // Hiển thị context menu khi nhấn giữ 1 item
        holder.itemView.setOnLongClickListener(v -> {
            int selectPosition = holder.getAdapterPosition();
            selectedEmail = users.get(selectPosition).getEmail();
            
            v.showContextMenu();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        if(users != null)
            return users.size();

        return 0;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvEmail;
        private TextView tvName;
        private TextView tvAge;
        private TextView tvPhone;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvName = itemView.findViewById(R.id.tvName);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvPhone = itemView.findViewById(R.id.tvPhone);
        }
    }
}
