package command;

import config.Configuration;
import client.Client;
import client.FileInfo;

import java.io.IOException;
import java.net.Socket;

/**
 * 发送文件信息管理
 * 2端之间的通信命令如下
 *
 * type,data...
 * type为1则表示发送文件信息:                                                                      1,Base64(文件名),文件大小,文件hash值
 * type为2则表示确认是否接收(第2位0表示拒绝，1表示接收。第3位表示发送文件的端口号，拒绝的就传0):       2,1,2333
 * type为3则表示暂停或者开始任务(data为0表示暂停，1表示开始):                                        3,0
 */
public class CommandSocket {

    private Socket commandSocket;
    private String serverIp;
    private Integer commandPort;
    private Configuration config;
    private Client client;
    private CommandSocketWrite commandSocketWrite;
    private CommandSocketRead commandSocketRead;

    public CommandSocket(String serverIp, Integer commandPort, Configuration config, Client client) {
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
            commandSocketWrite = new CommandSocketWrite(commandSocket, client, config);
            config.commandPool().execute(commandSocketWrite);
            commandSocketRead = new CommandSocketRead(commandSocket, client, config);
            config.commandPool().execute(commandSocketRead);
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
