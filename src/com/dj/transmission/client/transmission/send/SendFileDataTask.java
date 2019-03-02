package com.dj.transmission.client.transmission.send;

import com.dj.transmission.client.TransmissionClient;
import com.dj.transmission.client.command.send.OnSendClientListener;
import com.dj.transmission.client.transmission.TransmissionState;
import com.dj.transmission.file.TransmissionFileInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.List;

public class SendFileDataTask implements Runnable{
    private TransmissionClient client;
    private TransmissionFileInfo fileInfo;
    private Integer sectionIndex;
    // 接收者的ip和端口号
    private String hostAddress;
    private Integer port;
    private SendFileDataController sendFileDataController;
    private Socket socket;
    private RandomAccessFile randomAccessFile;
    private DataOutputStream dataOutputStream;
    private Boolean isStart;

    public SendFileDataTask(TransmissionClient client, TransmissionFileInfo fileInfo, Integer sectionIndex, String hostAddress, Integer port, SendFileDataController sendFileDataController) {
        this.client = client;
        this.fileInfo = fileInfo;
        this.sectionIndex = sectionIndex;
        this.hostAddress = hostAddress;
        this.port = port;
        this.sendFileDataController = sendFileDataController;
        isStart = true;
        connection();
    }

    private void connection() {
        try {
            socket = new Socket(hostAddress, port);
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
                // 先发送文件的hash,让接收端确认
                dataOutputStream.writeUTF(fileInfo.getFileHash());
                dataOutputStream.flush();
                // 再发送文件的开始索引
                dataOutputStream.writeLong(fileInfo.getSectionInfos().get(sectionIndex).getStartIndex());
                dataOutputStream.flush();
                // 再发送文件的结束索引
                dataOutputStream.writeLong(fileInfo.getSectionInfos().get(sectionIndex).getEndIndex());
                dataOutputStream.flush();
                // 再发送文件的完成进度索引
                dataOutputStream.writeLong(fileInfo.getSectionInfos().get(sectionIndex).getFinishIndex());
                dataOutputStream.flush();
                // 再取文件发送数据
                randomAccessFile.seek(fileInfo.getSectionInfos().get(sectionIndex).getFinishIndex());
                byte[] buffer = new byte[1024 * 4];
                // 保持一段时间再更新一下
                Long ct = System.currentTimeMillis();
                while (isStart) {
                    if (randomAccessFile.getFilePointer() + buffer.length - 1 < fileInfo.getSectionInfos().get(sectionIndex).getEndIndex()) {
                        if (randomAccessFile.read(buffer) != -1) {
                            dataOutputStream.write(buffer);

                            if (System.currentTimeMillis() - ct >= client.getFileTransmission().getConfig().sendFileUpdateFrequency()) {
                                fileInfo.getSectionInfos().get(sectionIndex).setFinishIndex(randomAccessFile.getFilePointer());
                                ct = System.currentTimeMillis();
                                callProgress(fileInfo.getProgress());
                            }
                        }
                    } else {
                        buffer = new byte[(int) (fileInfo.getSectionInfos().get(sectionIndex).getEndIndex() - randomAccessFile.getFilePointer() + 1)];
                        if (randomAccessFile.read(buffer) != -1) {
                            dataOutputStream.write(buffer);
                            dataOutputStream.flush();
                            fileInfo.getSectionInfos().get(sectionIndex).setFinishIndex(randomAccessFile.getFilePointer());
                            callProgress(fileInfo.getProgress());
                        }
                        return;
                    }
                }
                // 如果中途暂停了
                if (!isStart) {
                    fileInfo.getSectionInfos().get(sectionIndex).setFinishIndex(randomAccessFile.getFilePointer());
                    callProgress(fileInfo.getProgress());
                }

            } catch (IOException e) {
                if (client.getFileTransmission().getConfig().isDebug())
                    e.printStackTrace();
                // 报错了，说明接收端强制关闭了socket，则认为是暂停
                sendFileDataController.getSendClientStateHandle().sendClientStateChange(TransmissionState.PAUSE);
            } finally {
                socketClose();
            }
        }
    }

    private void callProgress(double progress) {
        List<OnSendClientListener> onSendClientListener = client.getOnSendClientListener();
        client.getFileTransmission().getScheduler().run(new Runnable() {
            @Override
            public void run() {
                if (onSendClientListener != null) {
                    for (OnSendClientListener listener : onSendClientListener) {
                        listener.onProgress(progress);
                    }
                }
            }
        });
    }

    public void close() {
        isStart = false;
    }

    private void socketClose() {
        try {
            if (randomAccessFile != null)
                randomAccessFile.close();
            if (dataOutputStream != null)
                dataOutputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e1) {
            if (client.getFileTransmission().getConfig().isDebug())
                e1.printStackTrace();
        }
        randomAccessFile = null;
        dataOutputStream = null;
        socket = null;
    }
}
