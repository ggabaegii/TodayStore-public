package com.hyeiin.stock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChecklistFragment extends Fragment {

    private TabLayout tabLayout;
    private TextView tvProgressLabel;
    private TextView tvProgressPercent;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private View layoutEmpty;
    private FloatingActionButton fabAdd;
    private ImageButton btnHistory;

    private ChecklistAdapter adapter;
    private int currentTab = 0;
    private ListenerRegistration listenerReg;

    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getCurrentUid() {
        String authUid = FirebaseManager.uid();
        return authUid.isEmpty() ? UserSession.get().getUid() : authUid;
    }

    private String getCurrentStoreId() {
        return UserSession.get().getStoreId();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checklist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tabLayoutChecklist);
        tvProgressLabel = view.findViewById(R.id.tvProgressLabel);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        progressBar = view.findViewById(R.id.progressChecklist);
        recyclerView = view.findViewById(R.id.recyclerViewChecklist);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        fabAdd = view.findViewById(R.id.fabAdd);
        btnHistory = view.findViewById(R.id.btnHistory);

        setupAdapter();
        setupTabs();
        setupFab();
        setupHistory();
        loadTab(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerReg != null) {
            listenerReg.remove();
        }
    }

    private void setupAdapter() {
        UserSession session = UserSession.get();
        adapter = new ChecklistAdapter(
                getCurrentUid(),
                session.getName(),
                session.isOwner(),
                this::updateProgress
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadTab(currentTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadTab(int tab) {
        if (listenerReg != null) {
            listenerReg.remove();
            listenerReg = null;
        }
        adapter.setList(new ArrayList<>());

        if (tab == 0) {
            fabAdd.setVisibility(UserSession.get().isOwner() ? View.VISIBLE : View.GONE);
            subscribeGlobal();
        } else {
            fabAdd.setVisibility(View.VISIBLE);
            subscribePersonal();
        }
    }

    private void subscribeGlobal() {
        String storeId = getCurrentStoreId();
        if (storeId.isEmpty()) return;

        Query query = FirebaseManager.db()
                .collection("stores").document(storeId)
                .collection("checklist").document(today())
                .collection("global")
                .orderBy("createdAt", Query.Direction.ASCENDING);

        listenerReg = query.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;

            List<ChecklistItem> list = new ArrayList<>();
            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                list.add(fsDocToItem(doc, ChecklistItem.Type.GLOBAL));
            }
            adapter.setList(list);
            updateEmptyState();
        });
    }

    private void subscribePersonal() {
        String uid = getCurrentUid();
        if (uid.isEmpty()) return;

        Query query = FirebaseManager.db()
                .collection("personalChecklist").document(uid)
                .collection(today())
                .orderBy("createdAt", Query.Direction.ASCENDING);

        listenerReg = query.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;

            List<ChecklistItem> list = new ArrayList<>();
            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                list.add(fsDocToItem(doc, ChecklistItem.Type.PERSONAL));
            }
            adapter.setList(list);
            updateEmptyState();
        });
    }

    private ChecklistItem fsDocToItem(com.google.firebase.firestore.DocumentSnapshot doc,
                                      ChecklistItem.Type type) {
        String id = doc.getId();
        String task = doc.getString("task");
        String date = doc.getString("date");
        if (task == null) task = "";
        if (date == null) date = today();

        ChecklistItem item;
        if (type == ChecklistItem.Type.GLOBAL) {
            item = ChecklistItem.createGlobal(id, task, date);
        } else {
            item = ChecklistItem.createPersonal(id, task, date, getCurrentUid(), doc.getString("routineId"));
        }

        Boolean done = doc.getBoolean("done");
        if (Boolean.TRUE.equals(done)) {
            item.markDone(
                    doc.getString("doneBy"),
                    doc.getString("doneByUid"),
                    doc.getString("doneAt")
            );
        }
        return item;
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> {
            if (currentTab == 0) {
                showAddDialog(false);
            } else {
                showFabActionSheet();
            }
        });
    }

    private void showFabActionSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_fab_action, null);
        dialog.setContentView(view);

        view.findViewById(R.id.layoutDirectAdd).setOnClickListener(v -> {
            dialog.dismiss();
            showAddDialog(true);
        });
        view.findViewById(R.id.layoutRoutineAdd).setOnClickListener(v -> {
            dialog.dismiss();
            showRoutineListSheet();
        });
        dialog.show();
    }

    private void showAddDialog(boolean isPersonal) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_todo, null);
        com.google.android.material.textfield.TextInputLayout til =
                dialogView.findViewById(R.id.tilDialogTask);
        com.google.android.material.textfield.TextInputEditText et =
                dialogView.findViewById(R.id.etDialogTask);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(isPersonal ? "개인 할 일 추가" : "전체 할 일 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (d, w) -> {
                    String task = et.getText() != null ? et.getText().toString().trim() : "";
                    if (task.isEmpty()) {
                        til.setError("내용을 입력해 주세요.");
                        return;
                    }
                    saveChecklistItem(task, isPersonal, null);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void saveChecklistItem(String task, boolean isPersonal, @Nullable String routineId) {
        if (task == null || task.isEmpty()) return;

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("task", task);
        data.put("date", today());
        data.put("done", false);
        data.put("doneAt", null);
        data.put("doneBy", "");
        data.put("doneByUid", "");
        data.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        if (routineId != null) {
            data.put("routineId", routineId);
        } else {
            data.put("routineId", null);
        }

        if (!isPersonal) {
            String storeId = getCurrentStoreId();
            if (storeId.isEmpty()) {
                android.widget.Toast.makeText(
                        requireContext(),
                        "매장 정보를 찾을 수 없습니다.",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
                return;
            }

            FirebaseManager.db()
                    .collection("stores").document(storeId)
                    .collection("checklist").document(today())
                    .collection("global")
                    .add(data)
                    .addOnFailureListener(e ->
                            android.widget.Toast.makeText(
                                    requireContext(),
                                    "할 일 추가 실패: " + e.getMessage(),
                                    android.widget.Toast.LENGTH_SHORT
                            ).show());
        } else {
            String uid = getCurrentUid();
            if (uid.isEmpty()) {
                android.widget.Toast.makeText(
                        requireContext(),
                        "로그인 정보가 없습니다.",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
                return;
            }

            data.put("ownerId", uid);
            FirebaseManager.db()
                    .collection("personalChecklist").document(uid)
                    .collection(today())
                    .add(data)
                    .addOnFailureListener(e ->
                            android.widget.Toast.makeText(
                                    requireContext(),
                                    "할 일 추가 실패: " + e.getMessage(),
                                    android.widget.Toast.LENGTH_SHORT
                            ).show());
        }
    }

    private void showRoutineListSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_routine_list, null);
        dialog.setContentView(view);

        RecyclerView rv = view.findViewById(R.id.recyclerViewRoutines);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        String storeId = getCurrentStoreId();
        String uid = getCurrentUid();

        Query query = FirebaseManager.db().collection("routines").whereEqualTo("ownerId", uid);

        query.get()
                .addOnSuccessListener(qs -> {
                    List<Routine> routines = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : qs.getDocuments()) {
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
                        routines.add(routine);
                    }

                    rv.setAdapter(new RoutineListAdapter(
                            routines,
                            routine -> {
                                dialog.dismiss();
                                applyRoutine(routine);
                            }
                    ));

                    if (routines.isEmpty()) {
                        android.widget.Toast.makeText(
                                requireContext(),
                                "사용 가능한 루틴이 없습니다.",
                                android.widget.Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e ->
                        android.widget.Toast.makeText(
                                requireContext(),
                                "루틴 목록을 불러오지 못했습니다. " + e.getMessage(),
                                android.widget.Toast.LENGTH_SHORT
                        ).show());

        view.findViewById(R.id.btnManageRoutine).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(requireContext(), RoutineManageActivity.class));
        });
        view.findViewById(R.id.layoutAddNewRoutine).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(requireContext(), RoutineManageActivity.class));
        });

        dialog.show();
    }

    private void applyRoutine(Routine routine) {
        for (String taskText : routine.getItems()) {
            saveChecklistItem(taskText, true, routine.getId());
        }
    }

    private void setupHistory() {
        btnHistory.setOnClickListener(v ->
                HistoryBottomSheet.newInstance(
                                currentTab == 0 ? ChecklistItem.Type.GLOBAL : ChecklistItem.Type.PERSONAL,
                                getCurrentUid()
                        )
                        .show(getChildFragmentManager(), "History"));
    }

    private void updateProgress(int done, int total) {
        String label = currentTab == 0 ? "전체 할 일" : "개인 할 일";
        if (total == 0) {
            tvProgressLabel.setText(label + " 없음");
            tvProgressPercent.setText("0%");
            progressBar.setProgress(0);
            return;
        }

        int pct = (int) ((done / (float) total) * 100);
        tvProgressLabel.setText(label + " " + done + "/" + total + " 완료");
        tvProgressPercent.setText(pct + "%");
        android.animation.ObjectAnimator
                .ofInt(progressBar, "progress", progressBar.getProgress(), pct)
                .setDuration(400)
                .start();
    }

    private void updateEmptyState() {
        boolean empty = adapter.getSourceList().isEmpty();
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}
