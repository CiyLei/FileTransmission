package command;

import client.SocketClient;
import config.Configuration;
import client.Client;
import scan.Scan;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * 接收端命令Socket的管理类
 * ReceiveFileCommandServerSocketRead   负责接收端分析回复信息
 * ReceiveFileCommandServerSocketWrite  负责接收端主动发送信息
 */
public class ReceiveFileCommandServerSocket extends ServerSocket {

    private Configuration config;
    private List<Scan.ScanListener> listeners;

    public ReceiveFileCommandServerSocket(Configuration config, List<Scan.ScanListener> listeners) throws IOException {
        super(config.commandPort());
        this.config = config;
        this.listeners = listeners;
        start();
    }

    private void start() {
        config.commandPool().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = accept();
                        Client client = config.getClient(socket.getInetAddress().getHostAddress());
                        // 客户不经过广播的情况下，手动进入广播的回调
                        if (client == null) {
                            client = new SocketClient(socket.getInetAddress().getHostAddress(), socket.getInetAddress().getHostName(), socket.getLocalPort(), config);
                            if (listeners != null) {
                                for (Scan.ScanListener listener : listeners) {
                                    listener.onGet(client);
                                }
                            }
                            config.addClient(client);
                        }
                        if ((client = config.getClient(socket.getInetAddress().getHostAddress())) != null) {
                            for (Client.ClientListener listener : client.getListeners())
                                listener.onConnection(true);
                            // 当commandSocket连接上了socket，则针对这个socket开启两个线程，一个死循环读消息专门回复消息，一个主动发送消息
                            ReceiveFileCommandServerSocketWrite commandServerSocketWrite = new ReceiveFileCommandServerSocketWrite(socket, config);
                            config.commandPool().execute(commandServerSocketWrite);
                            config.commandPool().execute(new ReceiveFileCommandServerSocketRead(socket, commandServerSocketWrite, config));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
