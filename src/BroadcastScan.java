import config.Configuration;
import scan.Scan;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BroadcastScan implements Scan {

    private Configuration config;
    // 根据可用的处理器数量*2开启多线程扫描（测过来这个效率最高）
    private ExecutorService scanThreadPool;
    private boolean isScan;
    private List<Scan.ScanListener> listeners;
    // upd互相识别的字符串
    public static String TAG_SEND = "FileTransmission_Broadcast_SEND";
    // 回复的标志
    public static String TAG_RECEIVE = "FileTransmission_Broadcast_RECEIVE";

    public BroadcastScan(Configuration configuration) throws SocketException {
        this.config = configuration;
        scanThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        isScan = false;
        listeners = new ArrayList<>();
        new BroadcastServer().start();
    }

    @Override
    public void startScan() throws SocketException {
        final Vector ips = this.config.broadcastHost();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
            scanThreadPool.execute(new BroadcastScanTask(ips));
        }
    }

    @Override
    public void stopScan() {
        isScan = false;
        listeners.clear();
    }

    @Override
    public void addListener(ScanListener listener) {
        listeners.add(listener);
    }

    /**
     * 扫描任务
     */
    private class BroadcastScanTask implements Runnable {

        private Vector ips;
        private DatagramSocket socket;

        BroadcastScanTask(Vector ips) throws SocketException {
            this.ips = ips;
            socket = new DatagramSocket(0);
        }

        @Override
        public void run() {
            isScan = true;
            boolean isNoFinish = true;
            while (isNoFinish && isScan) {
                try {
                    sendBroadcast(ips.remove(0).toString());
                } catch (Exception e) {
                    isNoFinish = false;
                }
            }
            socket.close();
            isScan = false;
        }

        private void sendBroadcast(String ip) {
//            System.out.println("扫描:" + ip);
            try {
                InetAddress address = InetAddress.getByName(ip);
                //指定包要发送的目的地
                DatagramPacket request = new DatagramPacket(BroadcastScan.TAG_SEND.getBytes(), BroadcastScan.TAG_SEND.getBytes().length, address, config.broadcastPort());
                socket.send(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class BroadcastServer extends Thread {

        private DatagramSocket serverSocket;

        BroadcastServer() throws SocketException {
            serverSocket = new DatagramSocket(config.broadcastPort());
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                byte[] data = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length); // 1024
                try {
                    serverSocket.receive(datagramPacket);
                    String respone = new String(datagramPacket.getData(), 0, datagramPacket.getLength()).trim();
                    for (Scan.ScanListener listener : listeners) {
                        System.out.println(respone);
                        listener.onGet(datagramPacket.getAddress().getHostAddress());
                    }
                    // 如果是接受到广播的话，就进行回复,否则的话就是回复广播，不理他
                    if (respone.equals(TAG_SEND)) {
                        reply(datagramPacket.getAddress().getHostAddress());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 回复广播
         * @param host
         */
        private void reply(String host) {
            try {
                InetAddress address = InetAddress.getByName(host);
                DatagramSocket socket = new DatagramSocket(0);
                DatagramPacket request = new DatagramPacket(BroadcastScan.TAG_RECEIVE.getBytes(), BroadcastScan.TAG_RECEIVE.getBytes().length, address, config.broadcastPort());
                socket.send(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
