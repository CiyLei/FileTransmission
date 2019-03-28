package com.dj.transmission.client.command.receive;

import com.dj.transmission.client.TransmissionClient;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 接收端命令Socket的分析信息类
 * 作为接收端，只负责分析type为1和3的信息分析
 *
 * type,data...
 * type为1则表示发送文件信息:                                                                                    1,Base64(文件名),文件大小,文件hash值,自己command端口
 * type为2则表示确认是否接收(第2位0表示拒绝，1表示接收。第3位表示发送文件的端口号，拒绝的就传0):                     2,1,2333
 * type为3则表示开始任务(发送端点击开始，则会发送3过来，我们在分析接受的情况返回4，接收端点击开始就直接发送4):        3,文件hash值
 * type为4则表示返回接收端自身接收情况:                                                                           4,文件hash值,startIndex-endIndex-finishIndex,startIndex-endIndex-finishIndex,...
 */
public class ReceiveCommandSocketRead {

    private TransmissionClient client;
    private Socket socket;
    private DataInputStream stream;
    private ReceiveCommandClientHandle handle;

    public ReceiveCommandSocketRead(TransmissionClient client, Socket socket, ReceiveCommandClientHandle handle) {
        this.client = client;
        this.socket = socket;
        this.handle = handle;
        run();
    }

    private void run() {
        connection();
        if (isConnection() && stream != null) {
            client.getFileTransmission().commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String result = stream.readUTF();
                            analysisReplyMsg(result);
                        }
                    } catch (Exception e) {
                        if (client.getFileTransmission().getConfig().isDebug())
                            e.printStackTrace();
                        close();
                    }
                }
            });
        }
    }

    // 作为接收端获取到的信息，只用分析type为1和3的情况
    private void analysisReplyMsg(String result) {
        JSONObject jo = new JSONObject(result);
        int type = jo.getInt("type");
        JSONObject jo_data = jo.getJSONObject("data");
        switch (type) {
            // 回复了是否接收文件的信息，这里分析进行回调
            case 1:
                String fileName = jo_data.getString("fileName");
                Long fileSize = jo_data.getLong("fileSize");
                String fileHash = jo_data.getString("fileHash");
                Integer commandPort = jo_data.getInt("commandPort");
                handle.handleFileInfoCommand(fileName, fileSize, fileHash, commandPort);
                break;
            // 开始
            case 3:
                handle.handleReceiveFileInfoCommand(jo_data.getString("fileHash"));
                break;
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
            if (client.getFileTransmission().getConfig().isDebug())
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
            if (client.getFileTransmission().getConfig().isDebug())
                e.printStackTrace();
        }
        handle.streamClose();
    }
}
