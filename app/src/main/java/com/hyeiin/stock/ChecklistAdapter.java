package com.hyeiin.stock;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChecklistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public interface OnProgressListener {
        void onProgress(int done, int total);
    }

    private static class Entry {
        boolean isHeader;
        String headerTitle;
        int headerCount;
        ChecklistItem item;

        static Entry header(String title, int count) {
            Entry e = new Entry();
            e.isHeader = true;
            e.headerTitle = title;
            e.headerCount = count;
            return e;
        }

        static Entry item(ChecklistItem i) {
            Entry e = new Entry();
            e.isHeader = false;
            e.item = i;
            return e;
        }
    }

    private final List<ChecklistItem> sourceList = new ArrayList<>();
    private final List<Entry> entries = new ArrayList<>();

    private final String currentUserId;
    private final String currentUserName;
    private final boolean isOwner;
    private final OnProgressListener progressListener;

    public ChecklistAdapter(String userId, String userName,
                            boolean isOwner, OnProgressListener listener) {
        this.currentUserId = userId;
        this.currentUserName = userName;
        this.isOwner = isOwner;
        this.progressListener = listener;
        setHasStableIds(true);
    }

    public List<ChecklistItem> getSourceList() {
        return sourceList;
    }

    public void setList(List<ChecklistItem> list) {
        sourceList.clear();
        sourceList.addAll(list);
        rebuildEntries();
    }

    private void rebuildEntries() {
        entries.clear();
        List<ChecklistItem> pending = new ArrayList<>();
        List<ChecklistItem> done = new ArrayList<>();
        for (ChecklistItem i : sourceList) {
            if (i.isDone()) done.add(i);
            else pending.add(i);
        }
        entries.add(Entry.header("미완료", pending.size()));
        for (ChecklistItem i : pending) entries.add(Entry.item(i));
        entries.add(Entry.header("완료", done.size()));
        for (ChecklistItem i : done) entries.add(Entry.item(i));

        notifyDataSetChanged();

        if (progressListener != null) {
            progressListener.onProgress(done.size(), sourceList.size());
        }
    }

    @Override
    public int getItemViewType(int pos) {
        return entries.get(pos).isHeader ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    @Override
    public long getItemId(int position) {
        Entry e = entries.get(position);
        if (e.isHeader) return ("header_" + e.headerTitle).hashCode();
        return e.item.getId().hashCode();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderVH(inf.inflate(R.layout.item_checklist_header, parent, false));
        }
        return new ItemVH(inf.inflate(R.layout.item_checklist_todo, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int pos) {
        Entry e = entries.get(pos);
        if (e.isHeader) {
            ((HeaderVH) vh).bind(e.headerTitle, e.headerCount);
        } else {
            ((ItemVH) vh).bind(e.item);
        }
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvCount;

        HeaderVH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvSectionTitle);
            tvCount = v.findViewById(R.id.tvSectionCount);
        }

        void bind(String title, int count) {
            tvTitle.setText(title);
            tvCount.setText(String.valueOf(count));
        }
    }

    class ItemVH extends RecyclerView.ViewHolder {
        MaterialCardView cardTodo;
        MaterialCheckBox checkbox;
        TextView tvTask;
        LinearLayout layoutDoneInfo;
        TextView tvDoneBy;
        TextView tvDoneAt;
        private ChecklistItem boundItem;

        ItemVH(View v) {
            super(v);
            cardTodo = v.findViewById(R.id.cardTodo);
            checkbox = v.findViewById(R.id.checkboxTodo);
            tvTask = v.findViewById(R.id.tvTask);
            layoutDoneInfo = v.findViewById(R.id.layoutDoneInfo);
            tvDoneBy = v.findViewById(R.id.tvDoneBy);
            tvDoneAt = v.findViewById(R.id.tvDoneAt);

            cardTodo.setOnClickListener(v2 -> {
                if (boundItem == null) return;
                handleToggle(boundItem);
            });

            checkbox.setClickable(false);
            checkbox.setFocusable(false);
        }

        void bind(ChecklistItem item) {
            this.boundItem = item;

            checkbox.setChecked(item.isDone());
            applyStyle(item.isDone());
            tvTask.setText(item.getTask());

            if (item.isDone()) {
                tvDoneBy.setText(item.getDoneBy() != null ? item.getDoneBy() : "");
                tvDoneAt.setText(item.getDoneAt() != null ? item.getDoneAt() : "");
                layoutDoneInfo.setVisibility(View.VISIBLE);
            } else {
                layoutDoneInfo.setVisibility(View.GONE);
            }

            if (isOwner) {
                cardTodo.setOnLongClickListener(v -> {
                    if (boundItem == null) return false;
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("할 일 삭제")
                            .setMessage("\"" + boundItem.getTask() + "\"\n\n정말 삭제하시겠습니까?")
                            .setPositiveButton("삭제", (d, w) -> {
                                deleteFromFirestore(boundItem);
                                sourceList.remove(boundItem);
                                rebuildEntries();
                            })
                            .setNegativeButton("취소", null)
                            .show();
                    return true;
                });
            } else {
                cardTodo.setOnLongClickListener(null);
            }
        }

        private void handleToggle(ChecklistItem item) {
            if (item.isDone()) {
                if (!item.canUncheck(currentUserId, isOwner)) {
                    Snackbar.make(
                            cardTodo,
                            "본인이 체크한 할 일만 취소할 수 있습니다.",
                            2000
                    ).show();
                    return;
                }
                item.markUndone();
            } else {
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                item.markDone(currentUserName, currentUserId, time);
            }

            checkbox.setChecked(item.isDone());
            animateStateChange(item.isDone());
            updateFirestoreCheck(item);

            cardTodo.postDelayed(() -> {
                if (getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                    rebuildEntries();
                }
            }, 250);
        }

        void applyStyle(boolean done) {
            Context ctx = itemView.getContext();
            if (done) {
                tvTask.setPaintFlags(tvTask.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTask.setTextColor(ContextCompat.getColor(ctx, R.color.colorHint));
                cardTodo.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.colorBackground));
            } else {
                tvTask.setPaintFlags(tvTask.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvTask.setTextColor(ContextCompat.getColor(ctx, R.color.colorTextPrimary));
                cardTodo.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.white));
            }
        }

        void animateStateChange(boolean done) {
            AnimatorSet shrink = new AnimatorSet();
            shrink.playTogether(
                    ObjectAnimator.ofFloat(cardTodo, "scaleX", 1f, 0.93f),
                    ObjectAnimator.ofFloat(cardTodo, "scaleY", 1f, 0.93f),
                    ObjectAnimator.ofFloat(cardTodo, "alpha", 1f, 0.7f)
            );
            shrink.setDuration(120);
            shrink.setInterpolator(new AccelerateDecelerateInterpolator());

            AnimatorSet expand = new AnimatorSet();
            expand.playTogether(
                    ObjectAnimator.ofFloat(cardTodo, "scaleX", 0.93f, 1f),
                    ObjectAnimator.ofFloat(cardTodo, "scaleY", 0.93f, 1f),
                    ObjectAnimator.ofFloat(cardTodo, "alpha", 0.7f, 1f)
            );
            expand.setDuration(180);
            expand.setInterpolator(new AccelerateDecelerateInterpolator());

            shrink.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator a) {
                    applyStyle(done);
                    layoutDoneInfo.setVisibility(done ? View.VISIBLE : View.GONE);
                }
            });
            AnimatorSet full = new AnimatorSet();
            full.playSequentially(shrink, expand);
            full.start();
        }

        private void updateFirestoreCheck(ChecklistItem item) {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("done", item.isDone());
            data.put("doneBy", item.getDoneBy());
            data.put("doneByUid", item.getDoneByUserId());
            data.put("doneAt", item.getDoneAt());

            if (item.getType() == ChecklistItem.Type.GLOBAL) {
                String storeId = UserSession.get().getStoreId();
                FirebaseManager.db()
                        .collection("stores").document(storeId)
                        .collection("checklist").document(today)
                        .collection("global").document(item.getId())
                        .update(data);
            } else {
                String uid = UserSession.get().getUid();
                FirebaseManager.db()
                        .collection("personalChecklist").document(uid)
                        .collection(today).document(item.getId())
                        .update(data);
            }
        }

        private void deleteFromFirestore(ChecklistItem item) {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            if (item.getType() == ChecklistItem.Type.GLOBAL) {
                String storeId = UserSession.get().getStoreId();
                FirebaseManager.db()
                        .collection("stores").document(storeId)
                        .collection("checklist").document(today)
                        .collection("global").document(item.getId())
                        .delete();
            } else {
                String uid = UserSession.get().getUid();
                FirebaseManager.db()
                        .collection("personalChecklist").document(uid)
                        .collection(today).document(item.getId())
                        .delete();
            }
        }
    }
}
