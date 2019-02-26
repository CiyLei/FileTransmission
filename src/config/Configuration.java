package config;

import client.Client;
import command.CommandListener;
import send.TransmissionFileInfo;

import java.io.UnsupportedEncodingException;
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
     * 传输过程中会对文件名称进行编码在传输，防止出现奇怪的问题，这里进行编码（默认Base64）
     * @return
     */
    public abstract String encodeString(String str);

    public abstract String decodeString(String str) throws UnsupportedEncodingException;

    /**
     * 判断是否是主线程
     * @return
     */
    public abstract Boolean isMainThread();

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
    private Set<Client> clients = new HashSet<>();

    public synchronized void addClient(Client client) {
        clients.add(client);
    }

    /**
     * 根据ip地址获取client
     * @param address
     * @return
     */
    public synchronized Client getClient(String address){
        for (Client client : clients) {
            if (client.getHostAddress().equals(address)) {
                return client;
            }
        }
        return null;
    }

    /**
     * 记录了确认接收文件的客户端
     */
    private Map<Client, TransmissionFileInfo> receiveClient = new HashMap<>();

    /**
     * 添加确认接收文件的客户端
     * @param client
     * @param transmissionFileInfo
     */
    public synchronized void addReceiveFileInfoOnClient(Client client, TransmissionFileInfo transmissionFileInfo) {
        receiveClient.put(client, transmissionFileInfo);
    }

    /**
     * 返回此客户端同意发送的文件信息
     * @param client
     * @return
     */
    public synchronized TransmissionFileInfo getTransmissionFileInfoForReceiveClient(Client client) {
        return receiveClient.get(client);
    }

    /**
     * 保存着要发送文件的客户端
     */
    private Map<Client, TransmissionFileInfo> sendClient = new HashMap<>();

    /**
     * 添加要发送文件的客户端
     * @param client
     * @param transmissionFileInfo
     */
    public synchronized void addSendClient(Client client, TransmissionFileInfo transmissionFileInfo) {
        sendClient.put(client, transmissionFileInfo);
    }

    /**
     * 返回此客户端要发生的文件信息
     * @param client
     * @return
     */
    public synchronized TransmissionFileInfo getTransmissionFileInfoForSendClient(Client client) {
        return sendClient.get(client);
    }

    /**
     * 默认文件接收保存路径
     */
    public abstract String saveFilePath();

    /**
     * 发送文件的时候，回调更新的频率
     * @return
     */
    public abstract Integer sendFileUpdateFrequency();
}
