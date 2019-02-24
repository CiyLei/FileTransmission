package send;

import config.Configuration;

import java.io.File;
import java.net.InetAddress;

/**
 * 采用socket方式连接设备和发送数据
 */
public class SocketClient extends Client {

    private FileInfo sendFile;
    private double sendProgress;
    private TransmissionState sendState;
    // 命令管理socket
    private CommandManager commandManager;

    public SocketClient(InetAddress inetAddress, Configuration configuration) {
        super(inetAddress, configuration);
        addListener(new ClientListener() {
            @Override
            void onProgress(double progress) {
                sendProgress = progress;
            }

            @Override
            void onStateChange(TransmissionState state) {
                sendState = state;
            }
        });

    }

    @Override
    void sendFile(File file) {
        if (sendState == null || sendState == TransmissionState.FINISH) {
            analysisFile(file);
        }
    }

    /**
     * 分析文件（主要是获取文件的hash值）
     * @param file
     */
    private void analysisFile(File file) {
        for (Client.ClientListener listener : listeners)
            listener.onStateChange(TransmissionState.ANALYSIS);

        // 发送文件信息给接收端

        // 等待接收端回应的回调

    }

    public FileInfo getFileInfo() {
        return sendFile;
    }

    public double getProgress() {
        return sendProgress;
    }

    public TransmissionState getState() {
        return sendState;
    }
}
