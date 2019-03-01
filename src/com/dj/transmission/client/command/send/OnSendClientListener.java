package com.dj.transmission.client.command.send;

public abstract class OnSendClientListener {

    /**
     * 作为发送端，接收端是否同意的回调
     * @param accept
     */
    public void onAccept(Boolean accept) {}

    /**
     * 是否发送完毕
     * @param finish
     */
    public void onFinish(Boolean finish) {}

    /**
     * 发送进度
     * @param progress
     */
    public void onProgress(double progress) {}
}
