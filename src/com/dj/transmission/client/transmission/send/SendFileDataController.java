package com.dj.transmission.client.transmission.send;

import com.dj.transmission.client.TransmissionClient;
import com.dj.transmission.client.transmission.TransmissionState;
import com.dj.transmission.file.TransmissionFileInfo;

import java.util.ArrayList;
import java.util.List;

public class SendFileDataController {
    private TransmissionClient client;
    private String hostAddress;
    private Integer port;
    private TransmissionFileInfo sendFileInfo;
    private SendClientStateHandle sendClientStateHandle;
    private List<SendFileDataTask> tasks = new ArrayList<>();

    public SendFileDataController(TransmissionClient client, String hostAddress, Integer port, TransmissionFileInfo sendFileInfo, SendClientStateHandle sendClientStateHandle) {
        this.client = client;
        this.hostAddress = hostAddress;
        this.port = port;
        this.sendFileInfo = sendFileInfo;
        this.sendClientStateHandle = sendClientStateHandle;
    }

    public void start() {
        tasks.clear();
        sendClientStateHandle.sendClientStateChange(TransmissionState.START);
        for (int i = 0; i < sendFileInfo.getSectionInfos().size(); i++) {
            SendFileDataTask sendFileDataTask = new SendFileDataTask(client, sendFileInfo, i, hostAddress, port, this);
            tasks.add(sendFileDataTask);
            client.getFileTransmission().sendFilePool().execute(sendFileDataTask);
        }
    }

    public void close() {
        for (SendFileDataTask task : tasks) {
            task.close();
        }
        sendClientStateHandle.sendClientStateChange(TransmissionState.PAUSE);
    }

    public SendClientStateHandle getSendClientStateHandle() {
        return sendClientStateHandle;
    }
}
