package com.mtcnn_insightface;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;

//import static android.content.ContentValues.TAG;

public class FileOperationActivity extends AppCompatActivity {
    
    File sdDir = Environment.getExternalStorageDirectory();//获取跟目录
    String dir_path = sdDir.toString() + "/.Mtcnn_insightface/";
    File dir_file = new File(dir_path);
    private int dir_num = FileUtil.getNumOfDir(dir_file);
    private String[] faculty = new String[dir_num];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_operation);
        for(int i = 0;i < dir_num;i++){
            faculty[i] = "faculty" + String.valueOf(i + 1);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                FileOperationActivity.this, android.R.layout.simple_list_item_1, faculty);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(FileOperationActivity.this, ShowFaceActivity.class);
                intent.putExtra("extra_data", String.valueOf(position + 1));
                startActivity(intent);
            }
        });
    }
}
