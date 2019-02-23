import scan.Scan;

import java.net.SocketException;

public class Main {

    public static void main(String[] args) {
        try {
            Scan scan = new FileTransmission().getBroadcastScan();
            scan.addListener(new Scan.ScanListener() {
                @Override
                public void onGet(String ip) {
                    System.out.println("扫描到客户端：" + ip);
                }
            });
//            scan.startScan();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
