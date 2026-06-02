package com.hyeiin.stock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(AnnouncementItem item);
    }

    public interface OnDeleteListener {
        void onDelete(AnnouncementItem item);
    }

    private List<AnnouncementItem> list = new ArrayList<>();
    private final OnItemClickListener clickListener;
    private final OnDeleteListener deleteListener;
    private final String currentUid;
    private final boolean isOwner;

    public AnnouncementAdapter(boolean isOwner, String currentUid,
                               OnItemClickListener click,
                               OnDeleteListener delete) {
        this.isOwner = isOwner;
        this.currentUid = currentUid;
        this.clickListener = click;
        this.deleteListener = delete;
    }

    public void setList(List<AnnouncementItem> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        h.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View viewAccentBar;
        MaterialCardView cardTypeBadge;
        TextView tvTitle;
        TextView tvDate;
        TextView tvAuthor;
        TextView tvPreview;

        ViewHolder(@NonNull View v) {
            super(v);
            viewAccentBar = v.findViewById(R.id.viewAccentBar);
            cardTypeBadge = v.findViewById(R.id.cardTypeBadge);
            tvTitle = v.findViewById(R.id.tvAnnouncementTitle);
            tvDate = v.findViewById(R.id.tvAnnouncementDate);
            tvAuthor = v.findViewById(R.id.tvAuthor);
            tvPreview = v.findViewById(R.id.tvContentPreview);
        }

        void bind(AnnouncementItem item) {
            Context ctx = itemView.getContext();

            tvTitle.setText(item.getTitle());
            tvDate.setText(item.getDateShort());
            tvAuthor.setText(item.getAuthor());
            tvPreview.setText(item.getContent());

            if (item.isSpecial()) {
                viewAccentBar.setVisibility(View.VISIBLE);
                cardTypeBadge.setVisibility(View.VISIBLE);
            } else {
                viewAccentBar.setVisibility(View.GONE);
                cardTypeBadge.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onClick(item);
            });

            boolean canDeleteThis = isOwner ||
                    (item.getAuthorId() != null && item.getAuthorId().equals(currentUid));
            itemView.setOnLongClickListener(v -> {
                if (!canDeleteThis) return false;
                new AlertDialog.Builder(ctx)
                        .setTitle("삭제 확인")
                        .setMessage("\"" + item.getTitle() + "\"\n\n정말 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (d, w) -> {
                            if (deleteListener != null) deleteListener.onDelete(item);
                        })
                        .setNegativeButton("취소", null)
                        .show();
                return true;
            });
        }
    }
}
