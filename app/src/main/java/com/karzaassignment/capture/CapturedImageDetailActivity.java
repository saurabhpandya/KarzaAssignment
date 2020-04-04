package com.karzaassignment.capture;

import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karzaassignment.R;
import com.karzaassignment.databinding.ActivityCapturedImageDetailBinding;
import com.karzaassignment.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class CapturedImageDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final String TAG = CapturedImageDetailActivity.class.getSimpleName();
    private String imagePath;

    private ActivityCapturedImageDetailBinding binding;
    private GoogleMap mMap;
    private LatLng latlng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_captured_image_detail);

        setTitle("Image Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getData();

    }

    private void getData() {
        imagePath = getIntent().getStringExtra("image_path");
        Log.d(TAG, "ImagePath: " + imagePath);

        Glide.with(this).load(imagePath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imgvwDetail);

        File file = new File(imagePath);

        String fileName = file.getName();
        String fileSize = Utils.getFileSize(file.length() + "");
        String fileTimeStamp = new Date(file.lastModified()).toString();

        binding.txtvwName.setText(fileName);
        binding.txtvwSize.setText(String.valueOf(fileSize));
        binding.txtvwTimestamp.setText(String.valueOf(fileTimeStamp));

        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);

            binding.txtvwPath.setText(file.getCanonicalPath());

            float[] latLong = new float[2];

            if (exifInterface.getLatLong(latLong)) {
                binding.txtvwLatitude.setText(String.valueOf(latLong[0]));
                binding.txtvwLongitude.setText(String.valueOf(latLong[1]));

                // Add a marker in Sydney and move the camera
                latlng = new LatLng(latLong[0], latLong[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (latlng != null) {
            mMap.addMarker(new MarkerOptions().position(latlng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
