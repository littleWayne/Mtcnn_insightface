package com.mtcnn_insightface;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static android.content.ContentValues.TAG;

public class DoubleCalcSimActivity extends AppCompatActivity {

    public static final int SELECT_PICTURE_ONE = 1;
    public static final int SELECT_PICTURE_TWO = 2;
    private static float similarity = 0;
    private float[] feature1;
    private float[] feature2;
    private Bitmap yourSelectedImage1 = null;
    private Bitmap yourSelectedImage2 = null;
    private TextView result;
    private ImageView pictureView1;
    private ImageView pictureView2;
    private Button match;
    int minFaceSize = 40;
    int testTimeCount = 1;
    int threadsNumber = 8;
    private String picture1Path;
    private String picture2Path;
    private MTCNN mtcnn = new MTCNN();
    private ARCFACE arcface = new ARCFACE();
    private String imagePath;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_calc_sim);

        result = (TextView) findViewById(R.id.match_result);
        pictureView1 = (ImageView) findViewById(R.id.show_picture_1);
        pictureView2 = (ImageView) findViewById(R.id.show_picture_2);
        match = (Button) findViewById(R.id.match);
        
        //模型初始化
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        final String sdPath = sdDir.toString() + "/.mtcnn/";
        mtcnn.FaceDetectionModelInit(sdPath);
        arcface.ArcFaceModelInit(sdPath);


        mtcnn.SetMinFaceSize(minFaceSize);
        mtcnn.SetTimeCount(testTimeCount);
        mtcnn.SetThreadsNumber(threadsNumber);

        if (ContextCompat.checkSelfPermission(DoubleCalcSimActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(DoubleCalcSimActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            openAlbum1();
        }

        if (ContextCompat.checkSelfPermission(DoubleCalcSimActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(DoubleCalcSimActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            openAlbum2();
        }

        match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String resultString = "";
                if ((yourSelectedImage1 == null) || (yourSelectedImage2 == null)){
                    //Intent intent = new Intent(DoubleCalcSimActivity.this, FreeMatchActivity.class);
                    //startActivity(intent);
                    //finish();
                    resultString += "图片缺失，没有两张图片" + "\n";
                }
                else {
                    int width_selected1 = yourSelectedImage1.getWidth();
                    int height_selected1 = yourSelectedImage1.getHeight();
                    byte[] imageDate_selected1 = PhotoUtil.getPixelsRGBA(yourSelectedImage1);

                    int width_selected2 = yourSelectedImage2.getWidth();
                    int height_selected2 = yourSelectedImage2.getHeight();
                    byte[] imageDate_selected2 = PhotoUtil.getPixelsRGBA(yourSelectedImage2);

                    int faceInfoSelected1[] = null;
                    faceInfoSelected1 = mtcnn.MaxFaceDetect(imageDate_selected1, width_selected1,
                            height_selected1, 4);
                    int faceInfoSelected2[] = null;
                    faceInfoSelected2 = mtcnn.MaxFaceDetect(imageDate_selected2, width_selected2,
                            height_selected2, 4);

                    resultString = picture1Path + "\n" + picture2Path + "\n";
                    Bitmap drawBitmap1 = yourSelectedImage1.copy(Bitmap.Config.ARGB_8888, true);
                    Bitmap drawBitmap2 = yourSelectedImage2.copy(Bitmap.Config.ARGB_8888, true);
                    if (faceInfoSelected1[0] < 1) {
                        resultString += "图片１没有检测到人脸！\n";
                    }
                    if (faceInfoSelected2[0] < 1) {
                        resultString += "图片２没有检测到人脸！\n";
                    } else if ((faceInfoSelected1[0] > 0) && (faceInfoSelected2[0] > 0)) {
                        int faceNum1 = faceInfoSelected1[0];
                        int faceNum2 = faceInfoSelected2[0];
                        for (int i = 0; i < faceNum1; i++) {
                            int left1, top1, right1, bottom1, left2, top2, right2, bottom2;
                            Canvas canvas1 = new Canvas(drawBitmap1);
                            Canvas canvas2 = new Canvas(drawBitmap2);
                            Paint paint = new Paint();
                            left1 = faceInfoSelected1[1 + 14 * i];
                            top1 = faceInfoSelected1[2 + 14 * i];
                            right1 = faceInfoSelected1[3 + 14 * i];
                            bottom1 = faceInfoSelected1[4 + 14 * i];
                            left2 = faceInfoSelected2[1 + 14 * i];
                            top2 = faceInfoSelected2[2 + 14 * i];
                            right2 = faceInfoSelected2[3 + 14 * i];
                            bottom2 = faceInfoSelected2[4 + 14 * i];
                            paint.setColor(Color.RED);
                            paint.setStyle(Paint.Style.STROKE);//不填充
                            paint.setStrokeWidth(5);  //线的宽度
                            canvas1.drawRect(left1, top1, right1, bottom1, paint);
                            canvas2.drawRect(left2, top2, right2, bottom2, paint);
                            //画特征点
                            canvas1.drawPoints(new float[]{faceInfoSelected1[5 + 14 * i], faceInfoSelected1[10 + 14 * i],
                                    faceInfoSelected1[6 + 14 * i], faceInfoSelected1[11 + 14 * i],
                                    faceInfoSelected1[7 + 14 * i], faceInfoSelected1[12 + 14 * i],
                                    faceInfoSelected1[8 + 14 * i], faceInfoSelected1[13 + 14 * i],
                                    faceInfoSelected1[9 + 14 * i], faceInfoSelected1[14 + 14 * i]}, paint);
                            canvas2.drawPoints(new float[]{faceInfoSelected2[5 + 14 * i], faceInfoSelected2[10 + 14 * i],
                                    faceInfoSelected2[6 + 14 * i], faceInfoSelected2[11 + 14 * i],
                                    faceInfoSelected2[7 + 14 * i], faceInfoSelected2[12 + 14 * i],
                                    faceInfoSelected2[8 + 14 * i], faceInfoSelected2[13 + 14 * i],
                                    faceInfoSelected2[9 + 14 * i], faceInfoSelected2[14 + 14 * i]}, paint);
                        }
                        feature1 = arcface.GetFeature(imageDate_selected1, width_selected1,
                                height_selected1, faceInfoSelected1);
                        feature2 = arcface.GetFeature(imageDate_selected2, width_selected2,
                                height_selected2, faceInfoSelected2);
                        similarity = arcface.NewCalcSimilarity(feature1, feature2);
                        resultString += ("相似度：" + String.valueOf(similarity));
                    }
                }
                result.setText(resultString);
            }
        });
    }

    private void openAlbum1(){
        //Log.i("openalbum:", "enter for album");
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE_ONE);
    }

    private void openAlbum2(){
        //Log.i("openalbum:", "enter for album");
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE_TWO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults){
        switch(requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum1();
                    openAlbum2();
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
        //Log.i("imagePath:", imagePath);
        bitmap = PhotoUtil.amendRotateWithoutSave(imagePath, bitmap);
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
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            //Uri selectedImage = data.getData();

            if (requestCode == SELECT_PICTURE_ONE) {
                Bitmap bitmap = decodeData(data);
                Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                yourSelectedImage1 = rgba;
                picture1Path = imagePath;
                pictureView1.setImageBitmap(yourSelectedImage1);
            }
            if (requestCode == SELECT_PICTURE_TWO) {
                Bitmap bitmap = decodeData(data);
                Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                yourSelectedImage2 = rgba;
                picture2Path = imagePath;
                pictureView2.setImageBitmap(yourSelectedImage2);
            }
        }
    }
}
