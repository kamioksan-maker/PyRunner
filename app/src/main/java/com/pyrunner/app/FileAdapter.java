package com.pyrunner.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<ScriptFile> files = new ArrayList<>();
    private OnFileItemClickListener listener;

    public interface OnFileItemClickListener {
        void onFileClick(ScriptFile file);
        void onFileDelete(ScriptFile file);
    }

    public FileAdapter(OnFileItemClickListener listener) {
        this.listener = listener;
    }

    public void setFiles(List<ScriptFile> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        ScriptFile file = files.get(position);
        holder.tvFileName.setText(file.name);
        holder.tvFileDate.setText(file.lastModified);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFileClick(file);
            }
        });
        
        holder.ivMore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFileDelete(file);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        TextView tvFileDate;
        View ivMore;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileDate = itemView.findViewById(R.id.tvFileDate);
            ivMore = itemView.findViewById(R.id.ivMore);
        }
    }
}
