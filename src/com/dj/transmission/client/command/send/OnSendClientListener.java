package com.dj.transmission.client.command.send;

public abstract class OnSendClientListener {

    /**
     * 作为发送端，接收端是否同意的回调
     * @param accept
     */
    public void onAccept(Boolean accept) {}

    /**
     * 是否发送完毕 onProgress的progress为1.0,就是发送完毕，感觉这个没必要
     * @param finish
     */
//    public void onFinish(Boolean finish) {}

    /**
     * 发送进度
     * @param progress
     */
    public void onProgress(double progress) {}

    /**
     * 开始或者暂停的状态改变
     * @param state
     */
    public void onStateChange(SendClientState state) {}

    enum SendClientState {
        START,
        PAUSE
    }
}
