package send;

import config.Configuration;
import utils.MD5Util;

import java.io.File;

/**
 * 采用socket方式连接设备和发送数据
 */
public class SocketClient extends Client {

    private FileInfo sendFile;
//    private double sendProgress;
//    private TransmissionState sendState;
    // 命令管理socket
    private SendFileCommandController sendFileInfoController;

    /**
     * Socket对象
     * @param hostAddress   对方ip地址
     * @param hostName      对方名称
     * @param commandPort   对方command的端口
     * @param configuration 自己的配置信息
     */
    public SocketClient(String hostAddress, String hostName, Integer commandPort, Configuration configuration) {
        super(hostAddress, hostName, commandPort, configuration);
//        addListener(new ClientListener() {
//            @Override
//            void onProgress(double progress) {
//                sendProgress = progress;
//            }
//
//            @Override
//            void onStateChange(TransmissionState state) {
//                sendState = state;
//            }
//        });
        sendFileInfoController = new SendFileCommandController(hostAddress, commandPort, configuration);
    }

    @Override
    public void sendFile(File file) {
        if (file.exists()) {
//            if (sendState == null || sendState == TransmissionState.FINISH || sendState == TransmissionState.ERROR) {
//                analysisFile(file);
//            }
            analysisFile(file);
        }
    }

    /**
     * 分析文件（主要是获取文件的hash值）
     * @param file
     */
    private void analysisFile(File file) {
//        for (Client.ClientListener listener : listeners)
//            listener.onStateChange(TransmissionState.ANALYSIS);
        configuration.commandPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String hash = MD5Util.md5HashCode(file.getPath());
                    sendFile = new FileInfo(file, hash);
                    // 发送文件信息给接收端
                    sendFileInfoController.sendFileInfo(sendFile);
                } catch (Exception e) {
                    e.printStackTrace();
//                    for (Client.ClientListener listener : listeners)
//                        listener.onStateChange(TransmissionState.ERROR);
                }
            }
        });

    }

    public FileInfo getFileInfo() {
        return sendFile;
    }

//    public double getProgress() {
//        return sendProgress;
//    }
//
//    public TransmissionState getState() {
//        return sendState;
//    }
}
