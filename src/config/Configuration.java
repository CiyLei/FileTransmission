package config;

import java.util.Vector;

/**
 * 自定义的配置信息
 */
public interface Configuration {

    /**
     * 需要扫描的网络ip
     * @return
     */
    Vector<String> broadcastHost();

    /**
     * 广播扫描的端口
     * @return
     */
    Integer broadcastPort();

}
