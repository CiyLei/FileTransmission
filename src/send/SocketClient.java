package send;

import config.Configuration;

import java.io.File;

/**
 * 采用socket方式连接设备和发送数据
 */
public class SocketClient extends Client {

    public SocketClient(String ip, Configuration configuration) {
        super(ip, configuration);
    }

    @Override
    void sendFile(File file) {

    }
}
