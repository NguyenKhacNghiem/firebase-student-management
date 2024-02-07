package com.example.studentmanagement.certificate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentmanagement.R;
import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ItemViewHolder>{
    private Context context;
    private List<String> certificates;
    private String selectedCertificate;

    public CertificateAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<String> certificates) {
        this.certificates = certificates;
        notifyDataSetChanged();
    }
    
    public String getSelectedCertificate() {
        return selectedCertificate;
    }

    @NonNull
    @Override
    public CertificateAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.certificate, parent, false);

        return new CertificateAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateAdapter.ItemViewHolder holder, int position) {
        String certificate = certificates.get(position);
        
        holder.tvCertificate.setText(certificate);

        // Hiển thị context menu khi nhấn giữ 1 item
        holder.itemView.setOnLongClickListener(v -> {
            int selectPosition = holder.getAdapterPosition();
            selectedCertificate = certificates.get(selectPosition);
            
            v.showContextMenu();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        if(certificates != null)
            return certificates.size();

        return 0;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCertificate;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCertificate = itemView.findViewById(R.id.tvCertificate);
        }
    }
}
