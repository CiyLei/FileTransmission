package send;

import config.Configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Base64;
import java.util.List;

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
    private Integer commandPort;
    private Configuration config;
    private List<Client.ClientListener> listeners;

    public SendFileCommandController(String serverIp, Integer commandPort, Configuration config, List<Client.ClientListener> listeners) {
        this.serverIp = serverIp;
        this.commandPort = commandPort;
        this.config = config;
        this.listeners = listeners;
        connection();
    }

    private void connection() {
        try {
            commandSocket = new Socket(serverIp, commandPort);
            commandDataOutputStream = new DataOutputStream(commandSocket.getOutputStream());
            commandDatainputStream = new DataInputStream(commandSocket.getInputStream());
            for (Client.ClientListener listener : listeners)
                listener.onConnection(true);
        } catch (IOException e) {
            e.printStackTrace();
            for (Client.ClientListener listener : listeners)
                listener.onConnection(false);
        }
    }

    public Boolean isConnection() {
        return commandSocket != null && commandSocket.isConnected() && commandDatainputStream != null && commandDataOutputStream != null;
    }

    public void sendFileInfo(FileInfo file) {
        if (!isConnection())
            connection();
        if (isConnection()) {
            try {
                // 发送文件信息
                String fileName = file.getFile().getName();
                String fileSize = String.valueOf(file.getFile().length());
                String fileHash = file.getFileHashValue();
                String data = "1," + Base64.getEncoder().encodeToString(fileName.getBytes("utf-8")) + "," + fileSize + "," + fileHash;
                commandDataOutputStream.writeUTF(data);
                commandDataOutputStream.flush();
                // 收到回复信息
                String replyMsg = commandDatainputStream.readUTF();
                //分析回复信息
                analysisReplyMsg(replyMsg);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    commandDataOutputStream.close();
                    commandDatainputStream.close();
                    commandSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                for (Client.ClientListener listener : listeners)
                    listener.onConnection(false);
            }
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
