package com.hyeiin.stock;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 재고 이미지를 전체 화면으로 표시한다.
 * Intent extras: imageUrl (String), itemName (String)
 */
public class ImageFullActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        String itemName = getIntent().getStringExtra("itemName");

        ImageView ivFull = findViewById(R.id.ivFullImage);
        TextView tvTitle = findViewById(R.id.tvFullImageTitle);
        ImageButton btnBack = findViewById(R.id.btnFullBack);

        if (itemName != null) tvTitle.setText(itemName);
        btnBack.setOnClickListener(v -> finish());

        // 이미지 로드
        if (imageUrl != null && !imageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(imageUrl)
                    .fitCenter()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_inventory)
                    .into(ivFull);
        }

        // 이미지를 누르면 닫기
        ivFull.setOnClickListener(v -> finish());
    }
}
