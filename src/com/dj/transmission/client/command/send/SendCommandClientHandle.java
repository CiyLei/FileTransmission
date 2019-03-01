package com.dj.transmission.client.command.send;

/**
 * 作为发送端任何处理回复的学习
 */
public interface SendCommandClientHandle {
    /**
     * 处理回复的是否接收文件的信息
     * @param accept
     * @param sendFilePort
     */
    void handleReplyAccept(Boolean accept, Integer sendFilePort);

    /**
     * 读取流被关闭了
     */
    void streamClose();
}
