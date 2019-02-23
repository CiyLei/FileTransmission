import config.Configuration;
import config.DefaultConfiguration;
import scan.BroadcastScan;
import scan.Scan;
import send.Client;
import send.SocketClient;

import java.net.SocketException;

public class FileTransmission {

    private Configuration configuration;
    private Scan broadcastScan;

    public FileTransmission() throws SocketException {
        this.configuration = new DefaultConfiguration();
        this.broadcastScan = new BroadcastScan(configuration);
    }

    public FileTransmission(Configuration configuration) throws SocketException {
        this.configuration = configuration;
        this.broadcastScan = new BroadcastScan(configuration);
    }

    /**
     * 获取扫描器
     * @return
     */
    public Scan getBroadcastScan() {
        return broadcastScan;
    }

    /**
     * 如果需要自己指定ip发送文件的话，用这个方法获取client对象再发送文件
     * @param ip
     * @return
     */
    public Client getSocketClient(String ip) {
        return new SocketClient(ip, configuration);
    }
}
