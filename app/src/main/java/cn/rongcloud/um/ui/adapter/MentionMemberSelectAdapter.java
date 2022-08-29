package cn.rongcloud.um.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cn.rongcloud.um.R;
import cn.rongcloud.um.utils.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.UserInfo;

public class MentionMemberSelectAdapter extends RecyclerView.Adapter<MentionMemberSelectAdapter.ViewHolder> {
    private List<UserInfo> data = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picklist_text, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvContent.setText(data.get(position).getName());
        holder.tvContent.setOnClickListener(view -> {
            if (listener != null) {
                listener.onClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setNewList(List<UserInfo> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public List<UserInfo> getData() {
        return data;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
