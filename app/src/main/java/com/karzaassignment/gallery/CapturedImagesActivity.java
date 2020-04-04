package com.karzaassignment.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karzaassignment.BaseActivity;
import com.karzaassignment.R;
import com.karzaassignment.capture.CameraActivity;
import com.karzaassignment.capture.CapturedImageDetailActivity;
import com.karzaassignment.databinding.ActivityCapturedImagesBinding;
import com.karzaassignment.gallery.adapter.CapturedImagesAdapter;
import com.karzaassignment.gallery.viewmodel.CapturedImagesViewModel;
import com.karzaassignment.model.CaptureImageModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class CapturedImagesActivity extends BaseActivity implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener, CapturedImagesAdapter.CapturedImageClickListner {

    private String TAG = CapturedImagesActivity.class.getSimpleName();

    private ActivityCapturedImagesBinding binding;
    private CapturedImagesAdapter adapter;
    private ArrayList<CaptureImageModel> arylstImages;

    private CapturedImagesViewModel.SORTBY sortby = CapturedImagesViewModel.SORTBY.NAME;

    private CapturedImagesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_captured_images);
        viewModel = ViewModelProviders.of(this).get(CapturedImagesViewModel.class);
        setupRecyclerView();
        setupListners();
        checkForPermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkForPermission();
    }

    private void setupListners() {
        binding.fab.setOnClickListener(this);
        binding.rdogrpSortby.setOnCheckedChangeListener(this);
    }

    private void setupRecyclerView() {
        adapter = new CapturedImagesAdapter(this, this);
        GridLayoutManager manager = new GridLayoutManager(this, 2);
        binding.rcyclvwImages.setLayoutManager(manager);
        binding.rcyclvwImages.setAdapter(adapter);
    }

    private void setData() {
        arylstImages = new ArrayList<>();
        showProgress();
        Disposable disposable = viewModel.getCapturedImagesObservable(sortby)
                .subscribe(new Consumer<ArrayList<CaptureImageModel>>() {
                    @Override
                    public void accept(ArrayList<CaptureImageModel> captureImageModels) throws Exception {
                        arylstImages = captureImageModels;
                        if (arylstImages.size() > 0) {
                            adapter.clearImages();
                            adapter.addImages(arylstImages);
                            binding.rcyclvwImages.setVisibility(View.VISIBLE);
                            binding.lnrlytSort.setVisibility(View.VISIBLE);
                            binding.txtvwNoData.setVisibility(View.GONE);
                        } else {
                            binding.rcyclvwImages.setVisibility(View.GONE);
                            binding.lnrlytSort.setVisibility(View.GONE);
                            binding.txtvwNoData.setVisibility(View.VISIBLE);
                        }
                        hideProgress();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        hideProgress();
                    }
                });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                startActivity(new Intent(this, CameraActivity.class));
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.rdobtn_sortby_name:
                sortby = CapturedImagesViewModel.SORTBY.NAME;
                setData();
                break;
            case R.id.rdobtn_sortby_size:
                sortby = CapturedImagesViewModel.SORTBY.SIZE;
                setData();
                break;
        }
    }

    @Override
    public void onTapImage(String imagePath) {
        Log.d(TAG, "Image Path: " + imagePath);
        Intent intent = new Intent(CapturedImagesActivity.this, CapturedImageDetailActivity.class);
        intent.putExtra("image_path", imagePath);
        startActivity(intent);
    }

    private void checkForPermission() {

        Dexter.withActivity(this)
                .withPermissions(neededPermissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            setData();
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

}
