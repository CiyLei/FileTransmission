package com.dj.transmission.client.transmission.receive;

import com.dj.transmission.client.TransmissionClient;
import com.dj.transmission.client.command.receive.OnReceiveClientListener;
import com.dj.transmission.file.TransmissionFileInfo;
import com.dj.transmission.file.TransmissionFileSectionInfo;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.List;

public class ReceiveFileDataController {
    private TransmissionClient client;
    private Socket socket;
    private TransmissionFileInfo reveiceFileInfo;
    private DataInputStream dataInputStream;
    private RandomAccessFile randomAccessFile;
    private Boolean isStart;

    public ReceiveFileDataController(TransmissionClient client, Socket socket, TransmissionFileInfo reveiceFileInfo) {
        this.client = client;
        this.socket = socket;
        this.reveiceFileInfo = reveiceFileInfo;
        this.isStart = false;
    }

    public void start() {
        isStart = true;
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            String fileHash = dataInputStream.readUTF();
            // 如果传输过来的文件hash与之前接收的文件hash值对不上的话，直接关闭拒绝接收
            if (reveiceFileInfo.getFileHash().equals(fileHash)) {
                Long startIndex = dataInputStream.readLong();
                Long endIndex = dataInputStream.readLong();
                Long finishIndex = dataInputStream.readLong();
                TransmissionFileSectionInfo sectionInfo = new TransmissionFileSectionInfo(startIndex, endIndex, finishIndex);
                reveiceFileInfo.getSectionInfos().add(sectionInfo);
                String saveFilePath = createSaveFilePath(client.getFileTransmission().getConfig().saveFilePath());
                randomAccessFile = new RandomAccessFile(saveFilePath + reveiceFileInfo.getFileName(), "rwd");
                randomAccessFile.seek(finishIndex);
                byte[] buffer = new byte[1024 * 4];
                int len = -1;
                // 保持一段时间再更新一下
                Long ct = System.currentTimeMillis();
                while((len = socket.getInputStream().read(buffer)) != -1){
                    randomAccessFile.write(buffer, 0, len);
                    if (System.currentTimeMillis() - ct >= client.getFileTransmission().getConfig().sendFileUpdateFrequency()) {
                        sectionInfo.setFinishIndex(randomAccessFile.getFilePointer());
                        ct = System.currentTimeMillis();
                        callProgress(reveiceFileInfo.getProgress());
                    }
                    if (!isStart){
                        break;
                    }
                }
                sectionInfo.setFinishIndex(randomAccessFile.getFilePointer());
                callProgress(reveiceFileInfo.getProgress());
            } else {
                socketClose();
            }
        } catch (IOException e) {
            if (client.getFileTransmission().getConfig().isDebug())
                e.printStackTrace();
        } finally {
            socketClose();
        }
    }

    private void callProgress(double progress) {
        List<OnReceiveClientListener> onReceiveClientListeners = client.getOnReceiveClientListeners();
        client.getFileTransmission().getScheduler().run(new Runnable() {
            @Override
            public void run() {
                if (onReceiveClientListeners != null) {
                    for (OnReceiveClientListener listener : onReceiveClientListeners) {
                        listener.onProgress(progress);
                    }
                }
            }
        });
    }

    private synchronized String createSaveFilePath(String saveFilePath) {
        String path = saveFilePath + (saveFilePath.endsWith(File.separator) ? "" : File.separator);
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        return path;
    }

    public void close() {
        isStart = false;
    }

    private void socketClose() {
        try {
            if (randomAccessFile != null)
                randomAccessFile.close();
            if (dataInputStream != null)
                dataInputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e1) {
            if (client.getFileTransmission().getConfig().isDebug())
                e1.printStackTrace();
        }
        randomAccessFile = null;
        dataInputStream = null;
        socket = null;
    }
}
