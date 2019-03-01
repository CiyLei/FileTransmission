package com.dj.transmission.client.command;

public interface CommandClientHandle {
    /**
     * 处理真正的发送消息动作
     * @param sendFilePort
     */
    void handleCommandStart(Integer sendFilePort);

    /**
     * 处理继续发送动作
     */
    void handleCommandContinue();
}
