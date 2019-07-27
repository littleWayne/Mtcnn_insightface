package com.mtcnn_insightface;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;

//import static android.content.ContentValues.TAG;


public class FaceMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_map);
        ImageView face_view = (ImageView) findViewById(R.id.show_face);
        Button delete_button = (Button) findViewById(R.id.delete_face);
        TextView path = (TextView) findViewById(R.id.path);

        final Intent intent = getIntent();
        String data[] = new String[2];
        data = intent.getStringArrayExtra("extra_data");
        final String facultyString = data[0];

        final File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        final String jpg_path = sdDir.toString() + "/.Mtcnn_insightface/" + data[0] + "/" + data[1] + ".jpg";
        final String txt_path = sdDir.toString() + "/.Mtcnn_insightface/" + data[0] + ".txt";
        final String dir_path = sdDir.toString() + "/.Mtcnn_insightface/" + data[0] + "/";

        File outputImage = new File(jpg_path);
        Uri imageUri;
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(FaceMapActivity.this, "com.mtcnn_insightface.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        Bitmap bitmap = null;
        try{
            bitmap = decodeUri(imageUri);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        Bitmap face = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        path.setText("文件路径：" + jpg_path);
        face_view.setImageBitmap(face);

        final int jpgNumber = Integer.valueOf(data[1]);

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FileUtil.fileIsExists(jpg_path)){
                    File face = new File(jpg_path);
                    face.delete();
                    deleteSpecificVector(txt_path, jpgNumber);
                    File dir_file = new File(dir_path);
                    int jpgNum = FileUtil.getNumOfJpg(dir_file);

                    if (jpgNum > 0) {
                        reSortJpgName(dir_path, jpgNumber);
                        Intent intent = new Intent(FaceMapActivity.this, FileOperationActivity.class);
                        startActivity(intent);
                        //finish();
                    }
                    //如果此时该人员已空，还要删除该文件夹然后重排，跳到更上层
                    else{
                        dir_file.delete();
                        String parent_path = sdDir.toString() + "/.Mtcnn_insightface/";
                        reSortFaculty(facultyString, parent_path);
                        reSortTxt(facultyString, parent_path);
                        //释放返回栈到MainActivity
                        //不让Main被再次创建，设置Main的模式
                        Intent intent1 = new Intent(FaceMapActivity.this, MainActivity.class);
                        startActivity(intent1);
                        //finish();
                    }
                }else{
                }
            }
        });

    }

    private void reSortJpgName(String dirPath, int num){
        File dir_file = new File(dirPath);
        File[] files = dir_file.listFiles();

        for (File f2 : files) {
            if (f2.isFile()) {
                if (f2.getName().endsWith(".jpg")) {
                    //Log.i(TAG, f2.getName());
                    String number = f2.getName().replace(".jpg", "");
                    if (Integer.valueOf(number) > num){
                        String newName = dirPath + String.valueOf(Integer.valueOf(number) - 1) + ".jpg";
                        String oldName = dirPath + number + ".jpg";
                        FileUtil.copyFile(oldName, newName);
                        File oldFile = new File(oldName);
                        oldFile.delete();
                    }
                }
            }
        }

    }

    private void reSortTxt(String facultyNum, String parentPath){
        String num = facultyNum.replace("faculty", "");
        File parent_file = new File(parentPath);
        File[] files = parent_file.listFiles();

        for (File f2 : files) {
            if (f2.isFile()) {
                if (f2.getName().endsWith(".txt")) {
                    String number = (f2.getName().replace(".txt", "")).replace("faculty", "");
                    if (Integer.valueOf(number) > Integer.valueOf(num)) {
                        String newName = parentPath + "faculty" + String.valueOf(Integer.valueOf(number) - 1) + ".txt";
                        String oldName = parentPath + "faculty" + number + ".txt";
                        FileUtil.copyFile(oldName, newName);
                        File oldFile = new File(oldName);
                        oldFile.delete();
                    }
                }
            }
        }
    }

    private void reSortFaculty(String facultyNum, String parentPath){
        String num = facultyNum.replace("faculty", "");
        File parent_file = new File(parentPath);
        File[] files = parent_file.listFiles();

        for (File f2 : files) {
            if (f2.isDirectory()) {
                String number = f2.getName().replace("faculty", "");
                if (Integer.valueOf(number) > Integer.valueOf(num)) {
                    String newName = parentPath + "faculty" + String.valueOf(Integer.valueOf(number) - 1);
                    String oldName = parentPath + "faculty" + number;
                    //Log.i(TAG, "old name:" + oldName);
                    //Log.i(TAG, "new name:" + newName);
                    FileUtil.copyFolder(oldName, newName);
                    File oldFile = new File(oldName);
                    FileUtil.deleteDirectory(oldFile);
                }
            }
        }

    }

    private void deleteSpecificVector(String file, int num){
        int line_num = FileUtil.getLineOfTxt(file);
        //Log.i(TAG, "line of txt:" + String.valueOf(line_num));
        float[][] Float = new float[line_num][128];
        for (int i = 0;i < line_num;i++){
            Float[i] = FileUtil.getFloatFromFile(file, (i+1));
        }
        float[][] FloatNew = new float[line_num - 1][128];
        FloatNew = remove(Float, line_num, num);//maybe wrong
        //把file先删掉再写
        if(FileUtil.fileIsExists(file)){
            File txt = new File(file);
            txt.delete();
        }else {
            //Log.i(TAG, file + " not exists!");
        }
        for (int j = 0;j < line_num -1;j++){
            FileUtil.saveFloatToFile(FloatNew[j], file);
        }
    }

    private float[][] remove(float[][] src, int num, int seq){
        float[][] out = new float[num - 1][128];
        int j = 0;
        for (int i = 0;i < num;i++){
            if ((i + 1) != seq){
                out[j] = src[i];
                j += 1;
            }
        }
        return out;
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
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }
}
