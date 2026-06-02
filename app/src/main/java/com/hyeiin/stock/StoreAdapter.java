package com.hyeiin.stock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * 매장 목록 RecyclerView 어댑터.
 *
 * - 매장 카드 목록 표시
 * - 선택된 매장 카드 강조
 * - 매장 클릭/롱클릭 콜백 전달
 */
public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    public interface OnStoreClickListener {
        void onStoreClick(StoreItem store);
    }

    public interface OnStoreLongClickListener {
        void onLongClick(StoreItem store);
    }

    private List<StoreItem> storeList = new ArrayList<>();
    private int selectedPosition = -1;
    private final OnStoreClickListener listener;
    private OnStoreLongClickListener longClickListener;

    public StoreAdapter(OnStoreClickListener listener) {
        this.listener = listener;
    }

    public void setOnLongClickListener(OnStoreLongClickListener l) {
        this.longClickListener = l;
    }

    public void setStoreList(List<StoreItem> list) {
        this.storeList = list;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store_card, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        StoreItem store = storeList.get(position);
        holder.bind(store, position == selectedPosition);

        holder.cardStore.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onLongClick(storeList.get(position));
                return true;
            }
            return false;
        });
        holder.cardStore.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            if (prev != -1) notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);

            if (listener != null) listener.onStoreClick(store);
        });
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView cardStore;
        ImageView ivStoreIcon;
        TextView tvStoreName;
        TextView tvStoreAddress;
        ImageView ivArrow;

        StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            cardStore = itemView.findViewById(R.id.cardStore);
            ivStoreIcon = itemView.findViewById(R.id.ivStoreIcon);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreAddress = itemView.findViewById(R.id.tvStoreAddress);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }

        void bind(StoreItem store, boolean isSelected) {
            Context ctx = itemView.getContext();

            tvStoreName.setText(store.getName());
            tvStoreAddress.setText(store.getAddress());

            if (isSelected) {
                cardStore.setStrokeColor(
                        ContextCompat.getColor(ctx, R.color.colorPrimary));
                cardStore.setStrokeWidth(dpToPx(ctx, 2));
                cardStore.setCardBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.colorPrimaryContainer));
                ivArrow.setColorFilter(
                        ContextCompat.getColor(ctx, R.color.colorPrimary));
            } else {
                cardStore.setStrokeColor(
                        ContextCompat.getColor(ctx, R.color.colorDivider));
                cardStore.setStrokeWidth(dpToPx(ctx, 1));
                cardStore.setCardBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.white));
                ivArrow.setColorFilter(
                        ContextCompat.getColor(ctx, R.color.colorHint));
            }
        }

        private int dpToPx(Context ctx, int dp) {
            float density = ctx.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
