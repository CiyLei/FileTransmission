package com.dj.transmission.client.transmission.receive;

import com.dj.transmission.FileTransmission;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveFileDataServerSocketController {
    private FileTransmission transmission;
    private ServerSocket server;

    public ReceiveFileDataServerSocketController(FileTransmission transmission) throws IOException {
        this.transmission = transmission;
        this.server = new ServerSocket(transmission.getConfig().sendFilePort());
        run();
    }

    private void run() {
        transmission.sendFilePool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Socket socket = server.accept();
                        // 将这个socket设置到相应的client对象里面
                        transmission.clientSetReceiveFileDataSocket(socket);
                    }
                } catch (IOException e) {
                    if (transmission.getConfig().isDebug())
                        e.printStackTrace();
                }
            }
        });
    }
}
