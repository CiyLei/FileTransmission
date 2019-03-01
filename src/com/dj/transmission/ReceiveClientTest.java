package com.dj.transmission;

import com.dj.transmission.client.command.OnConnectionListener;
import com.dj.transmission.client.TransmissionClient;
import com.dj.transmission.client.command.receive.AcceptController;
import com.dj.transmission.client.command.send.OnSendClientListener;
import com.dj.transmission.config.TransmissionConfig;
import com.dj.transmission.file.TransmissionFileInfo;

import java.io.File;
import java.io.IOException;

public class ReceiveClientTest {
    public static void main(String[] args) {
        try {
            ReceiveTestFileTransmission transmission = new ReceiveTestFileTransmission();
            transmission.addOnClienListeners(new OnClienListener() {
                @Override
                public void onReceiveFileInfo(TransmissionClient client, TransmissionFileInfo fileInfo, AcceptController controller) {
                    System.out.println(System.currentTimeMillis() + " 对方 ip:" + client.getHostAddress() + " port:" + client.getCommandPort() + " 发来了文件信息 fileName:" + fileInfo.getFileName() + " fileSize:" + fileInfo.getFileSize() + " fileHash:" + fileInfo.getFileHash());
                    controller.accept();
                    client.addOnSendClientListener(new OnSendClientListener() {
                        @Override
                        public void onAccept(Boolean accept) {
                            System.out.println(System.currentTimeMillis() + "人家" + (accept ? "同意" : "拒绝") + "了");
                        }
                    });
                    client.sendFile(new File("D:\\360极速浏览器下载\\WeChatSetup.exe"));
                }
            });
            TransmissionClient client = transmission.createOrGetClient("127.0.0.1", transmission.getConfig().commandPort());
            client.addOnConnectionListener(new OnConnectionListener() {
                @Override
                public void onConnection(Boolean connection, Boolean isSend) {
                    System.out.println(System.currentTimeMillis() + " 我ReceiveClientTest作为" + (isSend ? "发送端" : "接收端") + "连接" + (connection ? "成功" : "失败"));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ReceiveClientTest初始化失败");
        }
    }

    public static class ReceiveTestFileTransmission extends FileTransmission {

        public ReceiveTestFileTransmission() throws IOException {
            super(new ReceiveTestConfig());
        }
    }

    public static class ReceiveTestConfig extends TransmissionConfig {
        @Override
        public Integer commandPort() {
            return 10098;
        }

        @Override
        public Integer sendFilePort() {
            return 10099;
        }

        @Override
        public String saveFilePath() {
            return "D:\\FileTransmissionCache\\ReceiveTest\\";
        }
    }
}
