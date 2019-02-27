package command;

import config.Configuration;
import client.Client;
import client.FileInfo;

import java.io.IOException;
import java.net.Socket;

/**
 * 发送端命令Socket的管理类
 * 这里将开启两个线程
 * SendFileCommandSocketWrite 负责发送端分析回复的信息
 * SendFileCommandSocketRead  负责发送端主动发送的信息
 */
public class SendFileCommandSocket {

    private Socket commandSocket;
    private String serverIp;
    private Integer commandPort;
    private Configuration config;
    private Client client;
    private SendFileCommandSocketWrite commandSocketWrite;
    private SendFileCommandSocketRead commandSocketRead;

    public SendFileCommandSocket(String serverIp, Integer commandPort, Configuration config, Client client) {
        this.serverIp = serverIp;
        this.commandPort = commandPort;
        this.config = config;
        this.client = client;
    }

    public void connection() {
        try {
            if (commandSocket == null || commandSocket.isClosed())
                commandSocket = new Socket(serverIp, commandPort);
            for (Client.ClientListener listener : client.getListeners())
                listener.onConnection(true);
            // 当commandSocket连接上了socket，则针对这个socket开启两个线程，一个死循环读消息专门回复消息，一个主动发送消息
            commandSocketRead = new SendFileCommandSocketRead(commandSocket, client, config);
            config.commandPool().execute(commandSocketRead);
            commandSocketWrite = new SendFileCommandSocketWrite(commandSocket, config);
            config.commandPool().execute(commandSocketWrite);
        } catch (IOException e) {
            e.printStackTrace();
            for (Client.ClientListener listener : client.getListeners())
                listener.onConnection(false);
        }
    }

    public Boolean isConnection() {
        return commandSocket != null && commandSocket.isConnected();
    }

    public void sendFileInfo(FileInfo file) {
        if (!isConnection())
            connection();
        if (isConnection()) {
            commandSocketWrite.sendFileInfo(file);
        }
    }

}
