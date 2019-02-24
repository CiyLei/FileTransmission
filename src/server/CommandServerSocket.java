package server;

import config.Configuration;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

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

    Configuration config;
    private DataInputStream commandDataInputStream;

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
                        config.commandPool().execute(new CommandAnalysisTask(socket));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 命令分析类
     */
    public class CommandAnalysisTask implements Runnable {

        private Socket socket;

        public CommandAnalysisTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                commandDataInputStream = new DataInputStream(socket.getInputStream());
                String data = commandDataInputStream.readUTF();
                String[] split = data.split(",");
                if (split.length > 0) {
                    switch (Integer.parseInt(split[0])) {
                        case 1:
                            obtainFileInfo(split);
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 获取到了文件信息
         * @param data
         */
        private void obtainFileInfo(String[] data) {
            if (data.length == 4) {
                try {
                    String fileName = new String(Base64.getDecoder().decode(data[1]), "utf-8");
                    Long fileSize = Long.parseLong(data[2]);
                    String fileHash = data[3];
                    System.out.println("获取到了文件信息 name：" + fileName + " size:" + fileSize + " hash:" + fileHash);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
