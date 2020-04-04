package com.karzaassignment.gallery.viewmodel;

import android.app.Application;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.karzaassignment.R;
import com.karzaassignment.model.CaptureImageModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class CapturedImagesViewModel extends AndroidViewModel {

    private Application mApplication;

    public CapturedImagesViewModel(@NonNull Application application) {
        super(application);
        mApplication = application;
    }

    public Observable<ArrayList<CaptureImageModel>> getCapturedImagesObservable(final SORTBY sortby) {

        return Observable.create(new ObservableOnSubscribe<ArrayList<CaptureImageModel>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<CaptureImageModel>> emitter) throws Exception {
                String appDirectoryName = mApplication.getResources().getString(R.string.app_name);
                File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator
                        + appDirectoryName);
                directory.mkdirs();
                File[] fList = directory.listFiles();
                ArrayList<CaptureImageModel> arylstImages = new ArrayList<>();
                //get all the files from a directory
                if (fList != null && fList.length > 0) {
                    for (File file : fList) {
                        if (file.isFile()) {
                            CaptureImageModel captureImageModel = new CaptureImageModel();
                            captureImageModel.setPath(file.getAbsolutePath());
                            captureImageModel.setName(file.getName());
                            captureImageModel.setSize(file.length());
                            arylstImages.add(captureImageModel);
                        }
                    }
                }

                switch (sortby) {
                    case NAME:
                        Collections.sort(arylstImages, new NameCompare());
                        break;
                    case SIZE:
                        Collections.sort(arylstImages, new SizeCompare());
                        break;
                }

                emitter.onNext(arylstImages);
                emitter.onComplete();
            }
        });

    }

    public ArrayList<CaptureImageModel> getCapturedImages(SORTBY sortby) {
        String appDirectoryName = mApplication.getResources().getString(R.string.app_name);
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator
                + appDirectoryName);
        directory.mkdirs();
        File[] fList = directory.listFiles();
        ArrayList<CaptureImageModel> arylstImages = new ArrayList<>();
        //get all the files from a directory
        if (fList != null && fList.length > 0) {
            for (File file : fList) {
                if (file.isFile()) {
                    CaptureImageModel captureImageModel = new CaptureImageModel();
                    captureImageModel.setPath(file.getAbsolutePath());
                    captureImageModel.setName(file.getName());
                    captureImageModel.setSize(file.length());
                    arylstImages.add(captureImageModel);
                }
            }
        }

        switch (sortby) {
            case NAME:
                Collections.sort(arylstImages, new NameCompare());
                break;
            case SIZE:
                Collections.sort(arylstImages, new SizeCompare());
                break;
        }

        return arylstImages;

    }

    public enum SORTBY {
        NAME,
        SIZE
    }

    class NameCompare implements Comparator<CaptureImageModel> {

        @Override
        public int compare(CaptureImageModel captureImageModel, CaptureImageModel t1) {
            return t1.getName().compareTo(captureImageModel.getName());
        }
    }

    class SizeCompare implements Comparator<CaptureImageModel> {

        @Override
        public int compare(CaptureImageModel captureImageModel, CaptureImageModel t1) {
            if (captureImageModel.getSize() > t1.getSize()) return -1;
            if (captureImageModel.getSize() < t1.getSize()) return 1;
            else return 0;
        }
    }

}
