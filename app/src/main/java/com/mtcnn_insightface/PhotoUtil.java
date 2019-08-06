package com.mtcnn_insightface;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PhotoUtil {

    public static byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the

        return temp;
    }

    //    /**
//     * 处理旋转后的图片
//     * @param originpath 原图路径
//     * @param context 上下文
//     * @return 返回修复完毕后的图片路径
//     */
    public static Bitmap amendRotatePhoto(String originpath, Bitmap bmp) {

        // 取得图片旋转角度
        int angle = readPictureDegree(originpath);

        // 把原图压缩后得到Bitmap对象
        //Bitmap bmp = getCompressPhoto(originpath);;

        // 修复图片被旋转的角度
        Bitmap bitmap = rotatingImageView(angle, bmp);


        // 保存修复后的图片并返回保存后的图片路径
        savePhotoToSD(originpath, bitmap);

        return bitmap;
    }

    public static Bitmap amendRotateWithoutSave(String originpath, Bitmap bmp) {

        // 取得图片旋转角度
        int angle = readPictureDegree(originpath);


        // 把原图压缩后得到Bitmap对象
        //Bitmap bmp = getCompressPhoto(originpath);;

        // 修复图片被旋转的角度
        Bitmap bitmap = rotatingImageView(angle, bmp);

        return bitmap;
    }

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @param angle  被旋转角度
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */
    public static Bitmap rotatingImageView(int angle, Bitmap bitmap) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        //Log.i("sucess", "success here 1");
        matrix.postRotate(angle);
        //Log.i("sucess", "success here 2");
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            //Log.i("sucess", "success here 3");
        } catch (OutOfMemoryError e) {
        }
        //Log.i("sucess", "success here 4");
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            //Log.i("sucess", "success here 5");
            bitmap.recycle();
        }
        //Log.i("sucess", "success here 6");
        return returnBm;
    }

    public static void savePhotoToSD(String originString, Bitmap mbitmap) {
        File filePic;

        try {
            filePic = new File(originString);
            if (filePic.exists()) {
                filePic.delete();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            //Log.d("xxx", "saveBitmap: 2return");
            return;
        }
        //Log.d("xxx", "saveBitmap: " + filePic.getAbsolutePath());
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return resizedBitmap;
    }
}
