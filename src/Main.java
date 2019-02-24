import scan.Scan;
import send.Client;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

public class Main {

    public static void main(String[] args) {
        try {
            Scan scan = new FileTransmission().getBroadcastScan();
            scan.addListener(new Scan.ScanListener() {
                @Override
                public void onGet(Client client) {
                    // getInetAddress().getHostAddress()是个阻塞方法，慎用
//                    System.out.println("扫描到客户端：" + client.getInetAddress().getHostName() + "-" + client.getInetAddress().getHostAddress());
                    System.out.println("扫描到客户端：" + client.getInetAddress().getHostAddress());
                    client.sendFile(new File("E:\\Java8\\jdk1.8.0_25\\src.zip"));
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
