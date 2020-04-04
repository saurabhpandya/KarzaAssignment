package com.karzaassignment.capture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.karzaassignment.R;
import com.karzaassignment.databinding.ActivityCapturedImageBinding;
import com.karzaassignment.gallery.CapturedImagesActivity;

import java.io.File;

public class CapturedImageActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityCapturedImageBinding binding;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_captured_image);
        binding.btnNext.setOnClickListener(this);
        binding.btnRetake.setOnClickListener(this);
        setTitle("Comfirm Image");
        getData();
        setData();

    }

    private void getData() {
        imagePath = getIntent().getStringExtra("image");
    }

    private void setData() {
        File file = new File(imagePath);
        Glide.with(this)
                .load(file)
                .into(binding.imgvwCaotured);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_retake:
                File file = new File(imagePath);
                file.delete();
                finish();

                break;
            case R.id.btn_next:
                startActivity(new Intent(this, CapturedImagesActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {

    }
}
