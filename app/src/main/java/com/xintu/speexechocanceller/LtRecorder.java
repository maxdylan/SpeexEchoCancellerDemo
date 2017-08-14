package com.xintu.speexechocanceller;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.android.webrtc.audio.MobileAEC;
import com.example.echo.EchoCanceller;

/**
 * Created by tongchenfei on 2017/8/8.
 */

public class LtRecorder implements Runnable {

    private LtRecorder() {

    }

    private static LtRecorder instance = new LtRecorder();

    public static LtRecorder get() {
        return instance;
    }

    private AudioRecord audioRecord;
    private boolean isRunning = false;

    public void startRecord() {
        if (!isRunning) {
            isRunning = true;
            new Thread(this).start();
        }
    }

    public void stopRecord() {
        isRunning = false;
    }

    private Speex speex = new Speex();
    private static int RAW_PACKAGE_SIZE = 160;
    private EchoCanceller sampleCanceller = new EchoCanceller();

    private MobileAEC mobileAEC = new MobileAEC(MobileAEC.SamplingFrequency
            .FS_8000Hz);

    @Override
    public void run() {
        Log.d("LtRecorder", "prepare to Record: " + isRunning);
        android.os.Process.setThreadPriority(android.os.Process
                .THREAD_PRIORITY_URGENT_AUDIO);

        int recordBuff = AudioRecord.getMinBufferSize(Constants.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.d("LtRecorder", "recordBuff: " + recordBuff);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                Constants.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, recordBuff);

        speex.init();
        speex.initSpeexAec(160, 4000, Constants.SAMPLE_RATE);

        Log.d("LtRecorder", " mobile aec prepare to prepare");
        mobileAEC.setAecmMode(MobileAEC.AggressiveMode.MOST_AGGRESSIVE)
                .prepare();
        Log.d("LtRecorder", " mobile aec end prepare");
//        sampleCanceller.open(Constants.SAMPLE_RATE, 160, 4000);
        short[] rawData = new short[RAW_PACKAGE_SIZE];
        short[] echoCancelData = new short[RAW_PACKAGE_SIZE];
        byte[] speexEncode = new byte[1024];
        audioRecord.startRecording();
        while (isRunning) {
            audioRecord.read(rawData, 0, RAW_PACKAGE_SIZE);
            if (RawDataManager.get().getEchoData() != null) {

//                speex.speexAec(rawData, RawDataManager.get().getEchoData(),
//                        echoCancelData);
                try {
                    mobileAEC.farendBuffer(RawDataManager.get().getEchoData()
                            , RAW_PACKAGE_SIZE);
//                    mobileAEC.farendBuffer(rawData, RAW_PACKAGE_SIZE);
                    mobileAEC.echoCancellation(rawData, null, echoCancelData,
                            (short) 160, (short) 160);

                } catch (Exception e) {
                    Log.e("LtRecorder", "mobile aec exception");
                    e.printStackTrace();
                }
//                echoCancelData = sampleCanceller.process(rawData,
//                        RawDataManager.get().getEchoData());

                int iSize = speex.encode(echoCancelData, 0, speexEncode, 0);
                RawDataManager.get().addSpeexData(speexEncode, iSize);

//                RawDataManager.get().addRawData(echoCancelData);
            } else {
                int iSize = speex.encode(rawData, 0, speexEncode, 0);
                RawDataManager.get().addSpeexData(speexEncode, iSize);

//                RawDataManager.get().addRawData(rawData);
            }
        }
        mobileAEC.close();
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
//        speex.exitSpeexDsp();
        Log.d("LtRecorder", "runnable is over");
    }
}
