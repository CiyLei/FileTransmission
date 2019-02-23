package scan;

import java.net.SocketException;

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

    /**
     * 成功获取到客户端的回调
     */
    public interface ScanListener {
        void onGet(String ip);
    }
}
