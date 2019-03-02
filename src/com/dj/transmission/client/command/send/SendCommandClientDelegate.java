package com.dj.transmission.client.command.send;

import com.dj.transmission.file.TransmissionFileInfo;

import java.io.File;
import java.util.List;

public interface SendCommandClientDelegate {

    /**
     * 连接
     */
    void connection();

    /**
     * 发送文件
     * @param filem
     */
    void sendFile(File filem);

    /**
     * 获取发送的文件的信息
     * @return
     */
    TransmissionFileInfo getSendFileInfo();

    /**
     * 继续发送
     */
    void continueSend();

    /**
     * 添加作为发送端的回调
     * @param listener
     */
    void addOnSendClientListener(OnSendClientListener listener);

    /**
     * 删除回调
     * @param listener
     */
    void removeOnSendClientListener(OnSendClientListener listener);

    /**
     * 获取回调
     */
    List<OnSendClientListener> getOnSendClientListener();

    /**
     * 关闭连接
     */
    void close();
}
