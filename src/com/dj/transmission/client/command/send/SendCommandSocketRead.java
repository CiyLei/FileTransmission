package com.dj.transmission.client.command.send;

import com.dj.transmission.FileTransmission;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 发送端命令Socket的分析信息类
 * 作为发送端，只负责分析type为2和4的信息分析
 *
 * type,data...
 * type为1则表示发送文件信息:                                                                                    1,Base64(文件名),文件大小,文件hash值,自己command端口
 * type为2则表示确认是否接收(第2位0表示拒绝，1表示接收。第3位表示发送文件的端口号，拒绝的就传0):                     2,1,2333
 * type为3则表示开始任务(发送端点击开始，则会发送3过来，我们在分析接受的情况返回4，接收端点击开始就直接发送4):        3,文件hash值
 * type为4则表示返回接收端自身接收情况:                                                                           4,文件hash值,startIndex-endIndex-finishIndex,startIndex-endIndex-finishIndex,...
 */
public class SendCommandSocketRead {
    private FileTransmission transmission;
    private Socket socket;
    private SendCommandClientHandle handle;
    private DataInputStream stream;

    public SendCommandSocketRead(FileTransmission transmission, Socket socket, SendCommandClientHandle handle) {
        this.transmission = transmission;
        this.socket = socket;
        this.handle = handle;
        run();
    }

    private void run() {
        connection();
        if (isConnection() && stream != null) {
            transmission.commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String result = stream.readUTF();
                            analysisReplyMsg(result);
                        }
                    } catch (IOException e) {
                        if (transmission.getConfig().isDebug())
                            e.printStackTrace();
                        close();
                    }
                }
            });
        }
    }

    // 作为发送端获取到的信息，只用分析type为2和4的情况
    private void analysisReplyMsg(String result) {
        String[] split = result.split(",");
        if (split.length > 0) {
            switch (Integer.parseInt(split[0])) {
                // 回复了是否接收文件的信息，这里分析进行回调
                case 2:
                    if (split.length == 3) {
                        Boolean accept = split[1].trim().equals("1");
                        Integer sendFilePort = Integer.parseInt(split[2]);
                        handle.handleReplyAccept(accept, sendFilePort);
                    }
                    break;
                // 开始
                case 4:

                    break;
            }
        }
    }

    private Boolean isConnection() {
        return socket != null && socket.isConnected();
    }

    private void connection() {
        try {
            if (isConnection() && stream == null) {
                stream = new DataInputStream(socket.getInputStream());
            }
        } catch (IOException e) {
            if (transmission.getConfig().isDebug())
                e.printStackTrace();
            close();
        }
    }

    public void close() {
        try {
            if (stream != null)
                stream.close();
            stream = null;
        } catch (IOException e) {
            if (transmission.getConfig().isDebug())
                e.printStackTrace();
        }
        handle.streamClose();
    }
}
