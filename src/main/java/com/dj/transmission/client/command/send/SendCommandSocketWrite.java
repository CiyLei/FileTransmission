package com.dj.transmission.client.command.send;

import com.dj.transmission.client.TransmissionClient;
import com.dj.transmission.file.TransmissionFileInfo;
import com.dj.transmission.utils.TransmissionJsonConverter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 发送端专门发送命令用的Socket管理
 */
public class SendCommandSocketWrite {
    private TransmissionClient client;
    private Socket socket;
    private DataOutputStream stream;
    private SendCommandClientHandle handle;

    public SendCommandSocketWrite(TransmissionClient client, Socket socket, SendCommandClientHandle handle) {
        this.client = client;
        this.socket = socket;
        this.handle = handle;
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
            if (client.getFileTransmission().getConfig().isDebug())
                e.printStackTrace();
            close();
        }
    }

    public void sendFileInfoMessage(TransmissionFileInfo fileInfo) {
        connection();
        if (isConnection() && stream != null) {
            try {
                stream.writeUTF(TransmissionJsonConverter.converterFileInfo2Json(fileInfo, client.getFileTransmission().getConfig().commandPort()));
                stream.flush();
            } catch (IOException e) {
                if (client.getFileTransmission().getConfig().isDebug())
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
            if (client.getFileTransmission().getConfig().isDebug())
                e.printStackTrace();
        }
        handle.streamClose();
    }

    public void sendContinueMessage(TransmissionFileInfo sendFileInfo) {
        connection();
        if (isConnection() && stream != null) {
            try {
                stream.writeUTF(TransmissionJsonConverter.converterContinueInfo2Json(sendFileInfo));
                stream.flush();
            } catch (IOException e) {
                if (client.getFileTransmission().getConfig().isDebug())
                    e.printStackTrace();
                close();
            }
        }
    }
}
