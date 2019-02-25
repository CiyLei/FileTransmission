package config;

import client.Client;
import command.CommandListener;
import send.TransmissionFileInfo;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 自定义的配置信息
 */
public abstract class Configuration {

    /**
     * 命令socket监听
     */
    private CommandListener listener;

    public Configuration(CommandListener listener) {
        this.listener = listener;
    }

    public CommandListener getListener() {
        return listener;
    }

    /**
     * 默认编码
     */
    public String stringEncode() {
        return "utf-8";
    }

    /**
     * 获取自己名称
     * @return
     */
    public abstract String selfHostName();

    /**
     * 需要扫描的网络ip
     * @return
     */
    public abstract Vector<String> broadcastHost();

    /**
     * 广播扫描的端口
     * @return
     */
    public abstract Integer broadcastPort();

    /**
     * 因为广播太快了，之前的udp端口都还未来得及关闭，达到上限，所以就挂了，这里就给个最大重试的机会
     * @return
     */
    public abstract Integer broadcastRetryMaxCount();

    /**
     * 命令socket的端口
     * @return
     */
    public abstract Integer commandPort();

    /**
     * 文件发送socket的端口
     * @return
     */
    public abstract Integer sendFilePort();

    /**
     * 同时开启多少个线程去扫描广播
     * @return
     */
    public abstract Integer broadcastConcurrentCount();

    /**
     * 单例模式创建一个广播的线程池
     */
    private static volatile ExecutorService broadcastPoolInstance;

    public ExecutorService broadcastPool() {
        if (null == broadcastPoolInstance) {
            synchronized (Configuration.class) {
                if (null == broadcastPoolInstance) {
                    broadcastPoolInstance = Executors.newFixedThreadPool(broadcastConcurrentCount());
                }
            }
        }
        return broadcastPoolInstance;
    }

    /**
     * 广播发送标志
     * @return
     */
    public abstract String broadcastSendTag();

    /**
     * 广播回复的标志
     * @return
     */
    public abstract String broadcastReceiveTag();

    /**
     * 发送一个文件用多少个线程
     * @return
     */
    public abstract Integer sendFileTaskThreadCount();

    /**
     * 一次性最多发送几个文件
     */
    public abstract Integer sendFileTaskMaxCount();

    /**
     * 发送文件的线程池
     */
    private static volatile ExecutorService sendFilePoolInstance;

    public ExecutorService sendFilePool() {
        if (null == sendFilePoolInstance) {
            synchronized (Configuration.class) {
                if (null == sendFilePoolInstance) {
                    sendFilePoolInstance = Executors.newFixedThreadPool(sendFileTaskThreadCount() * sendFileTaskMaxCount());
                }
            }
        }
        return sendFilePoolInstance;
    }

    /**
     * 命令socket的线程池
     */
    private static volatile ExecutorService commandPoolInstance;

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

    /**
     * 一般情况下是接收到广播的回复信息则认为识别到一个客户端。
     * 但有这么一个情况，被识别到的客户端一被扫描到，就马上发送了一个文件，即开了一个commandSocket连到自己
     * 如果这时候这个commandSocket比回复的广播要快，那么Scan的onGet回调就会在是否接收文件的回调之后
     * 所以为了避免这个bug，我们将经过广播回复的客户端保存起来，在commandSocket过快，不在这个保存的客户端里面的时候，拒绝这个commandSocket
     * 同时也保存着接收文件的hash信息，保证并不是谁传给我我都收
     */

    private Map<Client, TransmissionFileInfo> clients = new HashMap<>();

    public synchronized void addClient(Client client, TransmissionFileInfo transmissionFIleInfo) {
        clients.put(client, transmissionFIleInfo);
    }

    /**
     * 根据ip地址获取client
     * @param address
     * @return
     */
    public synchronized Client getClient(String address){
        for (Client client : clients.keySet()) {
            if (client.getHostAddress().equals(address)) {
                return client;
            }
        }
        return null;
    }

    /**
     * 验证文件之前是否确认接收过
     * @param client
     * @return
     */
    public synchronized TransmissionFileInfo getTransmissionFileInfoForClient(Client client) {
        return clients.get(client);
    }

    /**
     * 默认文件接收保存路径
     */
    public abstract String saveFilePath();
}
