package com.dj.transmission.client.transmission.receive;

import com.dj.transmission.client.TransmissionClient;
import com.dj.transmission.client.transmission.TransmissionState;

import java.util.ArrayList;
import java.util.List;

public class ReceiveFileDataController {
    private TransmissionClient client;
    private ReceiveClientStateHandle receiveClientStateHandle;
    private List<ReceiveFileDataTask> receiveFileDataTasks = new ArrayList<>();
    /**
     * 作为一个接收端，文件是分段传过来的，如果在接收到一个socket就改变状态的话，有这么一种情况
     * 文件被分割了5段，在收到前3段的socket的时候，用户暂停了接收，这时候文件后2段又传了过来，那么状态就又变成了开始接收的
     * 所以这里有一个文件hash值得标志符，只有在第一次hashFlag等于null或者传过来的hash不一样的时候，才能改变开始状态
     */
    private String hashFlag;

    public ReceiveFileDataController(TransmissionClient client, ReceiveClientStateHandle receiveClientStateHandle) {
        this.client = client;
        this.receiveClientStateHandle = receiveClientStateHandle;
    }

    public void addReceiveFileDataTask(ReceiveFileDataTask task) {
        receiveFileDataTasks.add(task);
        client.getFileTransmission().sendFilePool().execute(task);
    }

    public void close() {
        for (ReceiveFileDataTask task : receiveFileDataTasks) {
            task.close();
        }
        receiveClientStateHandle.receiveClientStateChange(TransmissionState.PAUSE);
    }

    public void verificationStart(String hash) {
        if (hashFlag == null || !hashFlag.equals(hash)) {
            hashFlag = hash;
            receiveClientStateHandle.receiveClientStateChange(TransmissionState.START);
        }
    }

    public List<ReceiveFileDataTask> getReceiveFileDataTasks() {
        return receiveFileDataTasks;
    }

    public ReceiveClientStateHandle getReceiveClientStateHandle() {
        return receiveClientStateHandle;
    }
}
