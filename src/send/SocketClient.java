package send;

import config.Configuration;

import java.io.File;
import java.net.InetAddress;

/**
 * 采用socket方式连接设备和发送数据
 */
public class SocketClient extends Client {

    private File sendFile;
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
        if (!file.exists())
            return;
        this.sendFile = file;
    }

    public File getFile() {
        return sendFile;
    }

    public double getProgress() {
        return sendProgress;
    }

    public TransmissionState getState() {
        return sendState;
    }
}
