package com.dj.transmission.client.command.send;

import com.dj.transmission.FileTransmission;
import com.dj.transmission.client.command.OnConnectionListener;
import com.dj.transmission.client.command.CommandClientHandle;
import com.dj.transmission.file.TransmissionFileInfo;
import com.dj.transmission.file.TransmissionFileSectionInfo;
import com.dj.transmission.utils.MD5Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SendCommandClientDelegateImp implements SendCommandClientDelegate, SendCommandClientHandle {

    private String hostAddress;
    private Integer commandPort;
    private FileTransmission transmission;
    private List<OnSendClientListener> onSendClientListeners = new ArrayList<>();
    private List<OnConnectionListener> onConnectionListeners;
    private Socket socket;
    private SendCommandSocketWrite socketWrite;
    private SendCommandSocketRead socketRead;
    private TransmissionFileInfo sendFileInfo;
    private CommandClientHandle handle;

    public SendCommandClientDelegateImp(FileTransmission transmission, String hostAddress, Integer commandPort, List<OnConnectionListener> onConnectionListeners, CommandClientHandle handle) {
        this.transmission = transmission;
        this.hostAddress = hostAddress;
        this.commandPort = commandPort;
        this.onConnectionListeners = onConnectionListeners;
        this.handle = handle;
    }

    @Override
    public void connection() {
        if (socket == null || socket.isClosed()) {
            try {
                socket = new Socket(hostAddress, commandPort);
                socketWrite = new SendCommandSocketWrite(transmission, socket);
                socketRead = new SendCommandSocketRead(transmission, socket, this);
            } catch (IOException e) {
                if (transmission.getConfig().isDebug())
                    e.printStackTrace();
                close();
                return;
            }
        }
        transmission.getScheduler().run(new Runnable() {
            @Override
            public void run() {
                for (OnConnectionListener listener  : onConnectionListeners)
                    listener.onConnection(true, true);
            }
        });
    }

    private Boolean isConnection() {
        return socket != null && socket.isConnected();
    }

    @Override
    public void sendFile(File file) {
        if (!isConnection())
            connection();
        if (isConnection()) {
            analysisFileHash(file);
        }
    }

    @Override
    public TransmissionFileInfo getSendFileInfo() {
        return sendFileInfo;
    }

    @Override
    public void continueSend() {

    }

    @Override
    public void addOnSendClientListener(OnSendClientListener listener) {
        onSendClientListeners.add(listener);
    }

    @Override
    public void removeOnSendClientListener(OnSendClientListener listener) {
        onSendClientListeners.remove(listener);
    }

    public void addOnConnectionListener(OnConnectionListener listener) {
        onConnectionListeners.add(listener);
    }

    public void removeOnConnectionListener(OnConnectionListener listener) {
        onConnectionListeners.remove(listener);
    }

    /**
     * 分析文件hash值
     * @param file
     */
    private void analysisFileHash(File file) {
        String hash = null;
        try {
            hash = MD5Util.md5HashCode(file.getPath());
            sendFileInfo = new TransmissionFileInfo(file, hash);
            socketWrite.sendFileInfoMessage(sendFileInfo);
        } catch (FileNotFoundException e) {
            if (transmission.getConfig().isDebug())
                e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            if (transmission.getConfig().isDebug())
                e.printStackTrace();
        }
        transmission.getScheduler().run(new Runnable() {
            @Override
            public void run() {
                for (OnConnectionListener listener  : onConnectionListeners)
                    listener.onConnection(false, true);
            }
        });
    }

    @Override
    public void handleReplyAccept(Boolean accept, Integer sendFilePort) {
        // 同意了的话，马上分割文件
        if (accept) {
            sendFileInfo.getSectionInfos().clear();
            Long average = sendFileInfo.getFileSize() / transmission.getConfig().sendFileTaskThreadCount();
            for (int i = 0; i < transmission.getConfig().sendFileTaskThreadCount(); i++) {
                Long endIndex = (i + 1) * average - 1;
                // 最后一个全包了
                if (i == transmission.getConfig().sendFileTaskThreadCount() - 1)
                    endIndex = sendFileInfo.getFileSize() - 1;
                TransmissionFileSectionInfo sectionFileInfo = new TransmissionFileSectionInfo(i * average, endIndex, i * average);
                sendFileInfo.getSectionInfos().add(sectionFileInfo);
            }
        }
        // 进行回调
        transmission.getScheduler().run(new Runnable() {
            @Override
            public void run() {
                for (OnSendClientListener onSendClientListener : onSendClientListeners) {
                    onSendClientListener.onAccept(accept);
                }
            }
        });
        if (accept) {
            // 通知client端发送过来的sendFilePort，马上进行传输文件数据
            handle.handleCommandStart(sendFilePort);
        }
    }

    @Override
    public void streamClose() {
        close();
    }
}
