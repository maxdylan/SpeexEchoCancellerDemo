package com.xintu.speexechocanceller;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by tongchenfei on 2017/8/8.
 */

public class LtPlayer implements Runnable {
    private LtPlayer() {

    }

    private static LtPlayer instance = new LtPlayer();

    public static LtPlayer get() {
        return instance;
    }

    private AudioTrack audioTrack;
    public boolean isRunning = false;


    public void startPlay() {
        if (!isRunning) {
            isRunning = true;
            new Thread(this).start();
        }
    }

    public void stopPlay() {
        isRunning = false;
    }

    private Speex speex = new Speex();

    @Override
    public void run() {
        Log.d("LtPlayer", "prepare to play: " + isRunning);
        int playBuff = AudioTrack.getMinBufferSize(Constants.SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Constants
                .SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat
                .ENCODING_PCM_16BIT, playBuff, AudioTrack.MODE_STREAM);
//        audioTrack.play();
        speex.init();
        short[] rawData;
        byte[] speexEncode;
        while (isRunning) {
            if (RawDataManager.get().canRead()) {
//                rawData = RawDataManager.get().getRawData();

                rawData = new short[1024];
                speexEncode = RawDataManager.get().getSpeexData();
                int iSize = speex.decode(speexEncode, rawData, speexEncode
                        .length);

                audioTrack.write(rawData, 0, iSize);
                audioTrack.play();
                if (RawDataManager.get().isEchoCancel()) {
                    RawDataManager.get().addEchoData(rawData, iSize);
                }
            }
//            else {
//                try {
//                    Log.e("LtPlayer", "null to sleep");
//                    Thread.sleep(10);
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
        RawDataManager.get().clear();
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
        Log.d("LtPlayer", "player runnable is over");

    }
}
