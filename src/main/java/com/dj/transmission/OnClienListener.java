package com.dj.transmission;

import com.dj.transmission.client.command.receive.AcceptController;
import com.dj.transmission.file.TransmissionFileInfo;
import com.dj.transmission.client.TransmissionClient;

/**
 * 全局的事件回调
 */
public abstract class OnClienListener {
    /**
     * 当有文件传输过来的时候触发，控制是否接收文件
     * @param client
     * @param fileInfo
     * @param controller
     */
    public abstract void onReceiveFileInfo(TransmissionClient client, TransmissionFileInfo fileInfo, AcceptController controller);
}
