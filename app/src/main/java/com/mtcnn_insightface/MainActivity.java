package com.mtcnn_insightface;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

//import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create two dir
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        String dir_path1 = sdDir.toString() + "/.Mtcnn_insightface/";
        String dir_path2 = sdDir.toString() + "/.mtcnn/";
        if (!FileUtil.fileIsExists(dir_path1))
            FileUtil.newDirectory(dir_path1);
        if (!FileUtil.fileIsExists(dir_path2))
            FileUtil.newDirectory(dir_path2);

        //initialize
        verifyStoragePermissions(this);
        //拷贝模型到sd卡
        try {
            copyBigDataToSD("det1.bin");
            copyBigDataToSD("det2.bin");
            copyBigDataToSD("det3.bin");
            copyBigDataToSD("det1.param");
            copyBigDataToSD("det2.param");
            copyBigDataToSD("det3.param");
            copyBigDataToSD("mobilefacenet.param");
            copyBigDataToSD("mobilefacenet.bin");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button button_jump = (Button) findViewById(R.id.jump);
        button_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FaceRecognitionActivity.class);
                startActivity(intent);
            }
        });

        Button button_file = (Button) findViewById(R.id.file_management);
        button_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentFile = new Intent(MainActivity.this, FileOperationActivity.class);
                startActivity(intentFile);
            }
        });

        Button button_match = (Button) findViewById(R.id.free_match);
        button_match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                Intent intentMatch = new Intent(MainActivity.this, FreeMatchActivity.class);
                startActivity(intentMatch);
            }
        });

        Button button_test = (Button) findViewById(R.id.test);
        button_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                Intent intentTest = new Intent(MainActivity.this, TestDataSetActivity.class);
                startActivity(intentTest);
            }
        });
    }

    private void copyBigDataToSD(String strOutFileName) throws IOException {
        //Log.i(TAG, "start copy file " + strOutFileName);
        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        File file = new File(sdDir.toString()+"/.mtcnn/");
        if (!file.exists()) {
            file.mkdir();
        }

        String tmpFile = sdDir.toString()+"/.mtcnn/" + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            //Log.i(TAG, "file exists " + strOutFileName);
            return;
        }
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream(sdDir.toString()+"/.mtcnn/"+ strOutFileName);
        myInput = this.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        //Log.i(TAG, "end copy file " + strOutFileName);

    }

    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
