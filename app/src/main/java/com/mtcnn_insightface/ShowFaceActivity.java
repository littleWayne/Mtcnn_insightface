package com.mtcnn_insightface;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;

import static android.content.ContentValues.TAG;

public class ShowFaceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_face);
        Intent intent = getIntent();
        final String data = intent.getStringExtra("extra_data");

        File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        String dir_path = sdDir.toString() + "/.Mtcnn_insightface/";
        dir_path += "faculty" + data;
        Log.i(TAG, "path:" + dir_path);
        File dir_file = new File(dir_path);
        int jpg_num = FileUtil.getNumOfJpg(dir_file);
        String[] jpgName = new String[jpg_num];

        for(int i = 0;i < jpg_num;i++){
            jpgName[i] = String.valueOf(i + 1) + ".jpg";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                ShowFaceActivity.this, android.R.layout.simple_list_item_1, jpgName);
        ListView listView = (ListView) findViewById(R.id.list_face);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(ShowFaceActivity.this, FaceMapActivity.class);
                String Data[] = new String[2];
                Data[0] = "faculty" + data;
                Data[1] = String.valueOf(position + 1);
                intent.putExtra("extra_data", Data);
                startActivity(intent);
            }
        });
    }
}
