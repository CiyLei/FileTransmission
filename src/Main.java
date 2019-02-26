import scan.Scan;
import client.Client;
import send.TransmissionFileInfo;

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

                        @Override
                        public void onReceiveFileUpdate() {
                            System.out.println("接收文件:" + client.getReceiveTransmissionFileInfo().getFileName() + " 进度:" + client.getReceiveTransmissionFileInfo().getProgress());
                        }

                        @Override
                        public void onSendFileUpdate() {
                            System.out.println("发送文件:" + client.getSendTransmissionFileInfo().getFileName() + " 进度:" + client.getSendTransmissionFileInfo().getProgress());
                        }
                    });
                    try {
                        Thread.sleep(1000);
                        File file = new File("C:\\Users\\LENOVO\\Desktop\\QQ_V6.5.2.dmg");
                        System.out.println("开始发送文件信息 name：" + file.getName() + " size:" + file.length());
                        client.sendFile(file);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFinish() {
                    System.out.println("扫描完毕");
                }
            });
//            scan.startScan();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
