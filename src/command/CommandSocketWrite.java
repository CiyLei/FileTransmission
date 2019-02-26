package command;

import client.Client;
import client.FileInfo;
import config.Configuration;
import send.TransmissionFileInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * commandSocket专门写的类
 */
public class CommandSocketWrite implements Runnable {
    private Socket socket;
    private Client client;
    private DataOutputStream commandDataOutputStream;
    private DataInputStream commandDatainputStream;
    private Configuration config;

    public CommandSocketWrite(Socket socket, Client client, Configuration config) {
        this.socket = socket;
        this.client = client;
        this.config = config;
    }

    @Override
    public void run() {
        connection();
    }

    public void connection() {
        try {
            if (commandDataOutputStream == null)
                commandDataOutputStream = new DataOutputStream(socket.getOutputStream());
            if (commandDatainputStream == null)
                commandDatainputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean isConnection() {
        return commandDatainputStream != null && commandDataOutputStream != null;
    }

    public void sendFileInfo(FileInfo file) {
        if (!isConnection())
            connection();
        if (isConnection()) {
            try {
                // 发送文件信息
                String fileName = file.getFile().getName();
                String fileSize = String.valueOf(file.getFile().length());
                String fileHash = file.getFileHashValue();
                String data = "1," + config.encodeString(fileName) + "," + fileSize + "," + fileHash;
                commandDataOutputStream.writeUTF(data);
                commandDataOutputStream.flush();
                // 收到回复信息
                String replyMsg = commandDatainputStream.readUTF();
                System.out.println("我是CommandSocketSend 回复信息:" + replyMsg);
                if (null != replyMsg && !replyMsg.isEmpty()) {
                    //分析回复信息
                    analysisReplyMsg(replyMsg);
                }
            } catch (IOException e) {
                e.printStackTrace();
                colse();
            }
        }
    }

    public void colse() {
        try {
            if (commandDataOutputStream != null)
                commandDataOutputStream.close();
            if (commandDatainputStream != null)
                commandDatainputStream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        commandDataOutputStream = null;
        commandDatainputStream = null;
    }

    /**
     * 处理是否同意了接收文件的信息
     * @param replyMsg
     */
    private void analysisReplyMsg(String replyMsg) {
        String[] split = replyMsg.split(",");
        if (split.length > 0) {
            switch (Integer.parseInt(split[0])) {
                case 2:
                    if (split.length == 3 && config.getListener() != null) {
                        Boolean accept = split[1].trim().equals("1");
                        Integer sendFilePort = Integer.parseInt(split[2]);
                        if (accept) {
                            // 同意的话就记录下来，再后来传输的时候取
                            config.addSendClient(client, new TransmissionFileInfo(client.getSendFile().getFile().getName(), client.getSendFile().getFile().length(), client.getSendFile().getFileHashValue()));
                        }
                        client.replyIsAccept(accept, sendFilePort);
                    }
                    break;
            }
        }
    }
}
