package com.hyeiin.stock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_TYPE = "type";
    private static final String ARG_USER_ID = "userId";

    private ChecklistItem.Type type;
    private String userId;

    private TextView tvDateLabel;
    private LinearLayout containerItems;
    private TextView tvNoRecord;

    public static HistoryBottomSheet newInstance(ChecklistItem.Type type, String userId) {
        HistoryBottomSheet sheet = new HistoryBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type.name());
        args.putString(ARG_USER_ID, userId);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = ChecklistItem.Type.valueOf(getArguments().getString(ARG_TYPE));
            userId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        tvDateLabel = view.findViewById(R.id.tvHistoryDateLabel);
        containerItems = view.findViewById(R.id.containerHistoryItems);
        tvNoRecord = view.findViewById(R.id.tvHistoryNoRecord);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        loadRecordsForDate(today);

        calendarView.setOnDateChangeListener((picker, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    year, month + 1, day);
            loadRecordsForDate(date);
        });
    }

    private void setDateLabel(String date) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
            String label = new SimpleDateFormat("M월 d일(E)", new Locale("ko", "KR")).format(d);
            tvDateLabel.setText(label + " 완료 기록");
        } catch (Exception e) {
            tvDateLabel.setText(date + " 완료 기록");
        }
    }

    private void loadRecordsForDate(String date) {
        if (!isAdded()) return;
        setDateLabel(date);
        containerItems.removeAllViews();
        tvNoRecord.setVisibility(View.GONE);

        String storeId = UserSession.get().getStoreId();
        String uid = UserSession.get().getUid();

        final int[] loaded = {0};
        final List<ChecklistItem> globalList = new ArrayList<>();
        final List<ChecklistItem> personalList = new ArrayList<>();

        Runnable render = () -> {
            loaded[0]++;
            if (loaded[0] < 2) return;
            if (!isAdded()) return;
            renderBothSections(globalList, personalList);
        };

        if (!storeId.isEmpty()) {
            FirebaseManager.db()
                    .collection("stores").document(storeId)
                    .collection("checklist").document(date)
                    .collection("global")
                    .whereEqualTo("done", true)
                    .get()
                    .addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs.getDocuments()) {
                            globalList.add(fsDocToItem(doc, ChecklistItem.Type.GLOBAL, date));
                        }
                        render.run();
                    })
                    .addOnFailureListener(e -> render.run());
        } else {
            render.run();
        }

        if (!uid.isEmpty()) {
            FirebaseManager.db()
                    .collection("personalChecklist").document(uid)
                    .collection(date)
                    .whereEqualTo("done", true)
                    .get()
                    .addOnSuccessListener(qs -> {
                        for (DocumentSnapshot doc : qs.getDocuments()) {
                            personalList.add(fsDocToItem(doc, ChecklistItem.Type.PERSONAL, date));
                        }
                        render.run();
                    })
                    .addOnFailureListener(e -> render.run());
        } else {
            render.run();
        }
    }

    private void renderBothSections(List<ChecklistItem> globalList,
                                    List<ChecklistItem> personalList) {
        containerItems.removeAllViews();

        boolean hasAny = !globalList.isEmpty() || !personalList.isEmpty();
        if (!hasAny) {
            tvNoRecord.setVisibility(View.VISIBLE);
            return;
        }
        tvNoRecord.setVisibility(View.GONE);

        LayoutInflater inf = LayoutInflater.from(requireContext());

        addSectionHeader("전체 할 일");
        if (globalList.isEmpty()) {
            addEmptyRow("완료한 할 일이 없습니다");
        } else {
            for (ChecklistItem item : globalList) addItemRow(inf, item);
        }

        addDivider();

        addSectionHeader("개인 할 일");
        if (personalList.isEmpty()) {
            addEmptyRow("완료한 할 일이 없습니다");
        } else {
            for (ChecklistItem item : personalList) addItemRow(inf, item);
        }
    }

    private void addSectionHeader(String title) {
        TextView tv = new TextView(requireContext());
        tv.setText(title);
        tv.setTextColor(getResources().getColor(R.color.colorPrimary, null));
        tv.setTextSize(12f);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(dp(20), dp(12), dp(20), dp(4));
        containerItems.addView(tv);
    }

    private void addDivider() {
        View divider = new View(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        params.setMargins(0, dp(8), 0, dp(4));
        divider.setLayoutParams(params);
        divider.setBackgroundColor(getResources().getColor(R.color.colorDivider, null));
        containerItems.addView(divider);
    }

    private void addEmptyRow(String message) {
        TextView tv = new TextView(requireContext());
        tv.setText(message);
        tv.setTextColor(getResources().getColor(R.color.colorHint, null));
        tv.setTextSize(12f);
        tv.setPadding(dp(20), dp(6), dp(20), dp(6));
        containerItems.addView(tv);
    }

    private void addItemRow(LayoutInflater inf, ChecklistItem item) {
        View row = inf.inflate(R.layout.item_history_record, containerItems, false);

        TextView tvTask = row.findViewById(R.id.tvHistoryTask);
        tvTask.setText(item.getTask());
        tvTask.setPaintFlags(tvTask.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

        String meta = "";
        if (item.getDoneBy() != null && !item.getDoneBy().isEmpty()) meta += item.getDoneBy();
        if (item.getDoneAt() != null && !item.getDoneAt().isEmpty()) meta += "  " + item.getDoneAt();
        ((TextView) row.findViewById(R.id.tvHistoryMeta)).setText(meta);

        containerItems.addView(row);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private ChecklistItem fsDocToItem(DocumentSnapshot doc,
                                      ChecklistItem.Type itemType, String date) {
        String task = doc.getString("task");
        if (task == null) task = "";

        ChecklistItem item;
        if (itemType == ChecklistItem.Type.GLOBAL) {
            item = ChecklistItem.createGlobal(doc.getId(), task, date);
        } else {
            item = ChecklistItem.createPersonal(doc.getId(), task, date,
                    userId, doc.getString("routineId"));
        }

        item.markDone(
                doc.getString("doneBy"),
                doc.getString("doneByUid"),
                doc.getString("doneAt"));
        return item;
    }
}
