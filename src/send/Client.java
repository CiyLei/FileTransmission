package send;

import config.Configuration;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户端的一个抽象，用此对象进行文件的发送
 */
public abstract class Client {

    InetAddress inetAddress;
    Configuration configuration;
    List<ClientListener> listeners;

    public Client(InetAddress inetAddress, Configuration configuration) {
        this.inetAddress = inetAddress;
        this.configuration = configuration;
        listeners = new ArrayList<>();
    }

    public abstract void sendFile(File file);

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void addListener(ClientListener listener) {
        this.listeners.add(listener);
    }

    /**
     * 传输中的回调
     */
    public abstract class ClientListener {
        /**
         * 接收端是否同意
         * @param accept
         */
        void onAccept(Boolean accept) {}

        /**
         * 发送进度
         * @param progress
         */
        void onProgress(double progress) {}

        /**
         * 开始传输的过程中改变状态的回调
         * @param state
         */
        void onStateChange(TransmissionState state) {}
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Client) {
            return inetAddress.getHostAddress().equals(((Client) obj).inetAddress.getHostAddress());
        }
        return super.equals(obj);
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
