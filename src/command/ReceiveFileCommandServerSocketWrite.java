package command;

import client.Client;
import config.Configuration;
import send.TransmissionFileInfo;
import send.TransmissionSectionFileInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ReceiveFileCommandServerSocketWrite implements Runnable , AcceptController {

    private Socket socket;
    private Configuration config;
    private DataOutputStream commandDataOutputStream;

    public ReceiveFileCommandServerSocketWrite(Socket socket, Configuration config) {
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

    public void colse() {
        try {
            if (commandDataOutputStream != null)
                commandDataOutputStream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        commandDataOutputStream = null;
    }

    /**
     * 开始继续传输文件的信息
     * @param transmissionFileInfo
     */
    public void sendStartTransmissionFileInfo(TransmissionFileInfo transmissionFileInfo) {
        if (config.isMainThread()) {
            config.commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    runSendStartTransmissionFileInfo(transmissionFileInfo);
                }
            });
        } else {
            runSendStartTransmissionFileInfo(transmissionFileInfo);
        }
    }

    private void runSendStartTransmissionFileInfo(TransmissionFileInfo transmissionFileInfo) {
        try {
            connection();
            if (isConnection()) {
//                commandDataOutputStream = new DataOutputStream(socket.getOutputStream());
                StringBuilder sb = new StringBuilder("4," + transmissionFileInfo.getFileHash() + ",");
                for (TransmissionSectionFileInfo sectionFileInfo : transmissionFileInfo.getSectionFileInfos()) {
                    sb.append(sectionFileInfo.getStartIndex());
                    sb.append("-");
                    sb.append(sectionFileInfo.getEndIndex());
                    sb.append("-");
                    sb.append(sectionFileInfo.getFinishIndex());
                    sb.append(",");
                }
                transmissionFileInfo.getSectionFileInfos().clear();
                commandDataOutputStream.writeUTF(sb.toString());
                commandDataOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            colse();
        }
    }

    /**
     * 同意接收
     */
    @Override
    public void accept(TransmissionFileInfo transmissionFileInfo) {
        // 如果在主线程的话，放到子线程中执行
        if (config.isMainThread()) {
            config.commandPool().execute(new Runnable() {
                @Override
                public void run() {
                    runAccept(transmissionFileInfo);
                }
            });
        } else {
            runAccept(transmissionFileInfo);
        }
    }

    private void runAccept(TransmissionFileInfo transmissionFileInfo) {
        try {
            connection();
            if (isConnection()) {
                // 将接受文件的纪录保存下来
                Client client = config.getClient(socket.getInetAddress().getHostAddress());
                if (client != null) {
                    config.addReceiveFileInfoOnClient(client, transmissionFileInfo);
//                    commandDataOutputStream = new DataOutputStream(socket.getOutputStream());
                    commandDataOutputStream.writeUTF("2,1," + config.sendFilePort().toString());
                    commandDataOutputStream.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            colse();
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
            connection();
            if (isConnection()) {
//                commandDataOutputStream = new DataOutputStream(socket.getOutputStream());
                commandDataOutputStream.writeUTF("2,0,0");
                commandDataOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            colse();
        }
    }
}
