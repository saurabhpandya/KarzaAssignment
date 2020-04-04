package com.karzaassignment.capture.viewmodel;

import android.app.Application;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karzaassignment.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class CameraViewModel extends AndroidViewModel {

    private final String TAG = CameraViewModel.class.getSimpleName();

    public FusedLocationProviderClient fusedLocationClient;
    public Location lastKnownLocation;
    private Application application;

    public CameraViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public void getLastKnownLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(application);
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                lastKnownLocation = task.getResult();
            }
        });
    }

    public Bitmap rotateImage(Bitmap source, float angle) {

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File imgDir = new File(
                Environment.getExternalStorageDirectory() +
                        File.separator +
                        application.getResources().getString(R.string.app_name));
        // have the object build the directory structure, if needed.

        if (!imgDir.exists()) {
            Log.d(TAG, "" + imgDir.mkdirs());
            imgDir.mkdirs();
        }

        try {
            File f = new File(imgDir, Calendar.getInstance().getTimeInMillis() + ".jpg");
            f.createNewFile();   //give read write permission
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(application,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d(TAG, "File Saved::--->" + f.getAbsolutePath());

            ExifInterface exif = new ExifInterface(f.getCanonicalPath());
            //String latitudeStr = "90/1,12/1,30/1";
            double lat = lastKnownLocation.getLatitude();
            double alat = Math.abs(lat);
            String dms = Location.convert(alat, Location.FORMAT_SECONDS);
            String[] splits = dms.split(":");
            String[] secnds = (splits[2]).split("\\.");
            String seconds;
            if (secnds.length == 0) {
                seconds = splits[2];
            } else {
                seconds = secnds[0];
            }

            String latitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitudeStr);

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, lat > 0 ? "N" : "S");

            double lon = lastKnownLocation.getLongitude();
            double alon = Math.abs(lon);


            dms = Location.convert(alon, Location.FORMAT_SECONDS);
            splits = dms.split(":");
            secnds = (splits[2]).split("\\.");

            if (secnds.length == 0) {
                seconds = splits[2];
            } else {
                seconds = secnds[0];
            }
            String longitudeStr = splits[0] + "/1," + splits[1] + "/1," + seconds + "/1";


            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitudeStr);
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, lon > 0 ? "E" : "W");

            exif.saveAttributes();


            ExifInterface exifInterface = new ExifInterface(f.getAbsolutePath());

            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "" + lastKnownLocation.getLatitude());
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "" + lastKnownLocation.getLatitude());
            exifInterface.saveAttributes();

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";

    }

    public String getImageFromUri(Uri uri) {
        //data.getData return the content URI for the selected Image
        Uri selectedImage = uri;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        // Get the cursor
        Cursor cursor = application.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        // Move to first row
        cursor.moveToFirst();
        //Get the column index of MediaStore.Images.Media.DATA
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        //Gets the String value in the column
        String imgDecodableString = cursor.getString(columnIndex);
        cursor.close();
        return imgDecodableString;
    }

}
