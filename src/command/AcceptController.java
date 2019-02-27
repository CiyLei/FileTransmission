package command;


import send.TransmissionFileInfo;

/**
 * 是否同意接收文件的控制器
 */
public interface AcceptController {
    public void accept(TransmissionFileInfo transmissionFileInfo);
    public void reject();
}
