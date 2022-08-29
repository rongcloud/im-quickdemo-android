package cn.rongcloud.um.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cn.rongcloud.um.R;

import java.util.ArrayList;
import java.util.List;

public class LanguageSettingAdapter extends RecyclerView.Adapter<LanguageSettingAdapter.ViewHolder> {
    private List<String> list = new ArrayList<>();
    private int chooseId;

    @NonNull
    @Override
    public LanguageSettingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picklist_text, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull LanguageSettingAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tvContent.setText(list.get(position));
        holder.tvContent.setSelected(chooseId == position);
        holder.tvContent.setOnClickListener(view -> {
            chooseId = position;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNewList(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setChooseId(int chooseId) {
        this.chooseId = chooseId;
    }

    public int getChooseId() {
        return chooseId;
    }
}
