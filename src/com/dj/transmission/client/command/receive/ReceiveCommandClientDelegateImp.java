package com.dj.transmission.client.command.receive;

import com.dj.transmission.OnClienListener;
import com.dj.transmission.client.command.OnConnectionListener;
import com.dj.transmission.client.TransmissionClient;
import com.dj.transmission.client.command.CommandClientHandle;
import com.dj.transmission.file.TransmissionFileInfo;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ReceiveCommandClientDelegateImp implements ReceiveCommandClientDelegate, ReceiveCommandClientHandle, AcceptController {

    private Socket socket;
    private TransmissionClient client;
    private TransmissionFileInfo receiveFileInfo;
    private ReceiveCommandSocketRead socketRead;
    private ReceiveCommandSocketWrite socketWrite;
    private List<OnConnectionListener> onConnectionListeners;
    private CommandClientHandle handle;

    public ReceiveCommandClientDelegateImp(TransmissionClient client, Socket socket, List<OnConnectionListener> onConnectionListeners, CommandClientHandle handle) {
        this.client = client;
        this.socket = socket;
        this.onConnectionListeners = onConnectionListeners;
        this.handle = handle;
        connection();
    }

    private void connection() {
        if (socket != null && socket.isConnected()) {
            socketRead = new ReceiveCommandSocketRead(client, socket, this);
            socketWrite = new ReceiveCommandSocketWrite(client, socket);
            client.getFileTransmission().getScheduler().run(new Runnable() {
                @Override
                public void run() {
                    for (OnConnectionListener listener  : onConnectionListeners)
                        listener.onConnection(true, false);
                }
            });
        } else {
            close();
        }
    }

    @Override
    public TransmissionFileInfo getReceiveFileInfo() {
        return receiveFileInfo;
    }

    @Override
    public void continueReceive() {

    }

    public void addOnConnectionListener(OnConnectionListener listener) {
        onConnectionListeners.add(listener);
    }

    public void removeOnConnectionListener(OnConnectionListener listener) {
        onConnectionListeners.remove(listener);
    }

    /**
     * 关闭资源
     */
    @Override
    public void close() {
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                if (client.getFileTransmission().getConfig().isDebug())
                    e.printStackTrace();
            }
        }
        client.getFileTransmission().getScheduler().run(new Runnable() {
            @Override
            public void run() {
                for (OnConnectionListener listener  : onConnectionListeners)
                    listener.onConnection(false, false);
            }
        });
    }

    @Override
    public void handleFileInfoCommand(String fileName, Long fileSize, String fileHash, Integer commandPort) {
        receiveFileInfo = new TransmissionFileInfo(fileName, fileSize, fileHash);
        client.setCommandPort(commandPort);
        client.getFileTransmission().getScheduler().run(new Runnable() {
            @Override
            public void run() {
                for (OnClienListener onClienListener : client.getFileTransmission().getOnClienListeners()) {
                    onClienListener.onReceiveFileInfo(client, receiveFileInfo, ReceiveCommandClientDelegateImp.this);
                }
            }
        });
    }

    @Override
    public void streamClose() {
        close();
    }

    @Override
    public void accept() {
        handle.receiveClientHandleAccept();
        if (client.getFileTransmission().isMainThread()) {
            client.getFileTransmission().commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    if (socketWrite != null)
                        socketWrite.receiveAcceptOrReject(true);
                }
            });
        } else {
            if (socketWrite != null)
                socketWrite.receiveAcceptOrReject(true);
        }
    }

    @Override
    public void reject() {
        receiveFileInfo = null;
        if (client.getFileTransmission().isMainThread()) {
            client.getFileTransmission().commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    if (socketWrite != null)
                        socketWrite.receiveAcceptOrReject(false);
                }
            });
        } else {
            if (socketWrite != null)
                socketWrite.receiveAcceptOrReject(false);
        }
    }
}
