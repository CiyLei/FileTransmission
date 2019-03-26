package com.dj.transmission.client.command.receive;

import com.dj.transmission.file.TransmissionFileInfo;

public interface ReceiveCommandClientDelegate {

    /**
     * 获取接收的文件的信息
     * @return
     */
    TransmissionFileInfo getReceiveFileInfo();

    /**
     * 继续接收
     */
    void continueReceive();

    /**
     * 关闭连接
     */
    void close();
}
