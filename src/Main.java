import scan.Scan;
import send.Client;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            Scan scan = new FileTransmission().getBroadcastScan();
            scan.addListener(new Scan.ScanListener() {
                @Override
                public void onGet(Client client) {
                    // getInetAddress().getHostAddress()是个阻塞方法，慎用
//                    System.out.println("扫描到客户端：" + client.getInetAddress().getHostName() + "-" + client.getInetAddress().getHostAddress());
                    System.out.println("扫描到客户端：" + client.getHostName() + " ip:" + client.getHostAddress() + " commandPort:" + client.getCommandPort());
                    client.sendFile(new File("/Users/chenlei/Documents/Postman_v4.1.3/icon_32.png"));
                }

                @Override
                public void onFinish() {
                    System.out.println("扫描完毕");
                }
            });
            scan.startScan();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
