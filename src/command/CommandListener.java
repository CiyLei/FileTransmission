package command;

import send.TransmissionFileInfo;

/**
 * 命令的监听
 */
public interface CommandListener {
    /**
     * 监听到有客户端要发送文件给我
     * @param transmissionFIleInfo
     */
    void onFileInfoListener(TransmissionFileInfo transmissionFIleInfo, AcceptController controller);

    /**
     * 监听到接收端是否同意接收文件的命令
     */
    void onAcceptListener(Boolean accept);

    /**
     * 监听到接收端是否开始或者暂停的命令
     * @param isStart
     */
    void onStartOrPauseListener(Boolean isStart);
}

