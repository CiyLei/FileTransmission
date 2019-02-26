package command;

import client.Client;
import config.Configuration;
import send.TransmissionFileInfo;

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
 * type为1则表示发送文件信息:                                                                      1,Base64(文件名),文件大小,文件hash值
 * type为2则表示确认是否接收(第2位0表示拒绝，1表示接收。第3位表示发送文件的端口号，拒绝的就传0):       2,1,2333
 * type为3则表示暂停或者开始任务(data为0表示暂停，1表示开始):                                        3,0
 */
public class ReceiveFileCommandController implements Runnable, AcceptController {

    private Socket socket;
    private Configuration config;
    private DataInputStream commandDataInputStream;
    private DataOutputStream commandDataOutputStream;
    private TransmissionFileInfo currentTransmissionFIleInfo;

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
//            e.printStackTrace();
            colse();
        }
    }

    private Boolean isConnection() {
        return socket != null && socket.isConnected();
    }

    public void colse() {
        Client client = config.getClient(socket.getInetAddress().getHostAddress());
        if (client != null) {
            for (Client.ClientListener listener : client.getListeners())
                listener.onConnection(false);
        }
        try {
            if (commandDataInputStream != null)
                commandDataInputStream.close();
            if (commandDataOutputStream != null)
                commandDataOutputStream.close();
            socket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        commandDataOutputStream = null;
        commandDataInputStream = null;
        socket = null;
        for (Client.ClientListener listener : client.getListeners())
            listener.onConnection(false);
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
                String fileName = config.decodeString(data[1]);
                Long fileSize = Long.parseLong(data[2]);
                String fileHash = data[3];
                currentTransmissionFIleInfo = new TransmissionFileInfo(fileName, fileSize, fileHash);
                if (config.getListener() != null)
                    config.getListener().onFileInfoListener(currentTransmissionFIleInfo, this);
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
        // 如果在主线程的话，放到子线程中执行
        if (config.isMainThread()) {
            config.commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    runAccept();
                }
            });
        } else {
            runAccept();
        }
    }

    private void runAccept() {
        try {
            if (isConnection()) {
                // 将接受文件的纪录保存下来
                Client client = config.getClient(socket.getInetAddress().getHostAddress());
                if (client != null) {
                    config.addReceiveFileInfoOnClient(client, currentTransmissionFIleInfo);
                    commandDataOutputStream = new DataOutputStream(socket.getOutputStream());
                    commandDataOutputStream.writeUTF("2,1," + config.sendFilePort().toString());
                    commandDataOutputStream.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拒接接收
     */
    @Override
    public void reject() {
        if (config.isMainThread()) {
            config.commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    runReject();
                }
            });
        } else {
            runReject();
        }
    }

    private void runReject() {
        try {
            commandDataOutputStream = new DataOutputStream(socket.getOutputStream());
            commandDataOutputStream.writeUTF("2,0,0");
            commandDataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
