package send;

/**
 * 传输过程中的文件信息
 */
public class TransmissionFileInfo {
    private String fileName;
    private Long fileSize;
    private String fileHash;
    private volatile Long currentSize;

    public TransmissionFileInfo(String fileName, Long fileSize, String fileHash, Long currentSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.currentSize = currentSize;
    }

    public TransmissionFileInfo(String fileName, Long fileSize, String fileHash) {
        this(fileName, fileSize, fileHash, 0l);
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getFileHash() {
        return fileHash;
    }

    public double getProgress() {
        return currentSize.doubleValue() / fileSize.doubleValue();
    }

    public Long getCurrentSize() {
        return currentSize;
    }

    public synchronized void addSize(Long size) {
        currentSize += size;
        System.out.println("p:" + getProgress() * 100);
    }
}
