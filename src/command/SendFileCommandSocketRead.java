package command;

import client.Client;
import config.Configuration;
import send.TransmissionFileInfo;
import send.TransmissionSectionFileInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 发送端命令Socket的分析信息类
 * 作为发送端，只负责分析type为2和4的信息分析
 *
 * type,data...
 * type为1则表示发送文件信息:                                                                                    1,Base64(文件名),文件大小,文件hash值
 * type为2则表示确认是否接收(第2位0表示拒绝，1表示接收。第3位表示发送文件的端口号，拒绝的就传0):                     2,1,2333
 * type为3则表示开始任务(发送端点击开始，则会发送3过来，我们在分析接受的情况返回4，接收端点击开始就直接发送4):        3,文件hash值
 * type为4则表示返回接收端自身接收情况:                                                                           4,文件hash值,startIndex-endIndex-finishIndex,startIndex-endIndex-finishIndex,...
 */
public class SendFileCommandSocketRead implements Runnable{

    private Socket socket;
    private Client client;
    private DataInputStream commandDatainputStream;
    private Configuration config;

    public SendFileCommandSocketRead(Socket socket, Client client, Configuration config) {
        this.socket = socket;
        this.client = client;
        this.config = config;
    }

    @Override
    public void run() {
        connection();
        if (isConnection()) {
            try {
                // 收到回复信息
                while (true) {
                    String replyMsg = commandDatainputStream.readUTF();
                    //分析回复信息
                    analysisReplyMsg(replyMsg);
                }
            } catch (IOException e) {
                e.printStackTrace();
                colse();
            }
        }
    }

    public void connection() {
        try {
            if (commandDatainputStream == null)
                commandDatainputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            colse();
        }
    }

    public Boolean isConnection() {
        return commandDatainputStream != null;
    }

    public void colse() {
        try {
            if (commandDatainputStream != null)
                commandDatainputStream.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        commandDatainputStream = null;
    }

    /**
     * 分析回复信息
     * @param replyMsg
     */
    private void analysisReplyMsg(String replyMsg) {
        String[] split = replyMsg.split(",");
        if (split.length > 0) {
            switch (Integer.parseInt(split[0])) {
                // 回复了是否接收文件的信息，这里分析进行回调
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
                    // 开始
                case 4:
                    if (split.length > 2) {
                        String fileHash = split[1];
                        TransmissionFileInfo transmissionFileInfo = config.getTransmissionFileInfoForSendClient(client);
                        if (fileHash.equals(transmissionFileInfo.getFileHash())) {
                            transmissionFileInfo.getSectionFileInfos().clear();
                            for (int i = 2; i < split.length; i++) {
                                String[] ss = split[i].split("-");
                                if (ss.length == 3) {
                                    TransmissionSectionFileInfo sectionFileInfo = new TransmissionSectionFileInfo(Long.parseLong(ss[0]), Long.parseLong(ss[1]), Long.parseLong(ss[2]));
                                    transmissionFileInfo.getSectionFileInfos().add(sectionFileInfo);
                                }
                            }
                            client.continumSendFileData();
                        }
                    }
                    break;
            }
        }
    }
}
