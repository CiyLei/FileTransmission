package command;

import client.FileInfo;
import config.Configuration;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 发送端命令Socket的主动发送信息类
 */
public class SendFileCommandSocketWrite implements Runnable {
    private Socket socket;
    private DataOutputStream commandDataOutputStream;
    private Configuration config;

    public SendFileCommandSocketWrite(Socket socket, Configuration config) {
        this.socket = socket;
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
        } catch (IOException e) {
            e.printStackTrace();
            colse();
        }
    }

    public Boolean isConnection() {
        return commandDataOutputStream != null;
    }

    public void sendFileMessage(FileInfo file) {
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
            } catch (IOException e) {
                e.printStackTrace();
                colse();
            }
        }
    }

    public void sendStartMessage(String fileHash) {
        connection();
        if (isConnection()) {
            try {
                String data = "3," + fileHash;
                commandDataOutputStream.writeUTF(data);
                commandDataOutputStream.flush();
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
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        commandDataOutputStream = null;
    }
}
