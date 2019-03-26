package com.dj.transmission.client.transmission.receive;

import com.dj.transmission.client.transmission.TransmissionState;

public interface ReceiveClientStateHandle {
    /**
     * 接收端状态改变
     * @param state
     */
    void receiveClientStateChange(TransmissionState state);
}
