package scan;

import config.Configuration;
import client.SocketClient;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * 采用udp广播的形式实现扫描设备
 *  发送和回复的规则：TAG,Base64(名称),command端口号
 */
public class BroadcastScan implements Scan {

    private Configuration config;
    private boolean isScan;
    private List<Scan.ScanListener> listeners;
    // 保存所有线程是否完成的信息
    private Map<BroadcastScanTask, Boolean> tasks;

    public BroadcastScan(Configuration configuration) throws SocketException {
        this.config = configuration;
        isScan = false;
        listeners = new ArrayList<>();
        tasks = new HashMap<>();
        new BroadcastServer().start();
    }

    @Override
    public void startScan() throws SocketException {
        final Vector ips = this.config.broadcastHost();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 2; i++) {
            BroadcastScanTask task = new BroadcastScanTask(ips);
            this.tasks.put(task, false);
            config.broadcastPool().execute(task);
        }
    }

    @Override
    public void stopScan() {
        isScan = false;
        tasks.clear();
    }

    private synchronized void ckeckAllFinish() {
        if (tasks.size() > 0) {
            for (Boolean isFinish : tasks.values()) {
                if (!isFinish)
                    return;
            }
            tasks.clear();
            for (Scan.ScanListener listener : listeners)
                listener.onFinish();
        }
    }

    @Override
    public void addListener(ScanListener listener) {
        listeners.add(listener);
    }

    /**
     * 组装广播的内容
     * @param tag
     * @return
     */
    public String getContent(String tag) {
        return tag + "," + Base64.getEncoder().encodeToString(config.selfHostName().getBytes()) + "," + config.commandPort().toString();
    }

    /**
     * 扫描任务
     */
    private class BroadcastScanTask implements Runnable {

        private Vector ips;
        private DatagramSocket socket;
        private Integer retryMaxCount;
        private Integer retryCount;

        BroadcastScanTask(Vector ips) throws SocketException {
            this.ips = ips;
            this.retryCount = 0;
            this.retryMaxCount = config.broadcastRetryMaxCount();
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
            tasks.put(this, true);
            ckeckAllFinish();
        }

        private void sendBroadcast(String ip) {
//            System.out.println("扫描:" + ip);
            try {
                String data = getContent(config.broadcastSendTag());
                InetAddress address = InetAddress.getByName(ip);
                //指定包要发送的目的地
                DatagramPacket request = new DatagramPacket(data.getBytes(), data.getBytes().length, address, config.broadcastPort());
                socket.send(request);
//                if (retryCount > 0)
//                    System.out.println("重试了:" + retryCount);
                retryCount = 0;
            } catch (IOException e) {
//                e.printStackTrace();
                // 因为广播太快了，之前的udp端口都还未来得及关闭，达到上限，所以就挂了，这里就给个最大重试的机会
                if (retryCount < retryMaxCount) {
                    ++retryCount;
                    sendBroadcast(ip);
                }
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
                    if (respone.startsWith(config.broadcastSendTag()) || respone.startsWith(config.broadcastReceiveTag())) {
                        String[] split = respone.split(",");
                        if (split.length == 3) {
                            String hostName = new String(Base64.getDecoder().decode(split[1]), config.stringEncode());
                            Integer port = Integer.parseInt(split[2]);
                            SocketClient client = new SocketClient(datagramPacket.getAddress().getHostAddress(), hostName, port, config);
                            for (Scan.ScanListener listener : listeners) {
//                              System.out.println(respone);
                                listener.onGet(client);
                                config.addClient(client);
                            }
                            // 如果是接受到广播的话，就进行回复,否则的话就是回复广播，不理他
                            if (respone.startsWith(config.broadcastSendTag())) {
                                reply(datagramPacket.getAddress().getHostAddress());
                            }
                        }
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
                String data = getContent(config.broadcastReceiveTag());
                InetAddress address = InetAddress.getByName(host);
                DatagramSocket socket = new DatagramSocket(0);
                DatagramPacket request = new DatagramPacket(data.getBytes(), data.getBytes().length, address, config.broadcastPort());
                socket.send(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
