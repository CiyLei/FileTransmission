package send;

import config.Configuration;

import java.io.File;

/**
 * 客户端的一个抽象，用此对象进行文件的发送
 */
public abstract class Client {

    String ip;
    Configuration configuration;

    public Client(String ip, Configuration configuration) {
        this.ip = ip;
        this.configuration = configuration;
    }

    abstract void sendFile(File file);

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
