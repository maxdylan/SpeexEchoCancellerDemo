package com.xintu.speexechocanceller;

import android.util.Log;

/**
 * Created by Tong on 2017/8/8.
 */

public class Speex {
    static {
        try {
            Log.e("JNI", "Trying to load libspeex.so");
            System.loadLibrary("speex");
        } catch (UnsatisfiedLinkError ule) {
            Log.e("JNI", "WARNING:Gould not load libspeex.so");
        }
    }

    /*
     * quality 1 : 4kbps (very noticeable artifacts, usually intelligible) 2 :
     * 6kbps (very noticeable artifacts, good intelligibility) 4 : 8kbps
     * (noticeable artifacts sometimes) 6 : 11kpbs (artifacts usually only
     * noticeable with headphones) 8 : 15kbps (artifacts not usually noticeable)
     */
    private static final int DEFAULT_COMPRESSION = 4;

    Speex() {

    }

    public void init() {
        // load();
        open(DEFAULT_COMPRESSION);
    }

    private void load() {
        try {
            System.loadLibrary("speex");
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public native int open(int compression);

    public native int getFrameSize();

    public native int decode(byte encoded[], short lin[], int size);

    public native int encode(short lin[], int offset, byte encoded[], int
            size, short echo[], boolean deEcho);

    public native void close();

    public native int initSpeexAec(int frameSize, int filterLength, int
            sampleRate);

    public native int speexAec(byte[] record, byte[] play, byte[] aec);

    public native int exitSpeexDsp();
}
