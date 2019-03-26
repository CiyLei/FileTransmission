import com.dj.transmission.FileTransmission;
import com.dj.transmission.OnClienListener;
import com.dj.transmission.client.command.OnConnectionListener;
import com.dj.transmission.client.TransmissionClient;
import com.dj.transmission.client.command.receive.AcceptController;
import com.dj.transmission.client.command.receive.OnReceiveClientListener;
import com.dj.transmission.client.command.send.OnSendClientListener;
import com.dj.transmission.client.transmission.TransmissionState;
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
                    System.out.println(System.currentTimeMillis() + " 人家" + (accept ? "同意" : "拒绝") + "了");
                }

                @Override
                public void onProgress(double progress) {
                    System.out.println(client.getSendFileInfo().getFileName() + " 发送进度：" + progress);
                }

                @Override
                public void onStateChange(TransmissionState state) {
                    System.out.println((state == TransmissionState.START ? "开始" : "暂停") + "发送");
                }
            });
            client.addOnReceiveClientListeners(new OnReceiveClientListener() {
                @Override
                public void onProgress(double progress) {
                    System.out.println(client.getReceiveFileInfo().getFileName() + " 接收进度：" + progress);
                }

                @Override
                public void onStateChange(TransmissionState state) {
                    System.out.println((state == TransmissionState.START ? "开始" : "暂停") + "接收");
                }
            });
            client.sendFile(new File("F:\\陈雷\\软件安装包\\WePE_64_V2.0.exe"));
//            Thread.sleep(1500);
//            /**
//             * 为什么暂停了之后过了一会onProgress会回调呢？
//             * 你看 你在config里面是不是设置了sendFileUpdateFrequency更新频率，所以onProgress并不是实时的，暂停了之后，后续也会继续将真实的数值回调回来
//             */
//            client.pauseSend();
//            Thread.sleep(3000);
//            client.continueSend();
//            Thread.sleep(3000);
//            client.sendFile(new File("F:\\陈雷\\软件安装包\\ADSafe_3.5.5.1119.exe"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Main初始化失败");
        }
    }
}
