package com.dj.transmission.client.command.receive;

import com.dj.transmission.client.transmission.TransmissionState;

/**
 * 作为一个接收端的接口回调
 */
public abstract class OnReceiveClientListener {

    /**
     * 接收进度
     * @param progress
     */
    public void onProgress(double progress) {}

    /**
     * 开始或者暂停的状态改变
     * @param state
     */
    public void onStateChange(TransmissionState state) {}

}
