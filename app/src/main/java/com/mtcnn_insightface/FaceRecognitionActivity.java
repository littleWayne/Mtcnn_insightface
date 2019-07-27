package com.mtcnn_insightface;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import static android.content.ContentValues.TAG;

public class FaceRecognitionActivity extends AppCompatActivity {

    private static final int TAKE_PHOTO = 1;
    private static boolean TAKE_PHOTO_FLAG = false;
    private static float cal_similarity = 0;
    float similarity_threshold = (float)0.45;
    private String path_string = "";
    private float[] feature_taken;

    Bitmap takenImage = null;
    private ImageView picture;
    private TextView similarity;
    private TextView path;
    private Uri imageUri;
    private MTCNN mtcnn = new MTCNN();
    private ARCFACE arcface = new ARCFACE();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);
        TAKE_PHOTO_FLAG = false;
        cal_similarity = 0;
        path_string = "";

        similarity = (TextView) findViewById(R.id.similarity_result);
        path = (TextView) findViewById(R.id.path_result);
        Button save = (Button) findViewById(R.id.save_photo);
        Button cancel = (Button) findViewById(R.id.cancel_photo);
        picture = (ImageView) findViewById(R.id.show_photo);
        
        //模型初始化
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        final String sdPath = sdDir.toString() + "/.mtcnn/";
        mtcnn.FaceDetectionModelInit(sdPath);
        arcface.ArcFaceModelInit(sdPath);

        File outputImage = new File(Environment.getExternalStorageDirectory(), "/.Mtcnn_insightface/" + "capture.jpg");
        if (!outputImage.getParentFile().exists()) {
            outputImage.getParentFile().mkdirs();
        }

        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(FaceRecognitionActivity.this, "com.mtcnn_insightface.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TAKE_PHOTO_FLAG){
                    //rename and move capture.jpg
                    File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
                    String oldPicturePath = sdDir.toString() + "/.Mtcnn_insightface/capture.jpg";
                    File outputImage = new File(path_string);
                    if (!outputImage.getParentFile().exists()) {
                        outputImage.getParentFile().mkdirs();
                    }
                    FileUtil.copyFile(oldPicturePath, path_string);//可以做一步压缩
                    String txt_path_string = null;
                    int num = 0;
                    for (int j = 0; j < path_string.length(); j++) {
                        if (path_string.charAt(j) == '/') {
                            num += 1;
                        }
                        if (num == 6){
                            txt_path_string = path_string.substring(0, j);
                            break;
                        }
                    }
                    txt_path_string += ".txt";
                    FileUtil.saveFloatToFile(feature_taken, txt_path_string);
                    TAKE_PHOTO_FLAG = false;
                    finish();
                }
                else{
                    finish();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TAKE_PHOTO_FLAG){
                    //delete the capture, if not clicked, delete the capture before finish
                    TAKE_PHOTO_FLAG = false;
                    finish();
                }else {
                    finish();
                }
            }
        });
    }

    @Override
    public void finish(){
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        String oldPicturePath = sdDir.toString() + "/.Mtcnn_insightface/capture.jpg";
        if(FileUtil.fileIsExists(oldPicturePath)){
            File capture = new File(oldPicturePath);
            capture.delete();
        }else{
        }
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK){
                    try {//显示照片
                        Bitmap bitmap = decodeUri(imageUri);
                        takenImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                        picture.setImageBitmap(takenImage);
                        TAKE_PHOTO_FLAG = true;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    Log.i(TAG, "识别开始：");
                    long recognitiontime = System.currentTimeMillis();

                    mtcnn.SetMinFaceSize(40);
                    mtcnn.SetTimeCount(1);
                    mtcnn.SetThreadsNumber(8);

                    int width_taken = takenImage.getWidth();
                    int height_taken = takenImage.getHeight();

                    Log.i(TAG, "图像宽：" + String.valueOf(width_taken));
                    Log.i(TAG, "图像高：" + String.valueOf(height_taken));

                    byte[] imageDate_taken = PhotoUtil.getPixelsRGBA(takenImage);

                    Log.i(TAG, "mtcnn 检测开始：");
                    long mtcnntime = System.currentTimeMillis();
                    int faceInfo_taken[] = mtcnn.MaxFaceDetect(imageDate_taken, width_taken,
                            height_taken, 4);
                    mtcnntime = System.currentTimeMillis() - mtcnntime;
                    Log.i(TAG, "mtcnn 检测结束，用时：" + String.valueOf(mtcnntime));

                    if (faceInfo_taken[0] < 1){
                        similarity.setText("没有检测到人脸");
                        TAKE_PHOTO_FLAG = false;
                        break;
                    }

                    Log.i(TAG, "特征提取开始：");
                    long featuretime = System.currentTimeMillis();
                    feature_taken = arcface.GetFeature(imageDate_taken, width_taken,
                            height_taken, faceInfo_taken);
                    featuretime = System.currentTimeMillis() - featuretime;
                    Log.i(TAG, "特征提取结束，用时：" + String.valueOf(featuretime));

                    Log.i(TAG, "特征比对开始：");
                    long matchtime = System.currentTimeMillis();
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

                    boolean while_flag = true;
                    int vector_line = 1;
                    while(while_flag) {
                        if (vector_line > max_line)
                        {
                            while_flag = false;
                            path_string = sdDir.toString() + "/.Mtcnn_insightface/faculty" + String.valueOf(txt_num+1)+ "/" + "1.jpg";
                            similarity.setText("该人员不在人脸库里！");
                            break;
                        }
                        for (int i = 0; i < txt_num; i++) {
                            int current_txt_num = i + 1;
                            String vector_path = sdDir.toString() + "/.Mtcnn_insightface/faculty" +
                                    String.valueOf(current_txt_num) + ".txt";
                            float[] feature_selected;
                            if (vector_line > FileUtil.getLineOfTxt(vector_path)) {
                                    continue;
                            }else {
                                feature_selected = FileUtil.getFloatFromFile(vector_path, vector_line);
                            }

                            cal_similarity = arcface.NewCalcSimilarity(feature_selected, feature_taken);

                            if (cal_similarity > similarity_threshold) {
                                File file = new File(sdDir.toString() + "/.Mtcnn_insightface/faculty" +
                                        String.valueOf(current_txt_num));
                                File[] files = file.listFiles();
                                int num = files.length + 1;
                                path_string = sdDir.toString() + "/.Mtcnn_insightface/faculty"
                                        + String.valueOf(current_txt_num) + "/" + String.valueOf(num) + ".jpg";
                                similarity.setText("与faculty" + String.valueOf(current_txt_num) + "的相似度：" + cal_similarity);
                                while_flag = false;
                                break;
                            }
                        }
                        vector_line += 1;
                    }
                    matchtime = System.currentTimeMillis() - matchtime;
                    Log.i(TAG, "特征比对结束，用时：" + String.valueOf(matchtime));
                    recognitiontime = System.currentTimeMillis() - recognitiontime;
                    Log.i(TAG, "识别结束，用时:" + String.valueOf(recognitiontime));

                    path.setText("是否把该图存为标注图，路径是：" + path_string + "?");
                    picture.setImageBitmap(takenImage);
                }

                break;
            default:
                break;
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 200;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
        String path = selectedImage.getPath();
        return PhotoUtil.amendRotatePhoto(path, bitmap);
    }

//    private Bitmap defaultOpenAlbum(){
//        File outputImage = new File(Environment.getExternalStorageDirectory(), "/.Mtcnn_insightface/faculty1/" + "1.jpg");
//        if (Build.VERSION.SDK_INT >= 24) {
//            imageUri = FileProvider.getUriForFile(FaceRecognitionActivity.this, "com.mtcnn_insightface.fileprovider", outputImage);
//        } else {
//            imageUri = Uri.fromFile(outputImage);
//        }
//        Bitmap bitmap = null;
//        try{
//            bitmap = decodeUri(imageUri);
//        }catch (FileNotFoundException e){
//            e.printStackTrace();
//        }
//        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
//    }

}
