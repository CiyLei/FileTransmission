import config.Configuration;
import config.DefaultConfiguration;
import scan.BroadcastScan;
import scan.Scan;
import send.Client;
import send.SocketClient;
import server.AcceptController;
import server.CommandListener;
import server.CommandServerSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class FileTransmission implements CommandListener {

    private Configuration configuration;
    private Scan broadcastScan;
    private CommandServerSocket commandServerSocket;

    public FileTransmission() throws IOException {
        this.configuration = new DefaultConfiguration(this);
        this.broadcastScan = new BroadcastScan(this.configuration);
        this.commandServerSocket = new CommandServerSocket(this.configuration);
    }

    public FileTransmission(CommandListener listener) throws IOException {
        this(new DefaultConfiguration(listener));
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

    @Override
    public void onFileInfoListener(String fileName, Long fileSize, String fileHash, AcceptController controller) {
        System.out.println("获取到了文件信息 name：" + fileName + " size:" + fileSize + " hash:" + fileHash);
        controller.accept();
        System.out.println("我同意接收这个文件 name：" + fileName + " size:" + fileSize + " hash:" + fileHash);
    }

    @Override
    public void onAcceptListener(Boolean accept) {
        System.out.println("接收端" + (accept ? "" : "不") + "同意接收文件");
    }

    @Override
    public void onStartOrPauseListener(Boolean isStart) {
        System.out.println("接收端点了" + (isStart ? "开始" : "暂停"));
    }

}
