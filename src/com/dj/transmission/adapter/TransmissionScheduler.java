package com.dj.transmission.adapter;

/**
 * 回调函数运行类
 */
public class TransmissionScheduler {

    /**
     * 如果在Android平台可以做回到主线程的操作
     * @param scheduler
     */
    public void run(Runnable scheduler) {
        scheduler.run();
    }
}
