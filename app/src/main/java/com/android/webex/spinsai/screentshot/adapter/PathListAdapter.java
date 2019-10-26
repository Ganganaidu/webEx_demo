package com.android.webex.spinsai.screentshot.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.webex.spinsai.R;
import com.android.webex.spinsai.models.FilePaths;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class PathListAdapter extends RecyclerView.Adapter<PathListAdapter.MyHolder> {

    private ArrayList<FilePaths> pathList;
    private OnItemClickListener listener;

    public PathListAdapter(OnItemClickListener onItemClickListener) {
        listener = onItemClickListener;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_path, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.fileNameTextView.setText(pathList.get(position).getFileName());

        File file = new File(pathList.get(position).getFilePath());
        Picasso.with(holder.imageView.getContext())
                .load(file)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return pathList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView fileNameTextView;

        MyHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pathView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onItemClick(pathList.get(getAdapterPosition()));
                }
            });
        }
    }

    public void updateData(ArrayList<FilePaths> list) {
        pathList = list;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(FilePaths path);
    }
}
