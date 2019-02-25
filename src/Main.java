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
                public void onGet(final Client client) {
                    // getInetAddress().getHostAddress()是个阻塞方法，慎用
//                    System.out.println("扫描到客户端：" + client.getInetAddress().getHostName() + "-" + client.getInetAddress().getHostAddress());
                    System.out.println("扫描到客户端：" + client.getHostName() + " ip:" + client.getHostAddress() + " commandPort:" + client.getCommandPort());
                    client.addListener(new Client.ClientListener() {
                        @Override
                        public void onConnection(Boolean connection) {
                            System.out.println("command连接" + (connection ? "成功" : "失败") + " " + client.getHostName() + " ip:" + client.getHostAddress() + " commandPort:" + client.getCommandPort());
                        }
                    });
//                    File file = new File("/Users/chenlei/Documents/Postman_v4.1.3/icon_32.png");
//                    System.out.println("开始发送文件信息 name：" + file.getName() + " size:" + file.length());
//                    client.sendFile(file);
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
