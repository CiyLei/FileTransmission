package command;

import client.Client;
import config.Configuration;
import send.TransmissionFileInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * 接收端命令Socket的分析信息类
 * 作为接收端，只负责分析type为1和3的信息分析
 *
 * type,data...
 * type为1则表示发送文件信息:                                                                                    1,Base64(文件名),文件大小,文件hash值
 * type为2则表示确认是否接收(第2位0表示拒绝，1表示接收。第3位表示发送文件的端口号，拒绝的就传0):                     2,1,2333
 * type为3则表示开始任务(发送端点击开始，则会发送3过来，我们在分析接受的情况返回4，接收端点击开始就直接发送4):        3,文件hash值
 * type为4则表示返回接收端自身接收情况:                                                                           4,文件hash值,startIndex-endIndex-finishIndex,startIndex-endIndex-finishIndex,...
 */
public class ReceiveFileCommandServerSocketRead implements Runnable {

    private Socket socket;
    private Configuration config;
    private DataInputStream commandDataInputStream;
    private ReceiveFileCommandServerSocketWrite commandServerSocketWrite;

    public ReceiveFileCommandServerSocketRead(Socket socket, ReceiveFileCommandServerSocketWrite commandServerSocketWrite, Configuration config) {
        this.socket = socket;
        this.config = config;
        this.commandServerSocketWrite = commandServerSocketWrite;
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

    /**
     * 分析回复信息
     * @param replyMsg
     */
    private void analysisReplyMsg(String replyMsg) {
        String[] split = replyMsg.split(",");
        if (split.length > 0) {
            switch (Integer.parseInt(split[0])) {
                case 1:
                    if (split.length == 4) {
                        try {
                            String fileName = config.decodeString(split[1]);
                            Long fileSize = Long.parseLong(split[2]);
                            String fileHash = split[3];
                            TransmissionFileInfo currentTransmissionFileInfo = new TransmissionFileInfo(fileName, fileSize, fileHash);
                            if (config.getListener() != null)
                                config.getListener().onFileInfoListener(currentTransmissionFileInfo, commandServerSocketWrite);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 3:
                    if (split.length == 2) {
                        String fileHash = split[1];
                        Client client = config.getClient(socket.getInetAddress().getHostAddress());
                        TransmissionFileInfo transmissionFileInfo = config.getTransmissionFileInfoForReceiveClient(client);
                        // 如果有为接收完毕的任务的话
                        if (fileHash.equals(transmissionFileInfo.getFileHash()) && !transmissionFileInfo.getSectionFileInfos().isEmpty()) {
//                            System.out.println("你想开始:" + fileHash + " " + transmissionFileInfo.getSectionFileInfos());
                            commandServerSocketWrite.sendStartTransmissionFileInfo(transmissionFileInfo);
                        }
                    }
                    break;
            }
        }
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
            if (socket != null)
                socket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        commandDataInputStream = null;
        socket = null;
    }
}
