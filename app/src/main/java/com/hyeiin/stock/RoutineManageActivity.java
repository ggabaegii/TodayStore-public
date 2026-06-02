package com.hyeiin.stock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutineManageActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private View layoutEmpty;
    private FloatingActionButton fabAdd;
    private RoutineManageAdapter adapter;
    private final List<Routine> routines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_manage);
        setupToolbar(R.id.toolbarRoutine, false);

        MaterialToolbar toolbar = findViewById(R.id.toolbarRoutine);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewRoutineManage);
        layoutEmpty = findViewById(R.id.layoutRoutineEmpty);
        fabAdd = findViewById(R.id.fabAdd);

        adapter = new RoutineManageAdapter(routines, this::onRoutineChanged, this::deleteRoutine);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showAddRoutineDialog());
        loadFromFirestore();
    }

    private String getCurrentUid() {
        String authUid = FirebaseManager.uid();
        return authUid.isEmpty() ? UserSession.get().getUid() : authUid;
    }

    private String getCurrentStoreId() {
        return UserSession.get().getStoreId();
    }

    private void loadFromFirestore() {
        String uid = getCurrentUid();
        if (uid.isEmpty()) {
            toast("사용자 정보가 없어 루틴을 불러올 수 없습니다.");
            updateEmptyState();
            return;
        }

        FirebaseManager.db().collection("routines")
                .whereEqualTo("ownerId", uid)
                .get()
                .addOnSuccessListener(this::handleRoutineQueryResult)
                .addOnFailureListener(e -> {
                    toast("루틴을 불러오지 못했습니다: " + e.getMessage());
                    updateEmptyState();
                });
    }

    private void handleRoutineQueryResult(QuerySnapshot qs) {
        routines.clear();
        for (DocumentSnapshot doc : qs.getDocuments()) {
            Routine routine = new Routine(
                    doc.getId(),
                    doc.getString("name"),
                    doc.getString("ownerId"),
                    doc.getString("dayHint")
            );
            List<String> items = (List<String>) doc.get("items");
            if (items != null) {
                for (String item : items) {
                    routine.addItem(item);
                }
            }
            routine.setExpanded(false);
            routines.add(routine);
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void showAddRoutineDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_routine, null);
        TextInputLayout tilName = view.findViewById(R.id.tilRoutineName);
        TextInputEditText etName = view.findViewById(R.id.etRoutineName);
        TextInputEditText etDayHint = view.findViewById(R.id.etRoutineDayHint);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("새 루틴 추가")
                .setView(view)
                .setPositiveButton("저장", null)
                .setNegativeButton("취소", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        tilName.setError("루틴 이름을 입력하세요.");
                        return;
                    }

                    tilName.setError(null);
                    String dayHint = etDayHint.getText() != null
                            ? etDayHint.getText().toString().trim()
                            : "";
                    saveRoutineToFirestore(name, dayHint, new ArrayList<>());
                    dialog.dismiss();
                }));

        dialog.show();
    }

    private void saveRoutineToFirestore(String name, String dayHint, List<String> items) {
        String uid = getCurrentUid();
        String storeId = getCurrentStoreId();

        if (uid.isEmpty()) {
            toast("로그인 정보가 없어 루틴을 저장할 수 없습니다.");
            return;
        }
        if (storeId.isEmpty()) {
            toast("매장 정보가 없어 루틴을 저장할 수 없습니다.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("ownerId", uid);
        data.put("ownerName", UserSession.get().getName());
        data.put("ownerRole", UserSession.get().getRole());
        data.put("storeId", storeId);
        data.put("storeName", UserSession.get().getStoreName());
        data.put("dayHint", dayHint);
        data.put("items", items);
        data.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        data.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        String routineId = FirebaseManager.db().collection("routines").document().getId();
        FirebaseManager.db().collection("routines")
                .document(routineId)
                .set(data)
                .addOnSuccessListener(ref -> {
                    Routine routine = new Routine(routineId, name, uid, dayHint);
                    for (String item : items) {
                        routine.addItem(item);
                    }
                    routines.add(routine);
                    adapter.notifyItemInserted(routines.size() - 1);
                    updateEmptyState();
                    toast("루틴이 저장되었습니다.");
                })
                .addOnFailureListener(e ->
                        toast("루틴 저장 실패: " + e.getMessage()));
    }

    private void onRoutineChanged(Routine routine) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", routine.getName());
        data.put("dayHint", routine.getDayHint());
        data.put("items", routine.getItems());
        data.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        FirebaseManager.db().collection("routines")
                .document(routine.getId())
                .update(data)
                .addOnFailureListener(e -> toast("루틴 수정 실패: " + e.getMessage()));
        updateEmptyState();
    }

    private void deleteRoutine(Routine routine) {
        FirebaseManager.db().collection("routines")
                .document(routine.getId())
                .delete()
                .addOnFailureListener(e -> toast("루틴 삭제 실패: " + e.getMessage()));

        int index = routines.indexOf(routine);
        if (index >= 0) {
            routines.remove(index);
            adapter.notifyItemRemoved(index);
        }
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean empty = routines.isEmpty();
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

class RoutineManageAdapter extends RecyclerView.Adapter<RoutineManageAdapter.VH> {

    interface OnRoutineChangeListener {
        void onChange(Routine routine);
    }

    interface OnRoutineDeleteListener {
        void onDelete(Routine routine);
    }

    private final List<Routine> routines;
    private final OnRoutineChangeListener changeListener;
    private final OnRoutineDeleteListener deleteListener;

    RoutineManageAdapter(List<Routine> routines,
                         OnRoutineChangeListener changeListener,
                         OnRoutineDeleteListener deleteListener) {
        this.routines = routines;
        this.changeListener = changeListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine_manage, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(routines.get(position));
    }

    @Override
    public int getItemCount() {
        return routines.size();
    }

    class VH extends RecyclerView.ViewHolder {
        private final LinearLayout layoutHeader;
        private final LinearLayout layoutSubItems;
        private final LinearLayout containerSubItems;
        private final LinearLayout layoutAddSubItem;
        private final TextView tvName;
        private final TextView tvMeta;
        private final ImageView ivExpand;

        VH(View itemView) {
            super(itemView);
            layoutHeader = itemView.findViewById(R.id.layoutRoutineCardHeader);
            layoutSubItems = itemView.findViewById(R.id.layoutRoutineSubItems);
            containerSubItems = itemView.findViewById(R.id.containerSubItems);
            layoutAddSubItem = itemView.findViewById(R.id.layoutAddSubItem);
            tvName = itemView.findViewById(R.id.tvManageRoutineName);
            tvMeta = itemView.findViewById(R.id.tvManageRoutineMeta);
            ivExpand = itemView.findViewById(R.id.ivExpandIcon);
        }

        void bind(Routine routine) {
            tvName.setText(routine.getName());
            updateMeta(routine);
            applyExpanded(routine.isExpanded());
            renderSubItems(routine);

            layoutHeader.setOnClickListener(v -> {
                routine.setExpanded(!routine.isExpanded());
                applyExpanded(routine.isExpanded());
                renderSubItems(routine);
            });

            layoutHeader.setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(itemView.getContext())
                        .setTitle("루틴 삭제")
                        .setMessage("\"" + routine.getName() + "\" 루틴을 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (d, w) -> {
                            if (deleteListener != null) {
                                deleteListener.onDelete(routine);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
                return true;
            });

            layoutAddSubItem.setOnClickListener(v -> showAddSubItemDialog(routine));
        }

        private void updateMeta(Routine routine) {
            String dayHint = routine.getDayHint() != null && !routine.getDayHint().isEmpty()
                    ? routine.getDayHint() + " · "
                    : "";
            tvMeta.setText(dayHint + routine.getCount() + "개 항목");
        }

        private void applyExpanded(boolean expanded) {
            layoutSubItems.setVisibility(expanded ? View.VISIBLE : View.GONE);
            ivExpand.setImageResource(expanded ? R.drawable.ic_expand_less : R.drawable.ic_expand_more);
        }

        private void renderSubItems(Routine routine) {
            containerSubItems.removeAllViews();
            for (int i = 0; i < routine.getItems().size(); i++) {
                final int index = i;
                View row = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.item_routine_sub, containerSubItems, false);
                ((TextView) row.findViewById(R.id.tvSubItemText)).setText(routine.getItems().get(i));
                row.findViewById(R.id.btnDeleteSubItem).setOnClickListener(v -> {
                    routine.removeItem(index);
                    updateMeta(routine);
                    renderSubItems(routine);
                    if (changeListener != null) {
                        changeListener.onChange(routine);
                    }
                });
                containerSubItems.addView(row);
            }
        }

        private void showAddSubItemDialog(Routine routine) {
            EditText editText = new EditText(itemView.getContext());
            editText.setHint("항목 내용을 입력해 주세요.");
            editText.setPadding(48, 24, 48, 24);

            new MaterialAlertDialogBuilder(itemView.getContext())
                    .setTitle("항목 추가")
                    .setView(editText)
                    .setPositiveButton("추가", (d, w) -> {
                        String text = editText.getText().toString().trim();
                        if (text.isEmpty()) {
                            return;
                        }
                        routine.addItem(text);
                        updateMeta(routine);
                        renderSubItems(routine);
                        if (changeListener != null) {
                            changeListener.onChange(routine);
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        }
    }
}
