package command;

import client.Client;
import config.Configuration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * commandSocket专门读的类
 */
public class CommandSocketRead implements Runnable{

    private Socket socket;
    private Client client;
    private DataOutputStream commandDataOutputStream;
    private DataInputStream commandDatainputStream;
    private Configuration config;

    public CommandSocketRead(Socket socket, Client client, Configuration config) {
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
                    System.out.println("我是CommandSocketReceive 回复信息:" + replyMsg);
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
     * 处理开始或暂停的信息
     * @param replyMsg
     */
    private void analysisReplyMsg(String replyMsg) {
        String[] split = replyMsg.split(",");
        if (split.length > 0) {
            switch (Integer.parseInt(split[0])) {
                case 3:
//                    if (split.length == 3 && config.getListener() != null) {
//                        Boolean accept = split[1].trim().equals("1");
//                        Integer sendFilePort = Integer.parseInt(split[2]);
//                        if (accept) {
//                            // 同意的话就记录下来，再后来传输的时候取
//                            config.addSendClient(client, new TransmissionFileInfo(client.getSendFile().getFile().getName(), client.getSendFile().getFile().length(), client.getSendFile().getFileHashValue()));
//                        }
//                        client.replyIsAccept(accept, sendFilePort);
//                    }
                    break;
            }
        }
    }
}
