package com.xintu.speexechocanceller;

import java.util.LinkedList;

/**
 * Created by tongchenfei on 2017/8/8.
 */

public class RawDataManager {
    private RawDataManager() {
        raws = new LinkedList<>();
        speexs = new LinkedList<>();
    }

    private static RawDataManager instance = new RawDataManager();

    public static RawDataManager get() {
        return instance;
    }

    private short[] echo;

    private LinkedList<short[]> raws;

    private LinkedList<byte[]> speexs;

    public void addRawData(short[] raw) {
        short[] buff = raw.clone();
        raws.add(buff);
    }

    public short[] getRawData() {
        if (raws.size() > 0) {
            return raws.pop();
        } else {
            return null;
        }
    }

    public void clear() {
        raws.clear();
    }

    public boolean canRead() {
        return raws.size() > 0 || speexs.size() > 0;
    }

    public int rawSize() {
        return raws.size();
    }

    public void addEchoData(short[] echoData, int size) {
//        echo = echoData.clone();
        echo = new short[size];
        System.arraycopy(echoData, 0, echo, 0, size);
    }

    public short[] getEchoData() {
        return echo;
    }

    public void addSpeexData(byte[] speexData, int size) {
        byte[] buff = new byte[size];
        System.arraycopy(speexData, 0, buff, 0, size);
        speexs.add(buff);
    }

    public byte[] getSpeexData() {
        if (speexs.size() > 0) {
            return speexs.pop();
        } else {
            return null;
        }
    }

    public void clearSpeexs() {
        speexs.clear();
    }

    private boolean isEchoCancel = false;

    public boolean isEchoCancel() {
        return isEchoCancel;
    }

    public void setEchoCancel(boolean echoCancel) {
        isEchoCancel = echoCancel;
        if (!isEchoCancel) {
            echo = null;
        }
    }
}
