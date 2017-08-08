package com.xintu.speexechocanceller;

import java.util.LinkedList;

/**
 * Created by tongchenfei on 2017/8/8.
 */

public class RawDataManager {
    private RawDataManager(){
        raws = new LinkedList<>();
    }
    private static RawDataManager instance = new RawDataManager();
    public static RawDataManager get(){
        return instance;
    }

    private LinkedList<short[]> raws;

    public void addRawData(short[] raw) {
        short[] buff = raw.clone();
        raws.add(buff);
    }

    public short[] getRawData(){
        if (raws.size() > 0) {
            return raws.pop();
        }else{
            return null;
        }
    }

    public void clear(){
        raws.clear();
    }

    public boolean canRead(){
        return raws.size() > 0;
    }

    public int rawSize(){
        return raws.size();
    }
}
