import config.Configuration;
import config.DefaultConfiguration;
import scan.BroadcastScan;
import scan.Scan;
import send.Client;
import send.SocketClient;
import server.CommandServerSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class FileTransmission {

    private Configuration configuration;
    private Scan broadcastScan;
    private CommandServerSocket commandServerSocket;

    public FileTransmission() throws IOException {
        this(new DefaultConfiguration());
    }

    public FileTransmission(Configuration configuration) throws IOException {
        this.configuration = configuration;
        this.broadcastScan = new BroadcastScan(configuration);
        this.commandServerSocket = new CommandServerSocket(configuration);
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
    public Client getNewSocketClient(String ip) {
        try {
            return new SocketClient(InetAddress.getByName(ip), configuration);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
