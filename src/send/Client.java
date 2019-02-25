package send;

import config.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户端的一个抽象，用此对象进行文件的发送
 */
public abstract class Client {

    private String hostAddress;
    private String hostName;
    private Integer commandPort;
    Configuration configuration;
    List<ClientListener> listeners;

    public Client(String hostAddress, String hostName, Integer commandPort, Configuration configuration) {
        this.hostAddress = hostAddress;
        this.hostName = hostName;
        this.commandPort = commandPort;
        this.configuration = configuration;
        listeners = new ArrayList<>();
    }

    public abstract void sendFile(File file);

    public String getHostAddress() {
        return hostAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public Integer getCommandPort() {
        return commandPort;
    }

    public void addListener(ClientListener listener) {
        this.listeners.add(listener);
    }

    public abstract Boolean isConnection();

    public abstract void connection();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Client) {
            return hostAddress.equals(((Client) obj).hostAddress) && commandPort.equals(((Client) obj).commandPort);
        }
        return super.equals(obj);
    }

    /**
     * 传输中的回调
     */
    public abstract static class ClientListener {
        /**
         * 是否连接成功
         * @param connection
         */
        public void onConnection(Boolean connection) {}
    }
}

/**
 * 传输中的状态
 */
enum TransmissionState {
    /**
     * 分析文件中（主要是获取文件hash值）
     */
    ANALYSIS,
    /**
     * 传输中
     */
    TRANSMISSIONDING,
    /**
     * 传输暂停
     */
    PAUSE,
    /**
     * 传输完毕
     */
    FINISH,
    /**
     * 传输错误
     */
    ERROR
}
