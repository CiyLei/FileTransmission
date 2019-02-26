package scan;

import client.Client;

import java.net.SocketException;
import java.util.List;

/**
 * 扫描器，获取所有潜在的客户端
 */
public interface Scan {

    /**
     * 开始扫描
     */
    void startScan() throws SocketException;

    /**
     * 停止扫描
     */
    void stopScan();

    void addListener(ScanListener listener);

    List<ScanListener> getListener();

    /**
     * 成功获取到客户端的回调
     */
    public interface ScanListener {
        void onGet(Client client);
        void onFinish();
    }
}
