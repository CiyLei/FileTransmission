package com.dj.transmission.client.command.receive;

import com.dj.transmission.file.TransmissionFileInfo;
import com.dj.transmission.client.TransmissionClient;
import com.dj.transmission.utils.TransmissionJsonConverter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ReceiveCommandSocketWrite {
    private TransmissionClient client;
    private Socket socket;
    private DataOutputStream stream;
    private ReceiveCommandClientHandle handle;

    public ReceiveCommandSocketWrite(TransmissionClient client, Socket socket, ReceiveCommandClientHandle handle) {
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

    public void receiveAcceptOrReject(Boolean isAccept) {
        connection();
        if (isConnection() && stream != null) {
            try {
//                StringBuffer sb = new StringBuffer("2,");
//                sb.append(isAccept ? "1" : "0");
//                sb.append(",");
//                sb.append(client.getFileTransmission().getConfig().sendFilePort());
//                stream.writeUTF(sb.toString());
                stream.writeUTF(TransmissionJsonConverter.converterAcceptInfo2Json(isAccept, client.getFileTransmission().getConfig().sendFilePort()));
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

    public void sendFileReceiveInfo(TransmissionFileInfo receiveFileInfo) {
        connection();
        if (isConnection() && stream != null) {
            try {
//                StringBuffer sb = new StringBuffer("4,");
//                sb.append(receiveFileInfo.getFileHash());
//                synchronized (receiveFileInfo.getSectionInfos()) {
//                    for (TransmissionFileSectionInfo sectionInfo : receiveFileInfo.getSectionInfos()) {
//                        sb.append(",");
//                        sb.append(sectionInfo.getStartIndex());
//                        sb.append("-");
//                        sb.append(sectionInfo.getEndIndex());
//                        sb.append("-");
//                        sb.append(sectionInfo.getFinishIndex());
//                    }
//                }
//                stream.writeUTF(sb.toString());
                stream.writeUTF(TransmissionJsonConverter.converterSectionInfo2Json(receiveFileInfo));
                stream.flush();
            } catch (IOException e) {
                if (client.getFileTransmission().getConfig().isDebug())
                    e.printStackTrace();
                close();
            }
        }
    }
}
