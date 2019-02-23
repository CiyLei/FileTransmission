package config;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class DefaultConfiguration implements Configuration {

    /**
     * 这里遍历了后2个网段的所有ip
     * 比如现在的ip为192.168.2.3,则返回192.168.1.1-192.168.255.255所有的ip
     * 且从192.168.2.3开始散发，保证快速找到相近的ip段
     * @return
     */
    @Override
    public Vector<String> broadcastHost() {
        Vector<String> ips = new Vector<>(0, 255);
        try {
            List<String> localIPList = getLocalIPList();
            if (localIPList.size() > 0) {
                String selfIp = localIPList.get(0);
                String[] ip_split = selfIp.split("\\.");
                if (ip_split.length == 4) {
                    // 获取第四个网段的所有ip
                    List<String> DIP4 = distributeIpParagraph(Integer.parseInt(ip_split[3]));
                    for (String d4 : DIP4) {
                        String ip = ip_split[0] + "." + ip_split[1] + "." + ip_split[2] + "." + d4;
                        if (!localIPList.contains(ip))
                            ips.addElement(ip);
                    }
                    // 遍历第3个网段，第4个网段顺序为1-255
                    List<String> DIP3 = distributeIpParagraph(Integer.parseInt(ip_split[2]));
                    for (String d3 : DIP3) {
                        for (Integer i = 1; i <= 255; i++) {
                            String ip = ip_split[0] + "." + ip_split[1] + "." + d3 + "." + i.toString();
                            if (!localIPList.contains(ip))
                                ips.addElement(ip);
                        }
                    }
                }
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
        return ipList;
    }

    /**
     * 根据一个ip端，返回所有的散发ip端。（比如ip为56，则返回[57,58...255,55,54...1]）
     * @param ip
     * @return
     */
    private List<String> distributeIpParagraph(Integer ip) {
        List<String> ips = new ArrayList<>();
        if (ip < 255) {
            for (Integer i = ip + 1; i <= 255; i++) {
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
}
