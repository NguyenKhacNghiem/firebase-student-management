package com.example.studentmanagement.student;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentmanagement.R;
import com.example.studentmanagement.model.Student;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ItemViewHolder>{
    private Context context;
    private List<Student> students;
    private String selectedId;

    public StudentAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Student> students) {
        this.students = students;
        notifyDataSetChanged();
    }

    public String getSelectedId() {
        return selectedId;
    }

    @NonNull
    @Override
    public StudentAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.student, parent, false);

        return new StudentAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAdapter.ItemViewHolder holder, int position) {
        Student student = students.get(position);

        holder.tvId.setText("Student ID: " + student.getId());
        holder.tvName.setText("Name: " + student.getName());
        holder.tvGender.setText("Gender: " + student.getGender());
        holder.tvMajor.setText("Major: " + student.getMajor());

        // Hiển thị context menu khi nhấn giữ 1 item
        holder.itemView.setOnLongClickListener(v -> {
            int selectPosition = holder.getAdapterPosition();
            selectedId = students.get(selectPosition).getId();

            v.showContextMenu();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        if(students != null)
            return students.size();

        return 0;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvId;
        private TextView tvName;
        private TextView tvGender;
        private TextView tvMajor;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvName = itemView.findViewById(R.id.tvName);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvMajor = itemView.findViewById(R.id.tvMajor);
        }
    }
}
