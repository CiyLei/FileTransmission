import scan.Scan;
import send.Client;

import java.net.SocketException;

public class Main {

    public static void main(String[] args) {
        try {
            Scan scan = new FileTransmission().getBroadcastScan();
            scan.addListener(new Scan.ScanListener() {
                @Override
                public void onGet(Client client) {
                    System.out.println("扫描到客户端：" + client.getInetAddress().getHostName() + "-" + client.getInetAddress().getHostAddress());
                }

                @Override
                public void onFinish() {
                    System.out.println("扫描完毕");
                }
            });
            scan.startScan();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
