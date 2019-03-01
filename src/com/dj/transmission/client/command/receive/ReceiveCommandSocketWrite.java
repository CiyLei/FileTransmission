package com.dj.transmission.client.command.receive;

import com.dj.transmission.FileTransmission;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ReceiveCommandSocketWrite {
    private FileTransmission transmission;
    private Socket socket;
    private DataOutputStream stream;

    public ReceiveCommandSocketWrite(FileTransmission transmission, Socket socket) {
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

    public void receiveAcceptOrReject(Boolean isAccept) {
        connection();
        if (isConnection() && stream != null) {
            try {
                StringBuffer sb = new StringBuffer("2,");
                sb.append(isAccept ? "1" : "0");
                sb.append(",");
                sb.append(transmission.getConfig().sendFilePort());
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
