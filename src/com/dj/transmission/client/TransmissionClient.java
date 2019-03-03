package com.dj.transmission.client;

import com.dj.transmission.FileTransmission;
import com.dj.transmission.client.command.OnConnectionListener;
import com.dj.transmission.client.command.CommandClientHandle;
import com.dj.transmission.client.command.receive.OnReceiveClientListener;
import com.dj.transmission.client.command.receive.ReceiveCommandClientDelegate;
import com.dj.transmission.client.command.receive.ReceiveCommandClientDelegateImp;
import com.dj.transmission.client.command.send.OnSendClientListener;
import com.dj.transmission.client.command.send.SendCommandClientDelegate;
import com.dj.transmission.client.command.send.SendCommandClientDelegateImp;
import com.dj.transmission.client.transmission.TransmissionState;
import com.dj.transmission.client.transmission.receive.ReceiveClientStateHandle;
import com.dj.transmission.client.transmission.receive.ReceiveFileDataController;
import com.dj.transmission.client.transmission.receive.ReceiveFileDataTask;
import com.dj.transmission.client.transmission.send.SendClientStateHandle;
import com.dj.transmission.client.transmission.send.SendFileDataController;
import com.dj.transmission.file.TransmissionFileInfo;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TransmissionClient implements SendCommandClientDelegate, ReceiveCommandClientDelegate {
    private String hostAddress;
    private Integer commandPort;
    // command发送端委托
    private SendCommandClientDelegate sendCommandClientDelegate;
    // command接收端委托
    private ReceiveCommandClientDelegate receiveCommandClientDelegate;
    private FileTransmission transmission;
    // command连接回调
    private List<OnConnectionListener> onConnectionListeners = new ArrayList<>();
    // 发送文件数据的控制器
    private SendFileDataController sendFileDataController;
    // 接收文件数据的控制器
    private ReceiveFileDataController receiveFileDataController;
    // 接收端的回调
    private List<OnReceiveClientListener> onReceiveClientListeners = new ArrayList<>();
    // 发送状态
    private TransmissionState sendState = TransmissionState.PAUSE;
    // 接收状态
    private TransmissionState receiveState = TransmissionState.PAUSE;

    public TransmissionClient(FileTransmission transmission, String hostAddress, Integer commandPort) {
        this.transmission = transmission;
        this.hostAddress = hostAddress;
        this.receiveFileDataController = new ReceiveFileDataController(this, receiveClientStateHandle);
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
        public void sendClientHandleStartCommand(Integer sendFilePort) {
            sendFileDataController = new SendFileDataController(TransmissionClient.this, hostAddress, sendFilePort, getSendFileInfo(), sendClientStateHandle);
            sendFileDataController.start();
        }

        @Override
        public void receiveClientHandleAccept() {
            receiveFileDataController.getReceiveFileDataTasks().clear();
        }

        @Override
        public void handleCommandContinue() {
            if (sendFileDataController != null)
                sendFileDataController.start();
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
        if (transmission.isMainThread()) {
            transmission.commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    if (sendFileDataController != null)
                        sendFileDataController.close();
                }
            });
        } else {
            if (sendFileDataController != null)
                sendFileDataController.close();
        }
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
    public List<OnSendClientListener> getOnSendClientListener() {
        if (sendCommandClientDelegate != null)
            return sendCommandClientDelegate.getOnSendClientListener();
        return null;
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
        if (transmission.isMainThread()) {
            transmission.commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    if (receiveFileDataController != null)
                        receiveFileDataController.close();
                }
            });
        } else {
            if (receiveFileDataController != null)
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
    public void addReceiveFileDataTask(Socket socket) {
        if (getReceiveFileInfo() != null) {
            // 开始接收文件信息
            ReceiveFileDataTask receiveFileDataTask = new ReceiveFileDataTask(this, socket, getReceiveFileInfo(), receiveFileDataController);
            receiveFileDataController.addReceiveFileDataTask(receiveFileDataTask);
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
        if (commandPort != null && !commandPort.equals(this.commandPort)) {
            this.commandPort = commandPort;
            this.sendCommandClientDelegate = new SendCommandClientDelegateImp(this, hostAddress, commandPort, onConnectionListeners, handle);
        }
    }

    public void addOnReceiveClientListeners(OnReceiveClientListener listeners) {
        onReceiveClientListeners.add(listeners);
    }

    public void removeOnReceiveClientListeners(OnReceiveClientListener listeners) {
        onReceiveClientListeners.remove(listeners);
    }

    public List<OnReceiveClientListener> getOnReceiveClientListeners() {
        return onReceiveClientListeners;
    }

    /**
     * 由 SendFileDataController 进行回调
     * @param state
     */
    private SendClientStateHandle sendClientStateHandle = new SendClientStateHandle() {

        @Override
        public void sendClientStateChange(TransmissionState state) {
            sendState = state;
            transmission.getScheduler().run(new Runnable() {
                @Override
                public void run() {
                    if (getOnSendClientListener() != null) {
                        for (OnSendClientListener listener : getOnSendClientListener()) {
                            listener.onStateChange(state);
                        }
                    }
                }
            });
        }
    };

    /**
     * 由 ReceiveFileDataController 进行回调
     * @param state
     */
    private ReceiveClientStateHandle receiveClientStateHandle = new ReceiveClientStateHandle() {
        @Override
        public void receiveClientStateChange(TransmissionState state) {
            receiveState = state;
            transmission.getScheduler().run(new Runnable() {
                @Override
                public void run() {
                    if (getOnReceiveClientListeners() != null) {
                        for (OnReceiveClientListener listener : getOnReceiveClientListeners()) {
                            listener.onStateChange(state);
                        }
                    }
                }
            });
        }
    };

    public TransmissionState getSendState() {
        return sendState;
    }

    public TransmissionState getReceiveState() {
        return receiveState;
    }
}
