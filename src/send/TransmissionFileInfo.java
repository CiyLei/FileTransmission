package send;

/**
 * 传输过程中的文件信息
 */
public class TransmissionFileInfo {
    private String fileName;
    private Long fileSize;
    private String fileHash;
    private double progress;

    public TransmissionFileInfo(String fileName, Long fileSize, String fileHash, double progress) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.progress = progress;
    }

    public TransmissionFileInfo(String fileName, Long fileSize, String fileHash) {
        this(fileName, fileSize, fileHash, 0.0);
    }

    public void setProgress(double progress) {
        this.progress = progress;
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
        return progress;
    }
}
