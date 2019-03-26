package com.dj.transmission.client.command.send;

import com.dj.transmission.file.TransmissionFileSectionInfo;

import java.util.List;

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
     * 处理回复的接收端接收消息
     * @param fileHash
     * @param sectionInfos
     */
    void handleReplyContinueFileInfo(String fileHash, List<TransmissionFileSectionInfo> sectionInfos);

    /**
     * 读取流被关闭了
     */
    void streamClose();
}
