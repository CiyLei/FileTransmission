package com.dj.transmission.client.transmission.send;

import com.dj.transmission.client.transmission.TransmissionState;

public interface SendClientStateHandle {
    /**
     * 发送端状态改变
     * @param state
     */
    void sendClientStateChange(TransmissionState state);
}
