package com.xintu.speexechocanceller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LtRecorder.get().startRecord();
//                LtPlayer.get().startPlay();
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LtRecorder.get().stopRecord();
//                LtPlayer.get().stopPlay();
                if (LtPlayer.get().isRunning) {
                    LtPlayer.get().stopPlay();
                }else{
                    LtPlayer.get().startPlay();
                }
            }
        });

    }

}
