package com.dj.transmission.adapter;

import java.util.concurrent.ExecutorService;

/**
 * 针对不同平台的适配类
 */
public interface TransmissionAdapter {

    /**
     * 判断是否在主线程
     *
     * @return
     */
    Boolean isMainThread();

    /**
     * 发送文件的线程池
     *
     * @return
     */
    ExecutorService sendFilePool();

    /**
     * 发送命令socket的线程池
     *
     * @return
     */
    ExecutorService commandPool();
}
