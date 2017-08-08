package com.xintu.speexechocanceller;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by tongchenfei on 2017/8/8.
 */

public class LtRecorder implements Runnable {

    private LtRecorder(){

    }

    private static LtRecorder instance = new LtRecorder();
    public static LtRecorder get(){
        return instance;
    }

    private AudioRecord audioRecord;
    private boolean isRunning = false;

    public void startRecord(){
        if (!isRunning) {
            isRunning = true;
            new Thread(this).start();
        }
    }

    public void stopRecord(){
        isRunning = false;
    }

    @Override
    public void run() {
        Log.d("LtRecorder", "prepare to Record: " + isRunning);
        int recordBuff = AudioRecord.getMinBufferSize(Constants.SAMPLE_RATE, AudioFormat
                .CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, Constants.SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, recordBuff);

        short[] rawData = new short[recordBuff];
        audioRecord.startRecording();
        while (isRunning) {
            Log.d("LtRecorder", "prepare to read: " + rawData.length);
            audioRecord.read(rawData, 0, recordBuff);
            Log.d("LtRecorder", "read: " + rawData.length);
            RawDataManager.get().addRawData(rawData);
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        Log.d("LtRecorder", "runnable is over");
    }
}
