package send;

import client.Client;
import config.Configuration;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Base64;

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
            // 根据文件hash值确保文件之前被确认接收过
            if (config.verificationFileHash(client, fileHash)) {
                String fileName = new String(Base64.getDecoder().decode(dataInputStream.readUTF()), config.stringEncode());
                Long startIndex = dataInputStream.readLong();
//                System.out.println(this + " start:" + startIndex);
                File file = new File(config.saveFilePath() + fileName);
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(startIndex);
                byte[] buffer = new byte[1024 * 4];
                int len = -1;
                while((len = socket.getInputStream().read(buffer)) != -1){
                    randomAccessFile.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
