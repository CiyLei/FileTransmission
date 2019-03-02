package com.dj.transmission.client;

import com.dj.transmission.FileTransmission;
import com.dj.transmission.client.command.OnConnectionListener;
import com.dj.transmission.client.command.CommandClientHandle;
import com.dj.transmission.client.command.receive.ReceiveCommandClientDelegate;
import com.dj.transmission.client.command.receive.ReceiveCommandClientDelegateImp;
import com.dj.transmission.client.command.send.OnSendClientListener;
import com.dj.transmission.client.command.send.SendCommandClientDelegate;
import com.dj.transmission.client.command.send.SendCommandClientDelegateImp;
import com.dj.transmission.client.transmission.receive.ReceiveFileDataController;
import com.dj.transmission.client.transmission.send.SendFileDataController;
import com.dj.transmission.file.TransmissionFileInfo;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TransmissionClient implements SendCommandClientDelegate, ReceiveCommandClientDelegate {
    private String hostAddress;
    private Integer commandPort;
    private SendCommandClientDelegate sendCommandClientDelegate;
    private ReceiveCommandClientDelegate receiveCommandClientDelegate;
    private FileTransmission transmission;
    private List<OnConnectionListener> onConnectionListeners = new ArrayList<>();
    private SendFileDataController sendFileDataController;
    private List<ReceiveFileDataController> receiveFileDataControllers = new ArrayList<>();

    public TransmissionClient(FileTransmission transmission, String hostAddress, Integer commandPort) {
        this.transmission = transmission;
        this.hostAddress = hostAddress;
        if (commandPort != null) {
            this.commandPort = commandPort;
            this.sendCommandClientDelegate = new SendCommandClientDelegateImp(this, hostAddress, commandPort, onConnectionListeners, handle);
        }
    }

    public TransmissionClient(FileTransmission transmission, String hostAddress) {
        this(transmission, hostAddress, null);
    }

    private CommandClientHandle handle = new CommandClientHandle() {
        @Override
        public void handleCommandStart(Integer sendFilePort) {
            sendFileDataController = new SendFileDataController(TransmissionClient.this, hostAddress, sendFilePort, getSendFileInfo());
            sendFileDataController.start();
        }

        @Override
        public void handleCommandContinue() {

        }
    };

    @Override
    public void connection() {
        // 保证在子线程中运行，不阻塞主线程
        if (transmission.isMainThread()) {
            transmission.commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    if (sendCommandClientDelegate != null)
                        sendCommandClientDelegate.connection();
                }
            });
        } else {
            if (sendCommandClientDelegate != null)
                sendCommandClientDelegate.connection();
        }
    }

    @Override
    public void sendFile(File file) {
        // 保证在子线程中运行，不阻塞主线程
        if (transmission.isMainThread()) {
            transmission.commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    if (sendCommandClientDelegate != null)
                        sendCommandClientDelegate.sendFile(file);
                }
            });
        } else {
            if (sendCommandClientDelegate != null)
                sendCommandClientDelegate.sendFile(file);
        }
    }

    @Override
    public TransmissionFileInfo getSendFileInfo() {
        if (sendCommandClientDelegate != null)
            return sendCommandClientDelegate.getSendFileInfo();
        return null;
    }

    @Override
    public void continueSend() {
        // 保证在子线程中运行，不阻塞主线程
        if (transmission.isMainThread()) {
            transmission.commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    if (sendCommandClientDelegate != null)
                        sendCommandClientDelegate.continueSend();
                }
            });
        } else {
            if (sendCommandClientDelegate != null)
                sendCommandClientDelegate.continueSend();
        }
    }

    public void pauseSend() {
        if (sendFileDataController != null)
            sendFileDataController.close();
    }

    @Override
    public void addOnSendClientListener(OnSendClientListener listener) {
        if (sendCommandClientDelegate != null)
            sendCommandClientDelegate.addOnSendClientListener(listener);
    }

    @Override
    public void removeOnSendClientListener(OnSendClientListener listener) {
        if (sendCommandClientDelegate != null)
            sendCommandClientDelegate.removeOnSendClientListener(listener);
    }

    @Override
    public void close() {
        if (sendCommandClientDelegate != null)
            sendCommandClientDelegate.close();
        if (receiveCommandClientDelegate != null)
            receiveCommandClientDelegate.close();
    }

    public void addOnConnectionListener(OnConnectionListener listener) {
        onConnectionListeners.add(listener);
    }

    public void removeOnConnectionListener(OnConnectionListener listener) {
        onConnectionListeners.add(listener);
    }

    @Override
    public TransmissionFileInfo getReceiveFileInfo() {
        if (receiveCommandClientDelegate != null)
            return receiveCommandClientDelegate.getReceiveFileInfo();
        return null;
    }

    @Override
    public void continueReceive() {
        if (receiveCommandClientDelegate != null) {
            // 保证在子线程中运行，不阻塞主线程
            if (transmission.isMainThread()) {
                transmission.commandPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (receiveCommandClientDelegate != null)
                            receiveCommandClientDelegate.continueReceive();
                    }
                });
            } else {
                receiveCommandClientDelegate.continueReceive();
            }
        }
    }

    public void pauseReceive() {
        for (ReceiveFileDataController receiveFileDataController : receiveFileDataControllers) {
            receiveFileDataController.close();
        }
    }

    public FileTransmission getFileTransmission() {
        return transmission;
    }

    /**
     * 设置接收命令的socket
     * @param socket
     */
    public void setReceiveCommandSocket(Socket socket) {
        receiveCommandClientDelegate = new ReceiveCommandClientDelegateImp(this, socket, onConnectionListeners, handle);
    }

    /**
     * 设置接收文件的socket
     * @param socket
     */
    public void addReceiveFileDataController(Socket socket) {
        if (getReceiveFileInfo() != null) {
            // 开始接收文件信息
            ReceiveFileDataController receiveFileDataController = new ReceiveFileDataController(this, socket, getReceiveFileInfo());
            receiveFileDataController.start();
            receiveFileDataControllers.add(receiveFileDataController);
        }
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public Integer getCommandPort() {
        return commandPort;
    }

    /**
     * 如果自己作为接收端，那么对方的command端口是需要对方告诉我们的，设置这里开放可以设置command
     * @param commandPort
     */
    public void setCommandPort(Integer commandPort) {
        if (commandPort != null) {
            this.commandPort = commandPort;
            this.sendCommandClientDelegate = new SendCommandClientDelegateImp(this, hostAddress, commandPort, onConnectionListeners, handle);
        }
    }
}
