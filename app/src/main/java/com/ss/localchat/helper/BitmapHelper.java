package com.ss.localchat.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapHelper {

    public static Bitmap getResizedBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > height && width <= maxSize || width < height && height <= maxSize) {
            return bitmap;
        } else {
            float bitmapRatio = (float) width / (float) height;
            if (bitmapRatio > 1) {
                width = maxSize;
                height = (int) (width / bitmapRatio);
            } else {
                height = maxSize;
                width = (int) (height * bitmapRatio);
            }

            return Bitmap.createScaledBitmap(bitmap, width, height, true);
        }
    }

    public static Bitmap uriToBitmap(Context context, Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();

            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File bitmapToFile(Context context, Bitmap bitmap, String imageExtension) {
        File fileDir = context.getFilesDir();
        File imageFile = new File(fileDir, "image" + imageExtension);

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);

            if (imageExtension.equals(".jpg")) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os);
            } else if(imageExtension.equals(".png")) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, os);
            }

            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageFile;
    }
}
