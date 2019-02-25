package config;

import command.CommandListener;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * 默认的配置信息
 */
public class DefaultConfiguration extends Configuration {

    public DefaultConfiguration(CommandListener listener) {
        super(listener);
    }

    /**
     * 获取自己名称
     * @return
     */

    private static volatile String hostName;

    @Override
    public String selfHostName() {
        if (null == hostName) {
            synchronized (DefaultConfiguration.class) {
                if (null == hostName) {
                    try {
                        hostName = InetAddress.getLocalHost().getHostName();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        hostName = "";
                    }
                }
            }
        }
        return hostName;
    }

    /**
     * 这里根据自身所有的ip，每个都遍历了后2个网段的所有ip
     * 比如现在的ip为192.168.2.3,则返回192.168.1.1-192.168.254.254所有的ip
     * 且从192.168.2.3开始散发，保证快速找到相近的ip段
     * @return
     */
    @Override
    public Vector<String> broadcastHost() {
        Vector<String> ips = new Vector<>(0, 254);
        try {
            List<String> localIPList = getLocalIPList();
            for (String localIp : localIPList) {
                ips.addAll(distributeIp34(localIp, localIPList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ips;
    }

    /**
     * 获取自身所有ip地址
     * @return
     * @throws UnknownHostException
     */
    private List<String> getLocalIPList() {
        List<String> ipList = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
                        ipList.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        ipList.remove("127.0.0.1");
        return ipList;
    }

    /**
     * 根据一个ip, 返回所有的散发3和4网段的ip。（比如ip为2.2.2.3，则返回[ 2.2.2.4, 2.2.2.5 ... 2.2.2.254, 2.2.2.2, 2.2.2.1, 2.2.3.1, 2.2.3.2 ... 2.2.3.254, 2.2.4.1 ... 2.2.1.254]）
     * @param ip
     * @return
     */
    private List<String> distributeIp34(String ip, List<String> localIPList) {
        List<String> ips = new ArrayList<>();
        String[] ip_split = ip.split("\\.");
        if (ip_split.length == 4) {
//                    ips.addElement("192.168.1.108");
            // 获取第四个网段的所有ip
            List<String> DIP4 = distributeIpParagraph(Integer.parseInt(ip_split[3]));
            for (String d4 : DIP4) {
                String tmp_ip = ip_split[0] + "." + ip_split[1] + "." + ip_split[2] + "." + d4;
                if (!localIPList.contains(tmp_ip))
                    ips.add(tmp_ip);
            }
            // 遍历第3个网段，第4个网段顺序为1-254
            List<String> DIP3 = distributeIpParagraph(Integer.parseInt(ip_split[2]));
            for (String d3 : DIP3) {
                for (Integer i = 1; i <= 255; i++) {
                    String tmp_ip = ip_split[0] + "." + ip_split[1] + "." + d3 + "." + i.toString();
                    if (!localIPList.contains(tmp_ip))
                        ips.add(tmp_ip);
                }
            }
        }
        return ips;
    }

    /**
     * 根据一个ip段，返回所有的散发ip段。（比如ip为56，则返回[ 57, 58 ... 253, 254, 55, 54 ... 2, 1 ]）
     * @param ip
     * @return
     */
    private List<String> distributeIpParagraph(Integer ip) {
        List<String> ips = new ArrayList<>();
        if (ip < 254) {
            for (Integer i = ip + 1; i <= 254; i++) {
                ips.add(i.toString());
            }
        }
        if (ip > 1) {
            for (Integer i = ip - 1; i >= 1; i--) {
                ips.add(i.toString());
            }
        }
        return ips;
    }

    /**
     * 指定广播扫描的端口号
     * @return
     */
    @Override
    public Integer broadcastPort() {
        return 8732;
    }

    /**
     * 因为广播太快了，之前的udp端口都还未来得及关闭，达到上限，所以就挂了，这里就给个最大重试的机会
     * @return
     */
    @Override
    public Integer broadcastRetryMaxCount() {
        return 100;
    }

    /**
     * 命令socket的端口
     * @return
     */
    @Override
    public Integer commandPort() {
        return 8736;
    }

    /**
     * 文件发送socket的端口
     * @return
     */
    @Override
    public Integer sendFilePort() {
        return 8734;
    }

    /**
     * 根据可用的处理器数量*2开启多线程扫描（测过来这个效率最高）
     * @return
     */
    @Override
    public Integer broadcastConcurrentCount() {
        return Runtime.getRuntime().availableProcessors() * 2;
    }

    /**
     * 广播发送标志
     * @return
     */
    @Override
    public String broadcastSendTag() {
        return "FileTransmission_Broadcast_SEND";
    }

    /**
     * 广播回复的标志
     * @return
     */
    @Override
    public String broadcastReceiveTag() {
        return "FileTransmission_Broadcast_RECEIVE";
    }

    /**
     * 发送一个文件用3个线程
     * @return
     */
    @Override
    public Integer sendFileTaskThreadCount() {
        return 3;
    }

    /**
     * 一次性最多发送5个文件
     */
    @Override
    public Integer sendFileTaskMaxCount() {
        return 5;
    }

    @Override
    public String saveFilePath() {
        return "D:\\FileTransmissionCache\\";
    }
}
