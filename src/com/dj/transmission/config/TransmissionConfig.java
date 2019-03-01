package com.dj.transmission.config;

import config.DefaultConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 配置类
 */
public class TransmissionConfig {

    private static volatile String hostName;

    /**
     * 如果处于debug状态，会打印各种报错信息，反之不报
     * @return
     */
    public Boolean isDebug() {
        return true;
    }

    /**
     * 默认编码
     */
    public String stringEncode() {
        return "utf-8";
    }

    /**
     * 获取自己名称
     *
     * @return
     */
    public String hostName() {
        if (null == hostName) {
            synchronized (DefaultConfiguration.class) {
                if (null == hostName) {
                    try {
                        hostName = InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        hostName = "";
                    }
                }
            }
        }
        return hostName;
    }

    /**
     * 命令socket的端口
     *
     * @return
     */
    public Integer commandPort() {
        return 8736;
    }

    /**
     * 文件发送socket的端口
     *
     * @return
     */
    public Integer sendFilePort() {
        return 9082;
    }

    /**
     * 发送一个文件用多少个线程
     *
     * @return
     */
    public Integer sendFileTaskThreadCount() {
        return 5;
    }

    /**
     * 一次性最多发送几个文件
     */
    public Integer sendFileTaskMaxCount() {
        return 3;
    }

    /**
     * 默认文件接收保存路径
     */
    public String saveFilePath() {
        return "D:\\FileTransmissionCache";
    }

    /**
     * 发送文件的时候，回调更新的频率
     *
     * @return
     */
    public Integer sendFileUpdateFrequency() {
        return 1000;
    }
}
