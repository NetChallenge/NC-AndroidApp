package com.vuforia.samples.encoder;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsyncThread<T> extends Thread {
    //raw image frames
    protected List<T> dataList = Collections.synchronizedList(new ArrayList());
    private boolean isClear = false;

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            T data = null;
            synchronized (dataList) {
                if(dataList.size() == 0)
                    try {
                        dataList.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                if(Thread.currentThread().isInterrupted())
                    break;
                if(dataList.size() != 0) {
                    data = dataList.get(0);
                    dataList.remove(0);
                }
            }

            if(data != null)
                innerProcess(data);
        }
        Log.d("AsyncThread", "AsyncThread end");
    }

    public void putData(T data) {
        synchronized (dataList) {
            if(!isClear) {
                dataList.add(data);
                dataList.notify();
            }
            else
                isClear = false;
        }
    }

    public synchronized void clear() {
        synchronized (dataList) {
            dataList.clear();
            isClear = true;
        }
    }

    public synchronized void release() {
        clear();
        interrupt();
        synchronized (dataList) {
            dataList.notify();
        }
    }

    private synchronized void innerProcess(T data) {
        if(!isClear)
            process(data);
    }

    protected void process(T data) {}
}