package com.hyeiin.stock;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class InventoryFragment extends Fragment {

    private TabLayout tabLayoutCategory;
    private TextInputEditText etSearch;
    private RecyclerView recyclerViewInventory;
    private View layoutEmpty;
    private FloatingActionButton fabAdd;

    private InventoryAdapter adapter;
    private final List<InventoryItem> fullList = new ArrayList<>();
    private String currentCategory = "전체";
    private ListenerRegistration listenerReg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupTabs();
        setupSearch();
        setupRecyclerView();
        setupFab();
        subscribeFirestore();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerReg != null) listenerReg.remove();
    }

    private void initViews(View view) {
        tabLayoutCategory = view.findViewById(R.id.tabLayoutCategory);
        etSearch = view.findViewById(R.id.etSearch);
        recyclerViewInventory = view.findViewById(R.id.recyclerViewInventory);
        layoutEmpty = view.findViewById(R.id.layoutInventoryEmpty);
        fabAdd = view.findViewById(R.id.fabAdd);
    }

    private void setupTabs() {
        tabLayoutCategory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategory = tab.getText() != null ? tab.getText().toString() : "전체";
                filterList(etSearch.getText() != null ? etSearch.getText().toString() : "");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                filterList(s.toString());
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new InventoryAdapter(
                this::showEditBottomSheet,
                this::deleteFromFirestore
        );
        recyclerViewInventory.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewInventory.setAdapter(adapter);
        recyclerViewInventory.setNestedScrollingEnabled(false);
    }

    private void subscribeFirestore() {
        String storeId = UserSession.get().getStoreId();
        if (storeId.isEmpty()) return;

        Query query = FirebaseManager.db()
                .collection("stores").document(storeId)
                .collection("inventory")
                .orderBy("name", Query.Direction.ASCENDING);

        listenerReg = query.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;

            for (DocumentChange change : snapshots.getDocumentChanges()) {
                String docId = change.getDocument().getId();
                InventoryItem item = documentToItem(docId, change.getDocument());

                switch (change.getType()) {
                    case ADDED:
                        int insertPos = 0;
                        while (insertPos < fullList.size() &&
                                fullList.get(insertPos).getName().compareTo(item.getName()) < 0) {
                            insertPos++;
                        }
                        fullList.add(insertPos, item);
                        break;
                    case MODIFIED:
                        for (int i = 0; i < fullList.size(); i++) {
                            if (fullList.get(i).getId().equals(docId)) {
                                fullList.set(i, item);
                                break;
                            }
                        }
                        break;
                    case REMOVED:
                        fullList.removeIf(it -> it.getId().equals(docId));
                        break;
                }
            }
            filterList(etSearch.getText() != null ? etSearch.getText().toString() : "");
        });
    }

    private InventoryItem documentToItem(String id, com.google.firebase.firestore.DocumentSnapshot doc) {
        String name = doc.getString("name");
        String category = doc.getString("category");
        Long qty = doc.getLong("quantity");
        String unit = doc.getString("unit");
        String imageUri = doc.getString("imageUri");
        return new InventoryItem(
                id,
                name != null ? name : "",
                category != null ? category : "기타",
                qty != null ? qty.intValue() : 0f,
                unit != null ? unit : "개",
                imageUri
        );
    }

    private void filterList(String query) {
        List<InventoryItem> filtered = new ArrayList<>();
        for (InventoryItem item : fullList) {
            boolean matchCat = currentCategory.equals("전체") || item.getCategory().equals(currentCategory);
            boolean matchQuery = item.getName().contains(query.trim());
            if (matchCat && matchQuery) filtered.add(item);
        }
        adapter.setList(filtered);
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerViewInventory.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupFab() {
        if (!UserSession.get().isOwner()) {
            fabAdd.setVisibility(View.GONE);
            return;
        }
        fabAdd.setOnClickListener(v -> showAddBottomSheet());
    }

    private void showAddBottomSheet() {
        InventoryBottomSheet sheet = InventoryBottomSheet.newInstance(null);
        sheet.setOnSaveListener(item -> saveToFirestore(item, null));
        sheet.show(getChildFragmentManager(), "AddInventory");
    }

    private void showEditBottomSheet(InventoryItem item) {
        InventoryBottomSheet sheet = InventoryBottomSheet.newInstance(item);
        sheet.setOnSaveListener(updated -> saveToFirestore(updated, item.getId()));
        sheet.show(getChildFragmentManager(), "EditInventory");
    }

    private void saveToFirestore(InventoryItem item, @Nullable String existingId) {
        String storeId = UserSession.get().getStoreId();
        if (storeId.isEmpty()) return;

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("name", item.getName());
        data.put("category", item.getCategory());
        data.put("quantity", item.getQuantity());
        data.put("unit", item.getUnit());
        data.put("imageUri", item.getImageUri());
        data.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        data.put("updatedBy", UserSession.get().getName());
        data.put("updatedByUid", UserSession.get().getUid());

        com.google.firebase.firestore.CollectionReference col =
                FirebaseManager.db().collection("stores").document(storeId).collection("inventory");

        if (existingId != null) {
            col.document(existingId).set(data);
        } else {
            col.add(data);
        }
    }

    private void deleteFromFirestore(InventoryItem item) {
        String storeId = UserSession.get().getStoreId();
        FirebaseManager.db()
                .collection("stores").document(storeId)
                .collection("inventory").document(item.getId())
                .delete();
    }
}
