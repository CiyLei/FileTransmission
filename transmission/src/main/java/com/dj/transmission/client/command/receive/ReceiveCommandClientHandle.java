package com.dj.transmission.client.command.receive;

public interface ReceiveCommandClientHandle {
    /**
     * 处理传输过来的文件信息
     * @param fileName
     * @param fileSize
     * @param fileHash
     */
    void handleFileInfoCommand(String fileName, Long fileSize, String fileHash, Integer commandPort);

    /**
     * 处理查看当前接收情况
     * @param fileHash
     */
    void handleReceiveFileInfoCommand(String fileHash);

    /**
     * 读取流被关闭了
     */
    void streamClose();
}
