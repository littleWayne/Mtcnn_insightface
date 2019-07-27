package com.mtcnn_insightface;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import static android.content.ContentValues.TAG;

public class FreeMatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_match);

        Button button_single = (Button) findViewById(R.id.single);
        button_single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "correct open single match!");
                Intent intentSingle = new Intent(FreeMatchActivity.this, SingleMatchActivity.class);
                startActivity(intentSingle);
            }
        });

        Button match = (Button) findViewById(R.id.match);
        match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                Intent intentMatchSim = new Intent(FreeMatchActivity.this, DoubleCalcSimActivity.class);
                startActivity(intentMatchSim);
            }
        });
    }
}
