package com.dj.transmission.client.command;

public interface OnConnectionListener {
    /**
     * 是否连接成功
     * @param connection
     * @param isSend 判断自己作为哪一方的连接断了，因为自己可以是发送端也可以是接收端
     */
    void onConnection(Boolean connection, Boolean isSend);
}
