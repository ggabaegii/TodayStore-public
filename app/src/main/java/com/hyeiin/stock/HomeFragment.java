package com.hyeiin.stock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    public interface OnMenuClickListener {
        void onInventoryClick();
        void onAnnouncementClick();
        void onSpecialClick();
        void onChecklistClick();
    }

    private OnMenuClickListener menuListener;

    private TextView tvGreeting;
    private TextView tvDate;
    private TextView tvStore;
    private TextView tvTodoRemaining;
    private TextView tvTodoTotal;
    private TextView tvTodoPercent;
    private ProgressBar progressTodo;
    private TextView tvMenuSub2;
    private TextView tvMenuSub3;
    private TextView tvMenuSub4;

    public void setOnMenuClickListener(OnMenuClickListener l) {
        this.menuListener = l;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreeting = view.findViewById(R.id.tvWelcomeGreeting);
        tvDate = view.findViewById(R.id.tvWelcomeDate);
        tvStore = view.findViewById(R.id.tvWelcomeStore);
        tvTodoRemaining = view.findViewById(R.id.tvTodoRemaining);
        tvTodoTotal = view.findViewById(R.id.tvTodoTotal);
        tvTodoPercent = view.findViewById(R.id.tvTodoPercent);
        progressTodo = view.findViewById(R.id.progressTodo);
        tvMenuSub2 = view.findViewById(R.id.tvMenuSub2);
        tvMenuSub3 = view.findViewById(R.id.tvMenuSub3);
        tvMenuSub4 = view.findViewById(R.id.tvMenuSub4);

        setupWelcome();
        loadTodoProgress();
        loadAnnouncementBadge();
        loadSpecialBadge();
        loadPersonalChecklistBadge();
        setupMenuClicks(view);
    }

    private void setupWelcome() {
        UserSession s = UserSession.get();
        tvGreeting.setText(getTimeOfDayGreeting() + ", " + s.getName() + "님");
        tvStore.setText(s.getStoreName() + " · " + (s.isOwner() ? "사장" : "직원"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 (E) HH:mm", Locale.KOREA);
        tvDate.setText(sdf.format(new Date()));
    }

    private String getTimeOfDayGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 18) return "좋은 밤이에요";
        if (hour >= 12) return "좋은 오후예요";
        if (hour >= 6) return "좋은 아침이에요";
        return "좋은 밤이에요";
    }

    private void loadTodoProgress() {
        String storeId = UserSession.get().getStoreId();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (storeId.isEmpty()) return;

        FirebaseManager.db()
                .collection("stores").document(storeId)
                .collection("checklist").document(today)
                .collection("global")
                .get()
                .addOnSuccessListener(qs -> {
                    if (!isAdded()) return;
                    List<DocumentSnapshot> docs = qs.getDocuments();
                    int total = docs.size();
                    int done = 0;
                    for (DocumentSnapshot d : docs) {
                        Boolean isDone = d.getBoolean("done");
                        if (Boolean.TRUE.equals(isDone)) done++;
                    }
                    updateTodoUI(done, total);
                })
                .addOnFailureListener(e -> updateTodoUI(0, 0));
    }

    private void updateTodoUI(int done, int total) {
        if (!isAdded()) return;
        if (total == 0) {
            tvTodoRemaining.setText("할 일 없음");
            tvTodoTotal.setText("전체 0건");
            tvTodoPercent.setText("0% 완료");
            progressTodo.setProgress(0);
            return;
        }
        int remaining = total - done;
        int percent = (int) ((done / (float) total) * 100);
        tvTodoRemaining.setText("미완료 " + remaining + "건");
        tvTodoTotal.setText("전체 " + total + "건");
        tvTodoPercent.setText(percent + "% 완료");
        progressTodo.setProgress(percent);
    }

    private void loadAnnouncementBadge() {
        String storeId = UserSession.get().getStoreId();
        if (storeId.isEmpty() || tvMenuSub2 == null) return;

        FirebaseManager.db()
                .collection("stores").document(storeId)
                .collection("announcements")
                .whereEqualTo("type", "ANNOUNCEMENT")
                .get()
                .addOnSuccessListener(qs -> {
                    if (!isAdded()) return;
                    int count = qs.size();
                    if (count > 0 && tvMenuSub2 != null) {
                        tvMenuSub2.setText(count + "건 등록됨");
                    }
                });
    }

    private void loadSpecialBadge() {
        String storeId = UserSession.get().getStoreId();
        if (storeId.isEmpty() || tvMenuSub3 == null) return;

        FirebaseManager.db()
                .collection("stores").document(storeId)
                .collection("announcements")
                .whereEqualTo("type", "SPECIAL")
                .get()
                .addOnSuccessListener(qs -> {
                    if (!isAdded()) return;
                    int count = qs.size();
                    if (count > 0 && tvMenuSub3 != null) {
                        tvMenuSub3.setText(count + "건 등록됨");
                    }
                });
    }

    private void loadPersonalChecklistBadge() {
        String uid = UserSession.get().getUid();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (uid.isEmpty() || tvMenuSub4 == null) return;

        FirebaseManager.db()
                .collection("personalChecklist").document(uid)
                .collection(today)
                .get()
                .addOnSuccessListener(qs -> {
                    if (!isAdded() || tvMenuSub4 == null) return;
                    int total = qs.size();
                    int done = 0;
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        if (Boolean.TRUE.equals(d.getBoolean("done"))) done++;
                    }
                    if (total == 0) {
                        tvMenuSub4.setText("오늘 할 일 없음");
                    } else {
                        tvMenuSub4.setText("완료 " + done + "/" + total + "건");
                    }
                });
    }

    private void setupMenuClicks(View view) {
        view.findViewById(R.id.btnTodoShortcut).setOnClickListener(v -> {
            if (menuListener != null) menuListener.onChecklistClick();
        });
        view.findViewById(R.id.menuInventory).setOnClickListener(v -> {
            if (menuListener != null) menuListener.onInventoryClick();
        });
        view.findViewById(R.id.menuAnnouncement).setOnClickListener(v -> {
            if (menuListener != null) menuListener.onAnnouncementClick();
        });
        view.findViewById(R.id.menuSpecial).setOnClickListener(v -> {
            if (menuListener != null) menuListener.onSpecialClick();
        });
        view.findViewById(R.id.menuChecklist).setOnClickListener(v -> {
            if (menuListener != null) menuListener.onChecklistClick();
        });
    }
}
