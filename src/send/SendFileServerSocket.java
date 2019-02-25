package send;

import config.Configuration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SendFileServerSocket extends ServerSocket {

    private Configuration config;

    public SendFileServerSocket(Configuration config) throws IOException {
        super(config.sendFilePort());
        this.config = config;
        start();
    }

    private void start() {
        config.sendFilePool().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = accept();
                        if (config.getClient(socket.getInetAddress().getHostAddress()) != null) {
                            config.sendFilePool().execute(new ReceiveFileSocketController(socket, config));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
