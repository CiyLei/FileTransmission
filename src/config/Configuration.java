package config;

import java.util.Vector;

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
