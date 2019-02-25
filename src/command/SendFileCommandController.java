package command;

import config.Configuration;
import client.Client;
import client.FileInfo;

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
    private Integer commandPort;
    private Configuration config;
    private Client client;

    public SendFileCommandController(String serverIp, Integer commandPort, Configuration config, Client client) {
        this.serverIp = serverIp;
        this.commandPort = commandPort;
        this.config = config;
        this.client = client;
//        connection();
    }

    public void connection() {
        try {
            if (commandSocket == null || commandSocket.isClosed())
                commandSocket = new Socket(serverIp, commandPort);
            if (commandDataOutputStream == null)
                commandDataOutputStream = new DataOutputStream(commandSocket.getOutputStream());
            if (commandDatainputStream == null)
            commandDatainputStream = new DataInputStream(commandSocket.getInputStream());
            for (Client.ClientListener listener : client.getListeners())
                listener.onConnection(true);
        } catch (IOException e) {
            e.printStackTrace();
            for (Client.ClientListener listener : client.getListeners())
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
                while (null != replyMsg && !replyMsg.isEmpty()) {
                    //分析回复信息
                    analysisReplyMsg(replyMsg);
                    replyMsg = commandDatainputStream.readUTF();
                }
            } catch (IOException e) {
//                e.printStackTrace();
                try {
                    commandDataOutputStream.close();
                    commandDatainputStream.close();
                    commandSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                commandDataOutputStream = null;
                commandDatainputStream = null;
                commandSocket = null;
                for (Client.ClientListener listener : client.getListeners())
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
                    if (split.length == 2 && config.getListener() != null) {
                        Boolean accept = split[1].trim().equals("1");
                        client.replyIsAccept(accept);
                    }
                    break;
            }
        }
    }
}