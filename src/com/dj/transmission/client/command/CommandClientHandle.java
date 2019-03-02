package com.dj.transmission.client.command;

public interface CommandClientHandle {
    /**
     * 作为发送端，得到了同意接收的答复
     * @param sendFilePort
     */
    void sendClientHandleStartCommand(Integer sendFilePort);

    /**
     * 作为接收端，同意了开始接收
     */
    void receiveClientHandleAccept();

    /**
     * 处理继续发送动作
     */
    void handleCommandContinue();
}
