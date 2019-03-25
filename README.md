# FileTransmission

多线程，断点续传，局域网文件传输

### TransmissionConfig

可继承这个类做相关的自定义配置

|<b>方法</b>|<b>默认值</b>|<b>说明</b>|
| :------- | :-------: | :-------|
|`Boolean isDebug()`|`true`|决定是否打印报错日志|
|`String stringEncode()`|`"utf-8"`|默认编码|
|`String hostName()`|`本机localhost名称`|获取自己名称（暂时没用到）|
|`Integer commandPort()`|`8736`|命令通信的socket端口|
|`Integer sendFilePort()`|`9082`|文件发送的socket端口|
|`Integer sendFileTaskThreadCount()`|`5`|发送一个文件用多少个线程|
|`Integer sendFileTaskMaxCount()`|`3`|一次性最多发送几个文件|
|`String saveFilePath()`|`D:\FileTransmissionCache`|默认文件接收保存路径|
|`Integer sendFileUpdateFrequency()`|`1000`|发送文件的时候，回调更新的频率|

### TransmissionScheduler

所有的回调方法都会经过这个方法间接执行

```Java
public class TransmissionScheduler {
    
    public void run(Runnable scheduler) {
        scheduler.run();
    }
}
```

### FileTransmission

入口方法，初始化方法参数接收`TransmissionConfig`和`TransmissionScheduler`

```Java
public FileTransmission(TransmissionConfig config) throws IOException 
public FileTransmission(TransmissionConfig config, TransmissionScheduler scheduler) throws IOException
```

|<b>方法</b>|<b>说明</b>|
| :------- | :-------|
|`TransmissionClient createOrGetClient(String hostAddress, Integer commandPort)`|通过此方法创建一个`TransmissionClient`|
|`void addOnClienListeners(OnClienListener onClienListener)`|监听客户端全局的回调|

### TransmissionAdapter

针对不同平台的适配， `FileTransmission`是实现了这个接口类

|<b>方法</b>|<b>默认值</b>|<b>说明</b>|
| :------- | :-------: | :-------|
|`String encodeString(String)`|使用Base64编码|传输过程中对文件名称的编码|
|`String decodeString(String)`|使用Base64解码|传输过程中对文件名称的解码|
|`Boolean isMainThread()`|判断是否在创建`FileTransmission`的线程上|判断是否在主线程|
|`ExecutorService sendFilePool()`|`TransmissionConfig.sendFileTaskThreadCount() * TransmissionConfig.sendFileTaskMaxCount()`|发送文件的线程池|
|`ExecutorService commandPool()`|`Executors.newCachedThreadPool()`|发送命令socket的线程池|

### TransmissionClient

作为一个发送端或者接收端的抽象，请通过`TransmissionClient.createOrGetClient`方法创建此对象。

|<b>方法</b>|<b>说明</b>|
| :------- | :-------|
|`void sendFile`|发送文件|
|`void pauseSend`|暂停发送文件|
|`void continueSend`|继续发送文件|
|`TransmissionFileInfo getSendFileInfo`|获取发送文件的信息|
|`TransmissionState getSendState`|获取发送文件的状态|
|`void pauseReceive`|暂停接收文件|
|`void continueReceive`|继续接收文件|
|`TransmissionFileInfo getReceiveFileInfo`|获取接收文件的信息|
|`TransmissionState getReceiveState`|获取接收文件的状态|
|`TransmissionState getReceiveState`|获取接收文件的状态|
|`void addOnSendClientListener(OnSendClientListener)`|添加作为发送端端的回调|
|`void addOnReceiveClientListeners(OnReceiveClientListener)`|添加作为接收端的回调|

### TransmissionFileInfo

发送/接收的文件信息

* `String fileName` 文件名称
* `Long fileSize` 文件大小
* `String fileHash` 文件hash值
* `File file` 文件
* `Vector<TransmissionFileSectionInfo> sectionInfos` 文件分块信息
    * `Long startIndex` 分块文件的起始地址
    * `Long endIndex` 分块文件的结束地址
    * `Long finishIndex` 分块的完成进度地址
    
### TransmissionState

发送/接收的文件状态

* `START` 传输中
* `PAUSE` 暂停中

### AcceptController

是否同意接收文件的控制器

|<b>方法</b>|<b>说明</b>|
| :------- | :-------|
|`void accept()`|同意接收|
|`void reject()`|拒绝接收|

### OnClienListener

客户端全局的事件回调，就监听是否有文件传输过来了

`public abstract void onReceiveFileInfo`
* `TransmissionClient client` 客户端对象（判断是哪个客户端传过来的）
* `TransmissionFileInfo fileInfo` 传过来的文件信息
* `AcceptController controller` 是否同意的控制器

### OnSendClientListener

作为发送端的回调

`void onAccept` 对方是否同意接收的回调
* `Boolean accept` `true`为同意，反之拒绝

`void onProgress` 发送文件的进度回调
* `double progress` `0.0` - `1.0`（`1.0`即发送完毕）

`void onStateChange` 发送文件的状态改变回调
* `TransmissionState state` 

### OnReceiveClientListener

作为接收端的回调

`void onProgress` 接收文件的进度回调
* `double progress` `0.0` - `1.0`（`1.0`即接收完毕）

`void onStateChange` 接收文件的状态改变回调
* `TransmissionState state` 