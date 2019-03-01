import com.dj.transmission.FileTransmission;
import com.dj.transmission.OnClienListener;
import com.dj.transmission.client.command.OnConnectionListener;
import com.dj.transmission.client.TransmissionClient;
import com.dj.transmission.client.command.receive.AcceptController;
import com.dj.transmission.client.command.send.OnSendClientListener;
import com.dj.transmission.file.TransmissionFileInfo;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        try {
            FileTransmission transmission = new FileTransmission();
            transmission.addOnClienListeners(new OnClienListener() {
                @Override
                public void onReceiveFileInfo(TransmissionClient client, TransmissionFileInfo fileInfo, AcceptController controller) {
                    System.out.println(System.currentTimeMillis() + " 对方 ip:" + client.getHostAddress() + " port:" + client.getCommandPort() + " 发来了文件信息 fileName:" + fileInfo.getFileName() + " fileSize:" + fileInfo.getFileSize() + " fileHash:" + fileInfo.getFileHash());
//                    controller.reject();
                    controller.accept();
                }
            });
            TransmissionClient client = transmission.createOrGetClient("127.0.0.1", 10098);
            client.addOnConnectionListener(new OnConnectionListener() {
                @Override
                public void onConnection(Boolean connection, Boolean isSend) {
                    System.out.println(System.currentTimeMillis() + " 我Main作为" + (isSend ? "发送端" : "接收端") + "连接" + (connection ? "成功" : "失败"));
                }
            });
            client.addOnSendClientListener(new OnSendClientListener() {
                @Override
                public void onAccept(Boolean accept) {
                    System.out.println(System.currentTimeMillis() + "人家" + (accept ? "同意" : "拒绝") + "了");
                }
            });
            client.sendFile(new File("D:\\360极速浏览器下载\\jdk-8u181-windows-x64.exe"));
//            Thread.sleep(100);
//            client.pauseSend();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Main初始化失败");
        }
    }
}
