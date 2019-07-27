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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static android.content.ContentValues.TAG;

public class TestDataSetActivity extends AppCompatActivity {

    private static float similarity = 0;
    private float[] feature1;
    private float[] feature2;
    private Bitmap yourSelectedImage1 = null;
    private Bitmap yourSelectedImage2 = null;
    private TextView result;
    int minFaceSize = 40;
    int testTimeCount = 1;
    int threadsNumber = 8;
    private MTCNN mtcnn = new MTCNN();
    private ARCFACE arcface = new ARCFACE();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_data_set);

        Log.i(TAG, "enter test");
        result = (TextView) findViewById(R.id.analysis_result);

        //模型初始化
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        final String sdPath = sdDir.toString() + "/.mtcnn/";
        mtcnn.FaceDetectionModelInit(sdPath);
        arcface.ArcFaceModelInit(sdPath);


        mtcnn.SetMinFaceSize(minFaceSize);
        mtcnn.SetTimeCount(testTimeCount);
        mtcnn.SetThreadsNumber(threadsNumber);

        //获取数据集不同人脸的目录
        String notOneSetPath = sdDir.toString() + "/notOneSet/";
        File notOneSet_file = new File(notOneSetPath);
        int notOneSet_num = FileUtil.getNumOfDir(notOneSet_file);

        //获取数据集同人脸的目录
        String isOneSetPath = sdDir.toString() + "/isOneSet/";
        File isOneSet_file = new File(isOneSetPath);
        int isOneSet_num = FileUtil.getNumOfDir(isOneSet_file);

        String similatity_dir= sdDir.toString() + "/similarity/";
        if (!FileUtil.fileIsExists(similatity_dir))
            FileUtil.newDirectory(similatity_dir);

        String notOneSetSimPath = sdDir.toString() + "/similarity/notOneSetTxt.txt";
        File file = new File(notOneSetSimPath);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String isOneSetSimPath = sdDir.toString() + "/similarity/isOneSetTxt.txt";
        File file1 = new File(isOneSetSimPath);
        if (file1.exists()) {
            file1.delete();
        }
        try {
            file1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }



        String resultString = "";
        float[] similarity_notone_set = new float[1500];
        for (int i = 0;i < 1500;i++){
            similarity_notone_set[i] = 1;
        }
        float[] similarity_isone_set = new float[1500];



        long notstarttime = System.currentTimeMillis();

        for (int index = 1;index < 1501;index++) {

            String pairPath = notOneSetPath + "pair" + String.valueOf(index) + "/";

            File pairFile = new File(pairPath);
            File[] dirs = pairFile.listFiles();

            if (dirs.length != 2) {
                resultString += "notOneSet pair" + String.valueOf(index) + "有问题，不是两个人" + "\n";
            } else {
                String picture1 = pairPath + dirs[0].getName() + "/" + dirs[0].listFiles()[0].getName();
                String picture2 = pairPath + dirs[1].getName() + "/" + dirs[1].listFiles()[0].getName();
                yourSelectedImage1 = BitmapFactory.decodeFile(picture1);
                yourSelectedImage2 = BitmapFactory.decodeFile(picture2);
            }
            if ((yourSelectedImage1 == null) || (yourSelectedImage2 == null)) {
                //Intent intent = new Intent(DoubleCalcSimActivity.this, FreeMatchActivity.class);
                //startActivity(intent);
                //finish();
                resultString += "notOneSet pair" + String.valueOf(index) + "图片缺失，没有两张图片" + "\n";
            } else {
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

                if (faceInfoSelected1[0] < 1) {
                    resultString += "notOneSet pair" + String.valueOf(index) + "图片１没有检测到人脸！\n";
                }
                if (faceInfoSelected2[0] < 1) {
                    resultString += "notOneSet pair" + String.valueOf(index) + "图片２没有检测到人脸！\n";
                } else if ((faceInfoSelected1[0] > 0) && (faceInfoSelected2[0] > 0)) {
                    feature1 = arcface.GetFeature(imageDate_selected1, width_selected1,
                            height_selected1, faceInfoSelected1);
                    feature2 = arcface.GetFeature(imageDate_selected2, width_selected2,
                            height_selected2, faceInfoSelected2);
                    similarity = arcface.NewCalcSimilarity(feature1, feature2);
                    similarity_notone_set[index-1] = similarity;

                }
            }
        }

        long notendtime = System.currentTimeMillis();
        resultString += "不是同一个人脸数据集相似度计算总耗时：" + String.valueOf(notendtime-notstarttime) + "ms" + "\n";

        long isstarttime = System.currentTimeMillis();

        for (int index = 1;index < 1501;index++) {

            String pairPath = isOneSetPath + "pair" + String.valueOf(index) + "/";

            File pairFile = new File(pairPath);
            File[] files = pairFile.listFiles();

            if (files.length != 2) {
                resultString += "isOneSet pair" + String.valueOf(index) + "有问题，不是两个人" + "\n";
            } else {
                String picture1 = pairPath + files[0].getName();
                String picture2 = pairPath + files[1].getName();
                yourSelectedImage1 = BitmapFactory.decodeFile(picture1);
                yourSelectedImage2 = BitmapFactory.decodeFile(picture2);
            }
            if ((yourSelectedImage1 == null) || (yourSelectedImage2 == null)) {
                //Intent intent = new Intent(DoubleCalcSimActivity.this, FreeMatchActivity.class);
                //startActivity(intent);
                //finish();
                resultString += "isOneSet pair" + String.valueOf(index) + "图片缺失，没有两张图片" + "\n";
            } else {
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

                if (faceInfoSelected1[0] < 1) {
                    resultString += "isOneSet pair" + String.valueOf(index) + "图片１没有检测到人脸！\n";
                }
                if (faceInfoSelected2[0] < 1) {
                    resultString += "isOneSet pair" + String.valueOf(index) + "图片２没有检测到人脸！\n";
                } else if ((faceInfoSelected1[0] > 0) && (faceInfoSelected2[0] > 0)) {
                    feature1 = arcface.GetFeature(imageDate_selected1, width_selected1,
                            height_selected1, faceInfoSelected1);
                    feature2 = arcface.GetFeature(imageDate_selected2, width_selected2,
                            height_selected2, faceInfoSelected2);
                    similarity = arcface.NewCalcSimilarity(feature1, feature2);
                    similarity_isone_set[index-1] = similarity;

                }
            }
        }

        long isendtime = System.currentTimeMillis();
        resultString += "同一个人脸数据集相似度计算总耗时：" + String.valueOf(isendtime-isstarttime) + "ms" + "\n";

        FileUtil.saveFloat1500ToFile(similarity_notone_set, notOneSetSimPath);
        FileUtil.saveFloat1500ToFile(similarity_isone_set, isOneSetSimPath);
        result.setText(resultString);
    }

}
