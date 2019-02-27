package send;

import client.Client;
import client.FileInfo;
import config.Configuration;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SendFileSocketController {
    private Configuration config;
    private FileInfo fileInfo;
    private String receiveAddress;
    private Integer receivePort;
    private Boolean isStart;

    public SendFileSocketController(Configuration config, FileInfo fileInfo, String receiveAddress, Integer sendFilePort) {
        this.config = config;
        this.fileInfo = fileInfo;
        this.receiveAddress = receiveAddress;
        this.receivePort = sendFilePort;
    }

    public void start() {
        isStart = true;
        Client client = config.getClient(receiveAddress);
        if (client != null) {
            TransmissionFileInfo transmissionFileInfo = config.getTransmissionFileInfoForSendClient(client);
            // 如果此客户端发送任务的hash值与要发送文件的hash对不上，或者没有发送任务，初始化分割任务
            if (!transmissionFileInfo.getFileHash().equals(fileInfo.getFileHashValue()) || transmissionFileInfo.getSectionFileInfos().isEmpty()) {
                Vector<TransmissionSectionFileInfo> sectionFileInfos = new Vector<>();
                Long average = fileInfo.getFile().length() / config.sendFileTaskThreadCount();
                for (int i = 0; i < config.sendFileTaskThreadCount(); i++) {
                    Long endIndex = (i + 1) * average - 1;
                    // 最后一个全包了
                    if (i == config.sendFileTaskThreadCount() - 1)
                        endIndex = fileInfo.getFile().length() - 1;
                    TransmissionSectionFileInfo sectionFileInfo = new TransmissionSectionFileInfo(i * average, endIndex, i * average);
                    sectionFileInfos.add(sectionFileInfo);
                }
                transmissionFileInfo.setSectionFileInfos(sectionFileInfos);
                for (int i = 0; i < transmissionFileInfo.getSectionFileInfos().size(); i++) {
                    config.sendFilePool().execute(new SendFileTask(transmissionFileInfo, i, receiveAddress, receivePort));
                }
            } else {
                // 如果是继续发送任务
                for (int i = 0; i < transmissionFileInfo.getSectionFileInfos().size(); i++) {
                    config.sendFilePool().execute(new SendFileTask(transmissionFileInfo, i, receiveAddress, receivePort));
                }
            }
        }
    }

    public void stop() {
        isStart = false;
    }

    public class SendFileTask implements Runnable{
        private TransmissionFileInfo transmissionFileInfo;
        private Integer sectionIndex;
        // 接收者的ip和端口号
        private String receiveAddress;
        private Integer receivePort;
        private Socket socket;
        private RandomAccessFile randomAccessFile;
        private DataOutputStream dataOutputStream;

        public SendFileTask(TransmissionFileInfo transmissionFileInfo, Integer sectionIndex, String receiveAddress, Integer receivePort) {
            this.transmissionFileInfo = transmissionFileInfo;
            this.sectionIndex = sectionIndex;
            this.receiveAddress = receiveAddress;
            this.receivePort = receivePort;
            connection();
        }

        private void connection() {
            try {
                socket = new Socket(receiveAddress, receivePort);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                randomAccessFile = new RandomAccessFile(fileInfo.getFile(), "r");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Boolean isConnection() {
            return socket != null && socket.isConnected();
        }

        @Override
        public void run() {
            if (!isConnection())
                connection();
            if (isConnection()) {
                try {
                    Client client = config.getClient(socket.getInetAddress().getHostAddress());
                    if (client != null) {
                        // 先发送文件的hash,让接收端确认
                        dataOutputStream.writeUTF(transmissionFileInfo.getFileHash());
                        dataOutputStream.flush();
                        // 再发送文件的开始索引
                        dataOutputStream.writeLong(transmissionFileInfo.getSectionFileInfos().get(sectionIndex).getStartIndex());
                        dataOutputStream.flush();
                        // 再发送文件的结束索引
                        dataOutputStream.writeLong(transmissionFileInfo.getSectionFileInfos().get(sectionIndex).getEndIndex());
                        dataOutputStream.flush();
                        // 再发送文件的完成进度索引
                        dataOutputStream.writeLong(transmissionFileInfo.getSectionFileInfos().get(sectionIndex).getFinishIndex());
                        dataOutputStream.flush();
                        // 再取文件发送数据
                        randomAccessFile.seek(transmissionFileInfo.getSectionFileInfos().get(sectionIndex).getFinishIndex());
                        byte[] buffer = new byte[1024 * 4];
                        // 保持一段时间再更新一下
                        Long ct = System.currentTimeMillis();
//                        Long sunSize = 0l;
                        while (isStart) {
                            if (randomAccessFile.getFilePointer() + buffer.length - 1 < transmissionFileInfo.getSectionFileInfos().get(sectionIndex).getEndIndex()) {
                                if (randomAccessFile.read(buffer) != -1) {
                                    dataOutputStream.write(buffer);
//                                    dataOutputStream.flush();

//                                    sunSize += buffer.length;
                                    if (System.currentTimeMillis() - ct >= config.sendFileUpdateFrequency()) {
                                        transmissionFileInfo.getSectionFileInfos().get(sectionIndex).setFinishIndex(randomAccessFile.getFilePointer());
                                        client.sendFileUpdate(transmissionFileInfo);
                                        ct = System.currentTimeMillis();
//                                        sunSize = 0l;
                                    }
                                }
                            } else {
                                buffer = new byte[(int) (transmissionFileInfo.getSectionFileInfos().get(sectionIndex).getEndIndex() - randomAccessFile.getFilePointer() + 1)];
                                if (randomAccessFile.read(buffer) != -1) {
                                    dataOutputStream.write(buffer);
                                    dataOutputStream.flush();
                                    transmissionFileInfo.getSectionFileInfos().get(sectionIndex).setFinishIndex(randomAccessFile.getFilePointer());
                                    client.sendFileUpdate(transmissionFileInfo);
//                                    sunSize = 0l;
                                }
                                return;
                            }
                        }
                        // 如果中途暂停了
                        if (!isStart) {
                            transmissionFileInfo.getSectionFileInfos().get(sectionIndex).setFinishIndex(randomAccessFile.getFilePointer());
                            client.sendFileUpdate(transmissionFileInfo);
                        }
                    }
                } catch (IOException e) {
//                    e.printStackTrace();
                } finally {
                    colse();
                }
            }
        }

        private void colse() {
            try {
                if (randomAccessFile != null)
                    randomAccessFile.close();
                if (dataOutputStream != null)
                    dataOutputStream.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            randomAccessFile = null;
            dataOutputStream = null;
            socket = null;
        }
    }
}
