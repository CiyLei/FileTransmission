package client;

import command.SendFileCommandSocket;
import config.Configuration;
import send.SendFileSocketController;
import send.TransmissionFileInfo;
import utils.MD5Util;

import java.io.File;

/**
 * 采用socket方式连接设备和发送数据
 */
public class SocketClient extends Client {

    // 命令管理socket
    private SendFileCommandSocket sendFileInfoController;
    private SendFileSocketController sendFileSocketController;
    private Configuration config;
    // 对方的端口号
    private Integer sendFilePort;

    /**
     * Socket对象
     * @param hostAddress   对方ip地址
     * @param hostName      对方名称
     * @param commandPort   对方command的端口
     * @param configuration 自己的配置信息
     */
    public SocketClient(String hostAddress, String hostName, Integer commandPort, Configuration configuration) {
        super(hostAddress, hostName, commandPort, configuration);
        this.config = configuration;
        sendFileInfoController = new SendFileCommandSocket(hostAddress, commandPort, configuration, this);
    }

    @Override
    public void sendFile(File file) {
        if (file.exists()) {
            analysisFile(file);
        }
    }

    @Override
    public void startSendFile() {
        config.commandPool().execute(new Runnable() {
            @Override
            public void run() {
                if (sendFileInfoController != null)
                    sendFileInfoController.sendStartMessage(sendFile.getFileHashValue());
            }
        });
    }

    @Override
    public void pauseSendFile() {
        if (sendFileSocketController != null)
            isSead = false;
    }

    /**
     * 分析文件（主要是获取文件的hash值）
     * @param file
     */
    private void analysisFile(File file) {
        configuration.commandPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String hash = MD5Util.md5HashCode(file.getPath());
                    sendFile = new FileInfo(file, hash);
                    // 发送文件信息给接收端
                    sendFileInfoController.sendFileMessage(sendFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public FileInfo getFileInfo() {
        return sendFile;
    }

    @Override
    public void connection() {
        sendFileInfoController.connection();
    }

    /**
     * 回复是否同意接收文件
     * @param accept
     * @return
     */
    @Override
    public void replyIsAccept(Boolean accept, Integer sendFilePort) {
        config.getListener().onAcceptListener(accept);
        if (accept) {
            this.sendFilePort = sendFilePort;
            sendFileData();
        }
    }

    @Override
    public void receiveFileUpdate(TransmissionFileInfo transmissionFileInfo) {
        receiveTransmissionFileInfo = transmissionFileInfo;
        for (Client.ClientListener listener : listeners)
            listener.onReceiveFileUpdate();
    }

    @Override
    public void sendFileUpdate(TransmissionFileInfo transmissionFileInfo) {
        sendTransmissionFileInfo = transmissionFileInfo;
        for (Client.ClientListener listener : listeners)
            listener.onSendFileUpdate();
    }

    /**
     * 开始发送文件
     */
    private void sendFileData() {
        isSead = true;
        if (sendFilePort != null && sendFile != null && !sendFile.getFileHashValue().isEmpty()) {
            sendFileSocketController = new SendFileSocketController(config, sendFile, getHostAddress(), sendFilePort);
            sendFileSocketController.start();
        }
    }

    @Override
    public void continumSendFileData() {
        isSead = true;
        if (sendFileSocketController != null)
            sendFileSocketController.start();
    }
}
