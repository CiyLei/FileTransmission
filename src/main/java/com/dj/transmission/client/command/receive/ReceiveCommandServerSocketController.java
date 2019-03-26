package com.dj.transmission.client.command.receive;

import com.dj.transmission.FileTransmission;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveCommandServerSocketController {
    private FileTransmission transmission;
    private ServerSocket server;

    public ReceiveCommandServerSocketController(FileTransmission transmission) throws IOException {
        this.transmission = transmission;
        server = new ServerSocket(transmission.getConfig().commandPort());
        run();
    }

    private void run() {
        transmission.commandPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Socket socket = server.accept();
                        // 将这个socket设置到相应的client对象里面
                        transmission.clientSetReceiveCommandSocket(socket);
                    }
                } catch (IOException e) {
                    if (transmission.getConfig().isDebug())
                        e.printStackTrace();
                }
            }
        });
    }
}
