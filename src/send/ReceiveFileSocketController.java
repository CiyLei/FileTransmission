package send;

import client.Client;
import config.Configuration;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

public class ReceiveFileSocketController implements Runnable{

    private Socket socket;
    private DataInputStream dataInputStream;
    private Configuration config;
    private RandomAccessFile randomAccessFile;

    public ReceiveFileSocketController(Socket socket, Configuration config) {
        this.socket = socket;
        this.config = config;
    }

    @Override
    public void run() {
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            String fileHash = dataInputStream.readUTF();
            Client client = config.getClient(socket.getInetAddress().getHostAddress());
            TransmissionFileInfo transmissionFileInfo = config.getTransmissionFileInfoForReceiveClient(client);
            // 根据文件hash值确保文件之前被确认接收过
            if (transmissionFileInfo != null && fileHash.equals(transmissionFileInfo.getFileHash())) {
                Long startIndex = dataInputStream.readLong();
                Long endIndex = dataInputStream.readLong();
                Long finishIndex = dataInputStream.readLong();
//                System.out.println(this + "分到的位置 start:" + startIndex + " end:" + endIndex + " finish:" + finishIndex);
                TransmissionSectionFileInfo sectionFileInfo = new TransmissionSectionFileInfo(startIndex, endIndex, finishIndex);
                transmissionFileInfo.getSectionFileInfos().add(sectionFileInfo);
                createSaveFilePath(config.saveFilePath());
                File file = new File(config.saveFilePath() + transmissionFileInfo.getFileName());
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(finishIndex);
                byte[] buffer = new byte[1024 * 4];
                int len = -1;
                // 保持一段时间再更新一下
                Long ct = System.currentTimeMillis();
//                Long sunSize = 0l;
                while((len = socket.getInputStream().read(buffer)) != -1){
                    randomAccessFile.write(buffer, 0, len);
//                    sunSize += len;
                    if (System.currentTimeMillis() - ct >= config.sendFileUpdateFrequency()) {
                        sectionFileInfo.setFinishIndex(randomAccessFile.getFilePointer());
                        client.receiveFileUpdate(transmissionFileInfo);
                        ct = System.currentTimeMillis();
//                        sunSize = 0l;
                    }
                    if (!client.isReceive()){
                        colse();
                    }
                }
                sectionFileInfo.setFinishIndex(randomAccessFile.getFilePointer());
                client.receiveFileUpdate(transmissionFileInfo);
            }
        } catch (Exception e) {
//            e.printStackTrace();
            colse();
        }
    }

    private void colse() {
        try {
            if (randomAccessFile != null)
                randomAccessFile.close();
            if (dataInputStream != null)
                dataInputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        randomAccessFile = null;
        dataInputStream = null;
        socket = null;
    }

    /**
     * 创建默认保存文件的目录
     * @param path
     */
    private synchronized void createSaveFilePath(String path) {
        File p = new File(path);
        if (!p.exists())
            p.mkdirs();
    }
}
