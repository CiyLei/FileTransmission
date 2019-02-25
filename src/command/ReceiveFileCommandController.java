package command;

import config.Configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Base64;

/**
 * 接收文件信息管理
 * 2端之间的通信命令如下
 *
 * type,data...
 * type为1则表示发送文件信息:                                1,Base64(文件名),文件大小,文件hash值
 * type为2则表示确认是否接收(data为0表示拒绝，1表示接收):       2,1
 * type为3则表示暂停或者开始任务(data为0表示暂停，1表示开始):   3,0
 */
public class ReceiveFileCommandController implements Runnable, AcceptController {

    private Socket socket;
    private Configuration config;
    private DataInputStream commandDataInputStream;
    private DataOutputStream commandDataOutputStream;

    public ReceiveFileCommandController(Socket socket, Configuration config) {
        this.socket = socket;
        this.config = config;
    }

    @Override
    public void run() {
        try {
            // 收到文件信息
            commandDataInputStream = new DataInputStream(socket.getInputStream());
            String replyMsg = commandDataInputStream.readUTF();
            while (null != replyMsg && !replyMsg.isEmpty()) {
                analysisReplyMsg(replyMsg);
                replyMsg = commandDataInputStream.readUTF();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分析回复信息
     * @param replyMsg
     */
    private void analysisReplyMsg(String replyMsg) {
        String[] split = replyMsg.split(",");
        if (split.length > 0) {
            switch (Integer.parseInt(split[0])) {
                case 1:
                    obtainFileInfo(split);
                    break;
            }
        }
    }

    /**
     * 获取到了文件信息
     * @param data
     */
    private void obtainFileInfo(String[] data) {
        if (data.length == 4) {
            try {
                String fileName = new String(Base64.getDecoder().decode(data[1]), "utf-8");
                Long fileSize = Long.parseLong(data[2]);
                String fileHash = data[3];
                if (config.getListener() != null)
                    config.getListener().onFileInfoListener(fileName, fileSize, fileHash, this);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 同意接收
     */
    @Override
    public void accept() {
        try {
            commandDataOutputStream = new DataOutputStream(socket.getOutputStream());
            commandDataOutputStream.writeUTF("2,1");
            commandDataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拒接接收
     */
    @Override
    public void reject() {
        try {
            commandDataOutputStream = new DataOutputStream(socket.getOutputStream());
            commandDataOutputStream.writeUTF("2,0");
            commandDataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
