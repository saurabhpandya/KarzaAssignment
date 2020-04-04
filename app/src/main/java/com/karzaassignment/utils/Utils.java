package com.karzaassignment.utils;

import android.text.format.DateFormat;

import java.util.Date;

public class Utils {

    public static String convertDateFormat(long millis, String format) {

        String dateString = DateFormat.format("MM/dd/yyyy", new Date(millis)).toString();
        return dateString;
    }

    public static String getFileSize(String bytes) {
        long bytesL = Long.parseLong(bytes);
        String fileSize = "";
        if (bytesL >= 1024) {
            float kbs = bytesL / 1024;
            fileSize = String.format("%.2f kb", kbs);
            if (kbs >= 1024) {
                float mbs = kbs / 1024;
                fileSize = String.format("%.2f mb", mbs);
            }
        } else {
            fileSize = bytesL + " bytes";
        }
        return fileSize;
    }

}
