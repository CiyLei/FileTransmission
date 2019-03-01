package com.dj.transmission.client.command.send;

import com.dj.transmission.FileTransmission;
import com.dj.transmission.file.TransmissionFileInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 发送端专门发送命令用的Socket管理
 */
public class SendCommandSocketWrite {
    private Socket socket;
    private DataOutputStream stream;
    private FileTransmission transmission;

    public SendCommandSocketWrite(FileTransmission transmission, Socket socket) {
        this.transmission = transmission;
        this.socket = socket;
    }

    private Boolean isConnection() {
        return socket != null && socket.isConnected();
    }

    private void connection() {
        try {
            if (isConnection() && stream == null) {
                stream = new DataOutputStream(socket.getOutputStream());
            }
        } catch (IOException e) {
            if (transmission.getConfig().isDebug())
                e.printStackTrace();
            close();
        }
    }

    public void sendFileInfoMessage(TransmissionFileInfo fileInfo) {
        connection();
        if (isConnection() && stream != null) {
            try {
                StringBuffer sb = new StringBuffer("1,");
                sb.append(transmission.encodeString(fileInfo.getFileName()));
                sb.append(",");
                sb.append(fileInfo.getFileSize());
                sb.append(",");
                sb.append(fileInfo.getFileHash());
                sb.append(",");
                sb.append(transmission.getConfig().commandPort());
                stream.writeUTF(sb.toString());
                stream.flush();
            } catch (IOException e) {
                if (transmission.getConfig().isDebug())
                    e.printStackTrace();
                close();
            }
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
    }
}
