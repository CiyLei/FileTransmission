package com.dj.transmission.client.command.receive;

/**
 * 接收文件控制器
 */
public interface AcceptController {
    /**
     * 同意接收
     */
    void accept();

    /**
     * 拒绝接收
     */
    void reject();
}
