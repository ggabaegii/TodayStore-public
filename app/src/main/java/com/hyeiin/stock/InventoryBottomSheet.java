package com.hyeiin.stock;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class InventoryBottomSheet extends BottomSheetDialogFragment {

    public interface OnSaveListener {
        void onSave(InventoryItem item);
    }

    private static final boolean ENABLE_IMAGE_FEATURE = false;

    private InventoryItem editItem;
    private OnSaveListener saveListener;

    private TextView tvSheetTitle;
    private ImageButton btnClose;
    private ImageView ivImagePreview;
    private MaterialButton btnCamera;
    private MaterialButton btnGallery;
    private TextInputLayout tilItemName;
    private TextInputEditText etItemName;
    private AutoCompleteTextView actvCategory;
    private AutoCompleteTextView actvUnit;
    private TextInputEditText etQuantity;
    private ImageButton btnDecrease;
    private ImageButton btnIncrease;
    private MaterialButton btnSave;

    private Uri selectedImageUri;
    private Uri cameraOutputUri;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            showImagePreview(selectedImageUri);
                        }
                    });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && cameraOutputUri != null) {
                            selectedImageUri = cameraOutputUri;
                            showImagePreview(selectedImageUri);
                        }
                    });

    private final ActivityResultLauncher<String> cameraPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            launchCamera();
                        }
                    });

    private final ActivityResultLauncher<String> storagePermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            launchGallery();
                        }
                    });

    public static InventoryBottomSheet newInstance(@Nullable InventoryItem item) {
        InventoryBottomSheet sheet = new InventoryBottomSheet();
        sheet.editItem = item;
        return sheet;
    }

    public void setOnSaveListener(OnSaveListener listener) {
        this.saveListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupDropdowns();
        setupQuantityButtons();
        setupImageButtons();
        setupSaveButton();

        if (editItem != null) {
            tvSheetTitle.setText("재고 수정");
            etItemName.setText(editItem.getName());
            actvCategory.setText(editItem.getCategory(), false);
            etQuantity.setText(String.valueOf((int) editItem.getQuantity()));
            actvUnit.setText(editItem.getUnit(), false);
            if (editItem.getImageUri() != null && !editItem.getImageUri().isEmpty()) {
                loadImageFromUrl(editItem.getImageUri());
            }
        }

        applyRoleRestrictions();

        btnClose.setOnClickListener(v -> dismiss());

        if (btnCamera != null) btnCamera.setVisibility(View.GONE);
        if (btnGallery != null) btnGallery.setVisibility(View.GONE);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                    Math.max(navBottom, imeBottom));
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private void bindViews(View view) {
        tvSheetTitle = view.findViewById(R.id.tvSheetTitle);
        btnClose = view.findViewById(R.id.btnSheetClose);
        ivImagePreview = view.findViewById(R.id.ivImagePreview);
        btnCamera = view.findViewById(R.id.btnCamera);
        btnGallery = view.findViewById(R.id.btnGallery);
        tilItemName = view.findViewById(R.id.tilItemName);
        etItemName = view.findViewById(R.id.etItemName);
        actvCategory = view.findViewById(R.id.actvCategory);
        etQuantity = view.findViewById(R.id.etQuantity);
        btnDecrease = view.findViewById(R.id.btnDecrease);
        btnIncrease = view.findViewById(R.id.btnIncrease);
        actvUnit = view.findViewById(R.id.actvUnit);
        btnSave = view.findViewById(R.id.btnSaveInventory);
    }

    private void setupDropdowns() {
        String[] categories = {"비품", "식재료", "기타"};
        actvCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, categories));

        String[] units = {"개", "박스", "kg", "g", "L", "ml", "봉"};
        actvUnit.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, units));
    }

    private void setupQuantityButtons() {
        btnDecrease.setOnClickListener(v -> {
            int quantity = (int) getQty();
            etQuantity.setText(String.valueOf(Math.max(0, quantity - 1)));
        });

        btnIncrease.setOnClickListener(v -> {
            int quantity = (int) getQty();
            etQuantity.setText(String.valueOf(quantity + 1));
        });
    }

    private void setupImageButtons() {
        if (!ENABLE_IMAGE_FEATURE) {
            if (btnCamera != null) btnCamera.setEnabled(false);
            if (btnGallery != null) btnGallery.setEnabled(false);
            return;
        }

        btnCamera.setOnClickListener(v -> checkAndLaunchCamera());
        btnGallery.setOnClickListener(v -> checkAndLaunchGallery());
    }

    private void applyRoleRestrictions() {
        if (UserSession.get().isOwner()) return;

        tilItemName.setEnabled(false);
        etItemName.setEnabled(false);
        actvCategory.setEnabled(false);
        actvUnit.setEnabled(false);
        if (btnCamera != null) btnCamera.setEnabled(false);
        if (btnGallery != null) btnGallery.setEnabled(false);
    }

    private void checkAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkAndLaunchGallery() {
        String permission = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED) {
            launchGallery();
        } else {
            storagePermLauncher.launch(permission);
        }
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            cameraOutputUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "이미지를 준비하지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("INVENTORY_" + stamp, ".jpg", storageDir);
    }

    private void showImagePreview(Uri uri) {
        ivImagePreview.setImageURI(uri);
        ivImagePreview.setColorFilter(null);
        ivImagePreview.setPadding(0, 0, 0, 0);
        ivImagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    private void loadImageFromUrl(String url) {
        ivImagePreview.setColorFilter(null);
        ivImagePreview.setPadding(0, 0, 0, 0);
        ivImagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        com.bumptech.glide.Glide.with(this)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .into(ivImagePreview);
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            String name = etItemName.getText() != null
                    ? etItemName.getText().toString().trim()
                    : "";

            if (name.isEmpty()) {
                tilItemName.setError("품목명을 입력하세요.");
                return;
            }

            tilItemName.setError(null);
            btnSave.setEnabled(false);
            btnSave.setText("저장 중...");

            if (ENABLE_IMAGE_FEATURE && selectedImageUri != null) {
                uploadImageThenSave(name);
            } else {
                String existingUrl = editItem != null ? editItem.getImageUri() : null;
                buildAndSave(name, existingUrl);
            }
        });
    }

    private void uploadImageThenSave(String name) {
        String storeId = UserSession.get().getStoreId();
        String fileName = "inventory/" + storeId + "/" + UUID.randomUUID() + ".jpg";
        StorageReference ref = FirebaseStorage.getInstance().getReference(fileName);

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri ->
                                buildAndSave(name, uri.toString())))
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "이미지 업로드에 실패해 기존 정보로 저장합니다.",
                            Toast.LENGTH_SHORT).show();
                    buildAndSave(name, editItem != null ? editItem.getImageUri() : null);
                });
    }

    private void buildAndSave(String name, @Nullable String imageUrl) {
        String category = actvCategory.getText() != null ? actvCategory.getText().toString() : "";
        String unit = actvUnit.getText() != null ? actvUnit.getText().toString() : "";
        float quantity = getQty();

        InventoryItem result;
        if (editItem != null) {
            editItem.setName(name);
            editItem.setCategory(category.isEmpty() ? "기타" : category);
            editItem.setQuantity(quantity);
            editItem.setUnit(unit.isEmpty() ? "개" : unit);
            if (imageUrl != null) {
                editItem.setImageUri(imageUrl);
            }
            result = editItem;
        } else {
            result = new InventoryItem(
                    UUID.randomUUID().toString(),
                    name,
                    category.isEmpty() ? "기타" : category,
                    quantity,
                    unit.isEmpty() ? "개" : unit,
                    imageUrl
            );
        }

        if (saveListener != null) {
            saveListener.onSave(result);
        }
        dismiss();
    }

    private float getQty() {
        try {
            String text = etQuantity.getText() != null ? etQuantity.getText().toString() : "0";
            return Float.parseFloat(text.isEmpty() ? "0" : text);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }
}
