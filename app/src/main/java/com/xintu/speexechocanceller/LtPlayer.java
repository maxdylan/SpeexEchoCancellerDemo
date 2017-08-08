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

public class LtPlayer implements Runnable{
    private LtPlayer(){

    }

    private static LtPlayer instance = new LtPlayer();
    public static LtPlayer get(){
        return instance;
    }

    private AudioTrack audioTrack;
    public boolean isRunning = false;


    public void startPlay(){
        if (!isRunning) {
            isRunning = true;
            new Thread(this).start();
        }
    }

    public void stopPlay(){
        isRunning = false;
    }

    @Override
    public void run() {
        Log.d("LtPlayer", "prepare to play: " + isRunning);
        int playBuff = AudioTrack.getMinBufferSize(Constants.SAMPLE_RATE, AudioFormat
                .CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.SAMPLE_RATE, AudioFormat
                .CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, playBuff, AudioTrack
                .MODE_STREAM);
        audioTrack.play();
        short[] rawData;
        while (isRunning) {
            if (RawDataManager.get().canRead()) {
                rawData = RawDataManager.get().getRawData();
                Log.d("LtPlayer", "prepare to write: " + RawDataManager.get().rawSize());
                audioTrack.setStereoVolume(0.7f, 0.7f);
                audioTrack.write(rawData, 0, playBuff);
                Log.d("LtPlayer", "audioTrack write: " + rawData.length);
            }else{
                try {
                    Log.e("LtPlayer", "null to sleep");
                    Thread.sleep(1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        RawDataManager.get().clear();
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
        Log.d("LtPlayer", "player runnable is over");

    }
}
