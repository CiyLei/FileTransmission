import config.Configuration;
import config.DefaultConfiguration;
import scan.BroadcastScan;
import scan.Scan;
import client.Client;
import client.SocketClient;
import command.AcceptController;
import command.CommandListener;
import command.CommandServerSocket;
import send.SendFileServerSocket;
import send.TransmissionFileInfo;

import java.io.IOException;

public class FileTransmission implements CommandListener {

    private Configuration configuration;
    private Scan broadcastScan;
    private CommandServerSocket commandServerSocket;
    private SendFileServerSocket sendFileServerSocket;

    public FileTransmission() throws IOException {
        this.configuration = new DefaultConfiguration(this);
        this.broadcastScan = new BroadcastScan(this.configuration);
        this.commandServerSocket = new CommandServerSocket(this.configuration);
        this.sendFileServerSocket = new SendFileServerSocket(this.configuration);
    }

    public FileTransmission(CommandListener listener) throws IOException {
        this(new DefaultConfiguration(listener));
    }

    public FileTransmission(Configuration configuration) throws IOException {
        this.configuration = configuration;
        this.broadcastScan = new BroadcastScan(configuration);
        this.commandServerSocket = new CommandServerSocket(configuration);
        this.sendFileServerSocket = new SendFileServerSocket(this.configuration);
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
     * @param hostAddress
     * @param hostName
     * @param commandPort
     * @return
     */
    public Client getNewSocketClient(String hostAddress, String hostName, Integer commandPort) {
        return new SocketClient(hostAddress, hostName, commandPort, configuration);
    }

    @Override
    public void onFileInfoListener(TransmissionFileInfo transmissionFIleInfo, AcceptController controller) {
        System.out.println("获取到了文件信息 name：" + transmissionFIleInfo.getFileName() + " size:" + transmissionFIleInfo.getFileSize() + " hash:" + transmissionFIleInfo.getFileHash());
        controller.accept();
        System.out.println("我同意接收这个文件 name：" + transmissionFIleInfo.getFileName() + " size:" + transmissionFIleInfo.getFileSize() + " hash:" + transmissionFIleInfo.getFileHash());
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
