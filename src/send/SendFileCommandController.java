package send;

import config.Configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Base64;

/**
 * 发送文件信息管理
 * 2端之间的通信命令如下
 *
 * type,data...
 * type为1则表示发送文件信息:                                1,Base64(文件名),文件大小,文件hash值
 * type为2则表示确认是否接收(data为0表示拒绝，1表示接收):       2,1
 * type为3则表示暂停或者开始任务(data为0表示暂停，1表示开始):   3,0
 */
public class SendFileCommandController {

    private Socket commandSocket;
    private DataOutputStream commandDataOutputStream;
    private DataInputStream commandDatainputStream;
    private String serverIp;
    private Configuration config;

    public SendFileCommandController(String serverIp, Configuration config) {
        this.serverIp = serverIp;
        this.config = config;
    }

    public void sendFileInfo(FileInfo file) {
        try {
            if (commandSocket == null)
                commandSocket = new Socket(serverIp, config.commandPort());
            if (commandDataOutputStream == null)
                commandDataOutputStream = new DataOutputStream(commandSocket.getOutputStream());
            if (commandDatainputStream == null)
                commandDatainputStream = new DataInputStream(commandSocket.getInputStream());
            // 发送文件信息
            String fileName = file.getFile().getName();
            String fileSize = String.valueOf(file.getFile().length());
            String fileHash = file.getFileHashValue();
            String data = "1," + Base64.getEncoder().encodeToString(fileName.getBytes("utf-8")) + "," + fileSize + "," + fileHash;
            commandDataOutputStream.writeUTF(data);
            commandDataOutputStream.flush();
            System.out.println("发送了文件信息 name：" + fileName + " size:" + fileSize + " hash:" + fileHash);
            // 收到回复信息
            String replyMsg = commandDatainputStream.readUTF();
            //分析回复信息
            analysisReplyMsg(replyMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分析回复信息
     * @param replyMsg
     */
    private void analysisReplyMsg(String replyMsg) {
        String[] split = replyMsg.split(",");
        if (split.length > 0) {
            switch (Integer.parseInt(split[0])) {
                case 2:
                    if (split.length == 2 && config.getListener() != null)
                        config.getListener().onAcceptListener(split[1].trim().equals("1"));
                    break;
            }
        }
    }
}
