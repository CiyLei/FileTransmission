package send;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Base64;

/**
 * 发送文件信息
 * 格式：1,Base64(文件名),文件大小,文件hash值
 */
public class CommandSendFileInfoController implements AcceptController {

    private Socket commandSocket;
    private DataOutputStream commandDataOutputStream;
    private String serverIp;
    private Integer commandPort;

    public CommandSendFileInfoController(String serverIp, Integer commandPort) {
        this.serverIp = serverIp;
        this.commandPort = commandPort;
    }

    public void sendFileInfo(FileInfo file) {
        try {
            System.out.println("--" + serverIp + ":" + commandPort);
            if (commandSocket == null)
                commandSocket = new Socket(serverIp, commandPort);
            if (commandDataOutputStream == null)
                commandDataOutputStream = new DataOutputStream(commandSocket.getOutputStream());
            String fileName = file.getFile().getName();
            String fileSize = String.valueOf(file.getFile().length());
            String fileHash = file.getFileHashValue();
            String data = "1," + Base64.getEncoder().encodeToString(fileName.getBytes("utf-8")) + "," + fileSize + "," + fileHash;
            commandDataOutputStream.writeUTF(data);
            commandDataOutputStream.flush();
            System.out.println("发送了文件信息 name：" + fileName + " size:" + fileSize + " hash:" + fileHash);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void accept() {

    }

    @Override
    public void reject() {

    }

    /**
     * 命令的监听
     */
    public interface Listener {
        /**
         * 监听到有客户端要发送文件给我
         * @param fileName
         * @param fileSize
         * @param fileHash
         */
        void onFileInfoListener(String fileName, int fileSize, String fileHash, AcceptController controller);

        /**
         * 监听到接收端是否同意接收文件的命令
         */
        void onAcceptListener();

        /**
         * 监听到接收端是否开始或者暂停的命令
         * @param isStart
         */
        void onStartOrPauseListener(Boolean isStart);
    }
}

/**
 * 是否同意的控制器
 */
interface AcceptController {
    public void accept();
    public void reject();
}
