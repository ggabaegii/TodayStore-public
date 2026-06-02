package com.hyeiin.stock;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    public interface OnEditClickListener {
        void onEdit(InventoryItem item);
    }

    public interface OnDeleteListener {
        void onDelete(InventoryItem item);
    }

    private List<InventoryItem> list = new ArrayList<>();
    private final OnEditClickListener listener;
    private final OnDeleteListener deleteListener;

    public InventoryAdapter(OnEditClickListener listener, OnDeleteListener deleteListener) {
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    public void setList(List<InventoryItem> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_card, parent, false);
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
        ImageView ivThumbnail;
        TextView tvItemName;
        Chip chipCategory;
        TextView tvQuantity;
        ImageButton btnEdit;

        ViewHolder(@NonNull View v) {
            super(v);
            ivThumbnail = v.findViewById(R.id.ivThumbnail);
            tvItemName = v.findViewById(R.id.tvItemName);
            chipCategory = v.findViewById(R.id.chipCategory);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            btnEdit = v.findViewById(R.id.btnEdit);
        }

        void bind(InventoryItem item) {
            Context ctx = itemView.getContext();

            tvItemName.setText(item.getName());
            chipCategory.setText(item.getCategory());

            String qtyText = item.getQuantity() + item.getUnit();
            tvQuantity.setText(qtyText);
            tvQuantity.setTextColor(ContextCompat.getColor(
                    ctx,
                    item.getQuantity() == 0 ? R.color.colorError : R.color.colorTextSecondary
            ));

            String imgUrl = item.getImageUri();
            if (imgUrl != null && !imgUrl.isEmpty()) {
                ivThumbnail.setColorFilter(null);
                ivThumbnail.setPadding(0, 0, 0, 0);
                ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                com.bumptech.glide.Glide.with(ctx)
                        .load(imgUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_inventory)
                        .into(ivThumbnail);

                ivThumbnail.setOnClickListener(v -> {
                    Intent intent = new Intent(ctx, ImageFullActivity.class);
                    intent.putExtra("imageUrl", imgUrl);
                    intent.putExtra("itemName", item.getName());
                    ctx.startActivity(intent);
                });
            } else {
                ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                int pad = (int) (16 * ctx.getResources().getDisplayMetrics().density);
                ivThumbnail.setPadding(pad, pad, pad, pad);
                ivThumbnail.setImageResource(R.drawable.ic_inventory);
                ivThumbnail.setColorFilter(ContextCompat.getColor(ctx, R.color.colorPrimary));
                ivThumbnail.setOnClickListener(null);
            }

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(item);
            });

            itemView.setOnLongClickListener(v -> {
                if (!UserSession.get().isOwner()) return false;
                new AlertDialog.Builder(ctx)
                        .setTitle("재고 삭제")
                        .setMessage("\"" + item.getName() + "\"\n정말 삭제하시겠습니까?")
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
