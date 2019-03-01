package com.dj.transmission;

import com.dj.transmission.adapter.TransmissionAdapter;
import com.dj.transmission.adapter.TransmissionScheduler;
import com.dj.transmission.client.TransmissionClient;
import com.dj.transmission.client.command.receive.ReceiveCommandServerSocketController;
import com.dj.transmission.client.transmission.receive.ReceiveFileDataServerSocketController;
import com.dj.transmission.config.TransmissionConfig;
import config.Configuration;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileTransmission implements TransmissionAdapter {

    private TransmissionConfig config;
    // 用于判断是否在主线程中，我们默认认为创建FileTransmission类的为主线程
    private long mainThreadId;
    // 发送命令Socket的账号
    private static volatile ExecutorService commandPoolInstance;
    // 发送文件的线程池
    private static volatile ExecutorService sendFilePoolInstance;
    // 执行回调的工具
    private TransmissionScheduler scheduler;
    // 缓存全部的客户端，在发送和接收来自同一个ip时，视为同一个客户端
    private List<TransmissionClient> clients = new ArrayList<>();
    // 作为一个接收端的serverSocket
    private ReceiveCommandServerSocketController receiveCommandServerSocketController;
    private List<OnClienListener> onClienListeners = new ArrayList<>();
    private ReceiveFileDataServerSocketController receiveFileDataServerSocketController;

    public FileTransmission() throws IOException {
        this(new TransmissionConfig());
    }

    public FileTransmission(TransmissionConfig config) throws IOException {
        this(config, new TransmissionScheduler());
    }

    public FileTransmission(TransmissionConfig config, TransmissionScheduler scheduler) throws IOException {
        if (config == null || scheduler == null)
            throw new NullPointerException();
        this.config = config;
        this.scheduler = scheduler;
        receiveCommandServerSocketController = new ReceiveCommandServerSocketController(this);
        receiveFileDataServerSocketController = new ReceiveFileDataServerSocketController(this);
    }

    @Override
    public String encodeString(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    @Override
    public String decodeString(String str) {
        try {
            return new String(Base64.getDecoder().decode(str), config.stringEncode());
        } catch (UnsupportedEncodingException e) {
            if (config.isDebug())
                e.printStackTrace();
        }
        return str;
    }

    @Override
    public Boolean isMainThread() {
        return mainThreadId == Thread.currentThread().getId();
    }

    @Override
    public ExecutorService commandPool() {
        if (null == commandPoolInstance) {
            synchronized (Configuration.class) {
                if (null == commandPoolInstance) {
                    commandPoolInstance = Executors.newCachedThreadPool();
                }
            }
        }
        return commandPoolInstance;
    }

    @Override
    public ExecutorService sendFilePool() {
        if (null == sendFilePoolInstance) {
            synchronized (Configuration.class) {
                if (null == sendFilePoolInstance) {
                    sendFilePoolInstance = Executors.newFixedThreadPool(config.sendFileTaskThreadCount() * config.sendFileTaskMaxCount());
                }
            }
        }
        return sendFilePoolInstance;
    }

    public TransmissionConfig getConfig() {
        return config;
    }

    public TransmissionScheduler getScheduler() {
        return scheduler;
    }

    /**
     * 手动指定要连接的客户端
     * @param hostAddress
     * @return
     */
    public TransmissionClient createOrGetClient(String hostAddress) {
        return createOrGetClient(hostAddress, config.commandPort());
    }

    /**
     * 手动指定要连接的客户端
     * @param hostAddress
     * @return
     */
    public TransmissionClient createOrGetClient(String hostAddress, Integer commandPort) {
        synchronized (FileTransmission.class) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).getHostAddress().equals(hostAddress))
                    return clients.get(i);
            }
            TransmissionClient client = new TransmissionClient(this, hostAddress, commandPort);
            clients.add(client);
            return client;
        }
    }

    /**
     * 接收端的commandsocket由serverSocket设置
     */
    public void clientSetReceiveCommandSocket(Socket socket) {
        synchronized (FileTransmission.class) {
            String address = socket.getLocalAddress().getHostAddress();
            for (int i = 0; i < clients.size(); i++) {
                // 如果这个接收端的client之前已经有了，直接将socket设置进去
                if (clients.get(i).getHostAddress().equals(address)) {
                    clients.get(i).setReceiveCommandSocket(socket);
                    return;
                }
            }
            TransmissionClient client = new TransmissionClient(this, address);
            client.setReceiveCommandSocket(socket);
            clients.add(client);
        }
    }

    /**
     * 接收端的filesocket由serverSocket设置
     */
    public void clientSetReceiveFileDataSocket(Socket socket) {
        synchronized (FileTransmission.class) {
            String address = socket.getLocalAddress().getHostAddress();
            for (int i = 0; i < clients.size(); i++) {
                // 必须保证在这个列表里面，说明是经过command的，不是谁传过来我都收的
                if (clients.get(i).getHostAddress().equals(address)) {
                    clients.get(i).addReceiveFileDataController(socket);
                    return;
                }
            }
        }
    }

    public List<TransmissionClient> getClients() {
        return clients;
    }

    public void addOnClienListeners(OnClienListener onClienListener) {
        onClienListeners.add(onClienListener);
    }

    public List<OnClienListener> getOnClienListeners() {
        return onClienListeners;
    }
}
