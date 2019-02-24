package server;

import config.Configuration;
import send.ReceiveFileCommandController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 命令管理类
 * 接收端和发送端之间发送文件之前，先通过这个类的socket来判断是否接收文件
 * 途中接收端也通过这个类的socket来暂停
 * 2端之间的通信命令如下
 *
 * type,data...
 * type为1则表示发送文件信息:                                1,Base64(文件名),文件大小,文件hash值
 * type为2则表示确认是否接收(data为0表示拒绝，1表示接收):       2,1
 * type为3则表示暂停或者开始任务(data为0表示暂停，1表示开始):   3,0
 */
public class CommandServerSocket extends ServerSocket {

    private Configuration config;

    public CommandServerSocket(Configuration config) throws IOException {
        super(config.commandPort());
        this.config = config;
        start();
    }

    private void start() {
        config.commandPool().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = accept();
                        config.commandPool().execute(new ReceiveFileCommandController(socket, config));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
