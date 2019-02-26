package send;

import client.Client;
import client.FileInfo;
import config.Configuration;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

public class SendFileSocketController {
    private Configuration config;
    private FileInfo fileInfo;
    private String receiveAddress;
    private Integer receivePort;

    public SendFileSocketController(Configuration config, FileInfo fileInfo, String receiveAddress, Integer sendFilePort) {
        this.config = config;
        this.fileInfo = fileInfo;
        this.receiveAddress = receiveAddress;
        this.receivePort = sendFilePort;
    }

    public void start() {
        Long average = fileInfo.getFile().length() / config.sendFileTaskThreadCount();
        for (int i = 0; i < config.sendFileTaskThreadCount(); i++) {
            Long endIndex = (i + 1) * average - 1;
            // 最后一个全包了
            if (i == config.sendFileTaskThreadCount() - 1)
                endIndex = fileInfo.getFile().length() - 1;
            config.sendFilePool().execute(new SendFileTask(fileInfo, i * average, endIndex, receiveAddress, receivePort));
        }
    }

    public class SendFileTask implements Runnable{
        private FileInfo fileInfo;
        private Long startIndex;
        private Long endIndex;
        // 接收者的ip和端口号
        private String receiveAddress;
        private Integer receivePort;
        private Socket socket;
        private RandomAccessFile randomAccessFile;
        private DataOutputStream dataOutputStream;

        public SendFileTask(FileInfo fileInfo, Long startIndex, Long endIndex, String receiveAddress, Integer receivePort) {
            this.fileInfo = fileInfo;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
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
                    TransmissionFileInfo transmissionFileInfo = config.getTransmissionFileInfoForSendClient(client);
                    if (client != null && transmissionFileInfo != null) {
                        // 先发送文件的hash,让接收端确认
                        dataOutputStream.writeUTF(fileInfo.getFileHashValue());
                        dataOutputStream.flush();
                        // 先发送文件的开始索引
                        dataOutputStream.writeLong(startIndex);
                        dataOutputStream.flush();
                        // 再取文件发送数据
                        randomAccessFile.seek(startIndex);
                        byte[] buffer = new byte[1024 * 4];
                        // 保持一段时间再更新一下
                        Long ct = System.currentTimeMillis();
                        Long sunSize = 0l;
                        while (true) {
                            if (randomAccessFile.getFilePointer() + buffer.length - 1 < endIndex) {
                                if (randomAccessFile.read(buffer) != -1) {
                                    dataOutputStream.write(buffer);
//                                    dataOutputStream.flush();

                                    sunSize += buffer.length;
                                    if (System.currentTimeMillis() - ct >= config.sendFileUpdateFrequency()) {
                                        transmissionFileInfo.addSize(sunSize);
                                        client.sendFileUpdate(transmissionFileInfo);
                                        ct = System.currentTimeMillis();
                                        sunSize = 0l;
                                    }
                                }
                            } else {
                                buffer = new byte[(int) (endIndex - randomAccessFile.getFilePointer() + 1)];
                                if (randomAccessFile.read(buffer) != -1) {
                                    dataOutputStream.write(buffer);
                                    dataOutputStream.flush();
                                    transmissionFileInfo.addSize(sunSize + buffer.length);
                                    client.sendFileUpdate(transmissionFileInfo);
                                }
                                break;
                            }
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
