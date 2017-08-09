package com.xintu.speexechocanceller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button echoCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LtRecorder.get().startRecord();
                LtPlayer.get().startPlay();
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LtRecorder.get().stopRecord();
                LtPlayer.get().stopPlay();
//                if (LtPlayer.get().isRunning) {
//                    LtPlayer.get().stopPlay();
//                }else{
//                    LtPlayer.get().startPlay();
//                }
            }
        });

        echoCancel = (Button) findViewById(R.id.echoCancel);
        echoCancel.setText("不消除");

        echoCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RawDataManager.get().setEchoCancel(!RawDataManager.get()
                        .isEchoCancel());
                echoCancel.setText(RawDataManager.get().isEchoCancel() ?
                        "回声消除" : "不消除");
            }
        });

    }

}
