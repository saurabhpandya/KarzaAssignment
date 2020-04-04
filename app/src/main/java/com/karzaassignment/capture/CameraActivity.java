package com.karzaassignment.capture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karzaassignment.BaseActivity;
import com.karzaassignment.R;
import com.karzaassignment.capture.viewmodel.CameraViewModel;
import com.karzaassignment.databinding.ActivityMainBinding;

import java.util.List;

public class CameraActivity extends BaseActivity implements View.OnClickListener {

    private final String TAG = CameraActivity.class.getSimpleName();

    private final int GALLERY_REQUEST_CODE = 1001;

    public Bitmap bitmap;

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;

    private ActivityMainBinding binding;
    private CameraViewModel viewModel;
    private boolean isTourchOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = ViewModelProviders.of(this).get(CameraViewModel.class);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Capture Image");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setListner();
        checkForPermission();
        setView();

    }

    private void setView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCamera = Camera.open();
                mPreview = new CameraPreview(CameraActivity.this, mCamera);
                binding.cPreview.addView(mPreview);
            }
        });

    }

    private void setListner() {
        binding.txtvwGallery.setOnClickListener(this);
        binding.imgvwCapture.setOnClickListener(this);
        binding.imgvwFlash.setOnClickListener(this);
    }

    public void onResume() {
        super.onResume();
        setupCamera();
    }

    private void setupCamera() {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        /* Set Auto focus */
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        mCamera.setParameters(parameters);
        mPicture = getPictureCallback();
        mPreview.refreshCamera(mCamera);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private void checkForPermission() {

        Dexter.withActivity(this)
                .withPermissions(neededPermissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            viewModel.getLastKnownLocation();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();

    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress();
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        bitmap = viewModel.rotateImage(bitmap, 90);
                        String imagePath = viewModel.saveImage(bitmap);

                        hideProgress();

                        Intent intent = new Intent(CameraActivity.this, CapturedImageActivity.class);
                        intent.putExtra("image", imagePath);
                        startActivity(intent);
                    }
                });


            }
        };
        return picture;
    }

    private void pickFromGallery() {
        //Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String imgDecodableString = viewModel.getImageFromUri(data.getData());
                            String imagePath = viewModel.saveImage(BitmapFactory.decodeFile(imgDecodableString));

                            Intent intent = new Intent(CameraActivity.this, CapturedImageActivity.class);
                            intent.putExtra("image", imagePath);
                            startActivity(intent);
                        }
                    });


                    break;

            }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgvw_capture:
                mCamera.takePicture(null, null, mPicture);
                break;
            case R.id.txtvw_gallery:
                pickFromGallery();
                break;
            case R.id.imgvw_flash:
                isTourchOn = !isTourchOn;
                toggleFlash();
                break;

        }
    }

    private void toggleFlash() {

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
