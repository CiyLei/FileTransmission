package send;

import client.FileInfo;
import config.Configuration;

public class SendFileSocketController {
    private Configuration config;
    private FileInfo fileInfo;
    private Integer sendFilePort;

    public SendFileSocketController(Configuration config, FileInfo fileInfo, Integer sendFilePort) {
        this.config = config;
        this.fileInfo = fileInfo;
        this.sendFilePort = sendFilePort;
        System.out.println("发送文件的端口号：" + sendFilePort);
    }

    public void start() {

    }
}
