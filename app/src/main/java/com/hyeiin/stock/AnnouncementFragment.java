package com.hyeiin.stock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AnnouncementFragment extends Fragment {

    private static final int REQUEST_WRITE = 1001;

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private View layoutEmpty;
    private FloatingActionButton fabAdd;
    private AnnouncementAdapter adapter;

    private final List<AnnouncementItem> allList = new ArrayList<>();
    private ListenerRegistration listenerReg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_announcement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = view.findViewById(R.id.tabLayoutAnnouncement);
        recyclerView = view.findViewById(R.id.recyclerViewAnnouncement);
        layoutEmpty = view.findViewById(R.id.layoutAnnouncementEmpty);
        fabAdd = view.findViewById(R.id.fabAdd);

        setupRecyclerView();
        setupTabs();
        setupFab();
        subscribeFirestore();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerReg != null) listenerReg.remove();
    }

    private void setupRecyclerView() {
        boolean isOwner = UserSession.get().isOwner();
        String currentUid = UserSession.get().getUid();
        adapter = new AnnouncementAdapter(
                isOwner,
                currentUid,
                this::openDetail,
                this::deleteFromFirestore
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterByTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AnnouncementWriteActivity.class);
            intent.putExtra("isOwner", UserSession.get().isOwner());
            intent.putExtra("defaultType", tabLayout.getSelectedTabPosition());
            startActivityForResult(intent, REQUEST_WRITE);
        });
    }

    private void subscribeFirestore() {
        String storeId = UserSession.get().getStoreId();
        if (storeId.isEmpty()) return;

        Query query = FirebaseManager.db()
                .collection("stores").document(storeId)
                .collection("announcements")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        listenerReg = query.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;

            for (DocumentChange change : snapshots.getDocumentChanges()) {
                String docId = change.getDocument().getId();
                AnnouncementItem item = docToItem(docId, change.getDocument());

                switch (change.getType()) {
                    case ADDED:
                        allList.add(0, item);
                        break;
                    case MODIFIED:
                        for (int i = 0; i < allList.size(); i++) {
                            if (allList.get(i).getId().equals(docId)) {
                                allList.set(i, item);
                                break;
                            }
                        }
                        break;
                    case REMOVED:
                        allList.removeIf(it -> it.getId().equals(docId));
                        break;
                }
            }
            filterByTab(tabLayout.getSelectedTabPosition());
        });
    }

    private AnnouncementItem docToItem(String id, com.google.firebase.firestore.DocumentSnapshot doc) {
        String type = doc.getString("type");
        com.google.firebase.Timestamp ts = doc.getTimestamp("createdAt");
        Date date = ts != null ? ts.toDate() : new Date();
        String dateTime = new SimpleDateFormat("yyyy.MM.dd  HH:mm", Locale.getDefault()).format(date);
        String dateShort = new SimpleDateFormat("MM.dd", Locale.getDefault()).format(date);

        return new AnnouncementItem(
                id,
                doc.getString("title"),
                doc.getString("content"),
                doc.getString("author"),
                doc.getString("authorUid"),
                dateTime,
                dateShort,
                "SPECIAL".equals(type) ? AnnouncementItem.Type.SPECIAL
                        : AnnouncementItem.Type.ANNOUNCEMENT
        );
    }

    private void filterByTab(int position) {
        AnnouncementItem.Type filterType = position == 1
                ? AnnouncementItem.Type.SPECIAL
                : AnnouncementItem.Type.ANNOUNCEMENT;

        if (position == 1) {
            fabAdd.setVisibility(UserSession.get().isOwner() ? View.VISIBLE : View.GONE);
        } else {
            fabAdd.setVisibility(View.VISIBLE);
        }

        List<AnnouncementItem> filtered = allList.stream()
                .filter(i -> i.getType() == filterType)
                .collect(Collectors.toList());

        adapter.setList(filtered);
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void openDetail(AnnouncementItem item) {
        Intent intent = new Intent(requireContext(), AnnouncementDetailActivity.class);
        intent.putExtra("itemId", item.getId());
        intent.putExtra("title", item.getTitle());
        intent.putExtra("content", item.getContent());
        intent.putExtra("author", item.getAuthor());
        intent.putExtra("authorId", item.getAuthorId());
        intent.putExtra("dateTime", item.getDateTime());
        intent.putExtra("isSpecial", item.isSpecial());
        intent.putExtra("isOwner", UserSession.get().isOwner());
        startActivity(intent);
    }

    private void deleteFromFirestore(AnnouncementItem item) {
        String storeId = UserSession.get().getStoreId();
        if (storeId.isEmpty()) return;
        FirebaseManager.db()
                .collection("stores").document(storeId)
                .collection("announcements").document(item.getId())
                .delete();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_WRITE || resultCode != Activity.RESULT_OK || data == null) return;

        String title = data.getStringExtra("title");
        String content = data.getStringExtra("content");
        int type = data.getIntExtra("type", 0);
        if (title == null || title.isEmpty()) return;

        String storeId = UserSession.get().getStoreId();
        if (storeId.isEmpty()) return;

        java.util.Map<String, Object> doc = new java.util.HashMap<>();
        doc.put("title", title);
        doc.put("content", content != null ? content : "");
        doc.put("type", type == 1 ? "SPECIAL" : "ANNOUNCEMENT");
        doc.put("author", UserSession.get().getName());
        doc.put("authorUid", UserSession.get().getUid());
        doc.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        doc.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        FirebaseManager.db()
                .collection("stores").document(storeId)
                .collection("announcements")
                .add(doc);
    }

    public void selectSpecialTab() {
        if (tabLayout != null) {
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) tab.select();
        }
    }
}
