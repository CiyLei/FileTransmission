package config;

import server.CommandListener;

import java.util.Vector;
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
                    commandPoolInstance = Executors.newFixedThreadPool(sendFileTaskMaxCount() * 2 + 1);
                }
            }
        }
        return commandPoolInstance;
    }

}
