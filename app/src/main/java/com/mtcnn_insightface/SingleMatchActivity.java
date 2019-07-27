package com.mtcnn_insightface;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import static android.content.ContentValues.TAG;

public class SingleMatchActivity extends AppCompatActivity {

    public static final int SELECT_PICTURE = 1;
    static float similarity = 0;
    float similarity_threshold = (float)0.45;
    float[][] feature;

    Bitmap yourSelectedImage = null;

    private TextView matchResult;
    private ImageView pictureView;
    private TextView picturePath;

    int minFaceSize = 40;
    int testTimeCount = 1;
    int threadsNumber = 8;

    private MTCNN mtcnn = new MTCNN();
    private ARCFACE arcface = new ARCFACE();

    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_match);

        matchResult = (TextView) findViewById(R.id.similarity);
        pictureView = (ImageView) findViewById(R.id.show_picture);
        picturePath = (TextView) findViewById(R.id.picture_path);
        
        //模型初始化
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        final String sdPath = sdDir.toString() + "/.mtcnn/";
        mtcnn.FaceDetectionModelInit(sdPath);
        arcface.ArcFaceModelInit(sdPath);

        mtcnn.SetMinFaceSize(minFaceSize);
        mtcnn.SetTimeCount(testTimeCount);
        mtcnn.SetThreadsNumber(threadsNumber);

        if (ContextCompat.checkSelfPermission(SingleMatchActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SingleMatchActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            openAlbum();
        }
    }

    private void openAlbum(){
        Log.i(TAG, "enter for album");
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            //Uri selectedImage = data.getData();
            if (requestCode == SELECT_PICTURE) {
                Bitmap bitmap = decodeData(data);

                Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                yourSelectedImage = rgba;

                long searchTime = System.currentTimeMillis();

                int width_selected = yourSelectedImage.getWidth();
                int height_selected = yourSelectedImage.getHeight();
                byte[] imageDate_selected = PhotoUtil.getPixelsRGBA(yourSelectedImage);

                int faceInfoSelected[] = mtcnn.FaceDetect(imageDate_selected, width_selected,
                        height_selected, 4);
                String resultString = "";
                Bitmap drawBitmap = yourSelectedImage.copy(Bitmap.Config.ARGB_8888, true);
                Log.i("numface:", String.valueOf(faceInfoSelected[0]));
                if (faceInfoSelected[0] < 1){
                    resultString = "没有检测到人脸";
                }else {
                    int faceNum = faceInfoSelected[0];
                    feature = new float[faceNum][128];
                    for(int i=0;i<faceNum;i++) {
                        int left, top, right, bottom;
                        Canvas canvas = new Canvas(drawBitmap);
                        Paint paint = new Paint();
                        left = faceInfoSelected[1+14*i];
                        top = faceInfoSelected[2+14*i];
                        right = faceInfoSelected[3+14*i];
                        bottom = faceInfoSelected[4+14*i];
                        paint.setColor(Color.RED);
                        paint.setStyle(Paint.Style.STROKE);//不填充
                        paint.setStrokeWidth(5);  //线的宽度
                        canvas.drawRect(left, top, right, bottom, paint);
                        Paint paint1 = new Paint();
                        paint1.setColor(Color.BLUE);
                        //paint1.setStyle(Paint.Style.STROKE);
                        paint1.setStrokeWidth(3);
                        canvas.drawText(String.valueOf(i+1), left, top, paint1);
                        //画特征点
                        canvas.drawPoints(new float[]{faceInfoSelected[5+14*i],faceInfoSelected[10+14*i],
                                faceInfoSelected[6+14*i],faceInfoSelected[11+14*i],
                                faceInfoSelected[7+14*i],faceInfoSelected[12+14*i],
                                faceInfoSelected[8+14*i],faceInfoSelected[13+14*i],
                                faceInfoSelected[9+14*i],faceInfoSelected[14+14*i]}, paint);//画多个点
                    }
                    for (int i = 0;i < faceInfoSelected[0];i++) {
                        int[] singleFaceInfoSelected = new int[15];
                        singleFaceInfoSelected[0] = 0;
                        for (int j = 0;j < 14;j++){
                            singleFaceInfoSelected[j+1] = faceInfoSelected[14*i + j + 1];
                        }
                        feature[i] = arcface.GetFeature(imageDate_selected, width_selected,
                                height_selected, singleFaceInfoSelected);
                    }
                    File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
                    String dir_path = sdDir.toString() + "/.Mtcnn_insightface/";
                    File dir_file = new File(dir_path);
                    int txt_num = FileUtil.getNumOfTxt(dir_file);
                    int[] txt_line = new int[txt_num];

                    if (txt_num > 0) {
                        for (int i = 0; i < txt_num; i++) {
                            int current_txt_num = i + 1;
                            String vector_path = sdDir.toString() + "/.Mtcnn_insightface/faculty" +
                                    String.valueOf(current_txt_num) + ".txt";
                            txt_line[i] = FileUtil.getLineOfTxt(vector_path);
                        }
                    }
                    Arrays.sort(txt_line);
                    int max_line = 0;
                    if (txt_line.length > 0)
                        max_line = txt_line[txt_line.length-1];

                    for (int k = 0;k < faceInfoSelected[0];k++) {

                        boolean whileFlag = true;
                        int vector_line = 1;
                        while (whileFlag) {
                            if (vector_line > max_line) {
                                whileFlag = false;
                                resultString += "人脸" + String.valueOf(k+1) + "不在数据库里！" + "\n";
                                break;
                            }
                            for (int i = 0; i < txt_num; i++) {
                                int current_txt_num = i + 1;
                                String vector_path = sdDir.toString() + "/.Mtcnn_insightface/faculty" +
                                        String.valueOf(current_txt_num) + ".txt";
                                float[] feature_selected;
                                if (vector_line > FileUtil.getLineOfTxt(vector_path)) {
                                    continue;
                                } else {
                                    feature_selected = FileUtil.getFloatFromFile(vector_path, vector_line);
                                }
                                similarity = arcface.NewCalcSimilarity(feature_selected, feature[k]);


                                if (similarity > similarity_threshold) {
                                    resultString += "人脸" + String.valueOf(k+1) +
                                            "与faculty" + String.valueOf(current_txt_num) +
                                            "的相似度：" + String.valueOf(similarity) +"\n";
                                    whileFlag = false;
                                    break;
                                }
                            }
                            vector_line += 1;
                        }
                    }
                }
                searchTime = System.currentTimeMillis() - searchTime;
                resultString += "查询耗时：" + String.valueOf(searchTime) + "ms\n";
                picturePath.setText("文件路径：" + imagePath);
                matchResult.setText(resultString);
                pictureView.setImageBitmap(drawBitmap);
                //pictureView.setImageBitmap(rgba);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults){
        switch(requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else{
                    Toast.makeText(this, "You denied the permission",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    private Bitmap decodeData(Intent data){
        Bitmap bitmap;
        if (Build.VERSION.SDK_INT >= 19){
            bitmap = handleImageOnKitKat(data);

        }
        else{
            bitmap = handleImageBeforeKitKat(data);
        }
        Log.i(TAG, "success here");
        Log.i(TAG, imagePath);
        bitmap = PhotoUtil.amendRotateWithoutSave(imagePath, bitmap);
        Log.i(TAG, "success here");
        int REQUIRED_SIZE = 200;
        int width_tmp = bitmap.getWidth();
        int height_tmp = bitmap.getHeight();
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
        }
        return PhotoUtil.resizeImage(bitmap, width_tmp, height_tmp);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null,
                null);
        if (cursor != null) {
            if (((Cursor) cursor).moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @TargetApi(19)
    private Bitmap handleImageOnKitKat(Intent data){
//        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content" +
                        "://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri, null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        return displayImage(imagePath);
    }

    private Bitmap handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        //String imagePath = getImagePath(uri, null);
        return displayImage(imagePath);
    }

    private Bitmap displayImage(String imagePath){
        if (imagePath != null) {
            return BitmapFactory.decodeFile(imagePath);
        }
        else
            return null;
    }
}
