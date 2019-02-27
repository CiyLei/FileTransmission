package send;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 传输过程中的文件信息
 */
public class TransmissionFileInfo {
    private String fileName;
    private Long fileSize;
    private String fileHash;
    private Vector<TransmissionSectionFileInfo> sectionFileInfos;

    public TransmissionFileInfo(String fileName, Long fileSize, String fileHash, Vector<TransmissionSectionFileInfo> sectionFileInfos) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.sectionFileInfos = sectionFileInfos;
    }

    public TransmissionFileInfo(String fileName, Long fileSize, String fileHash) {
        this(fileName, fileSize, fileHash, new Vector<>());
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

    public synchronized double getProgress() {
        Long sunSize = 0l;
        for (TransmissionSectionFileInfo sectionFileInfo : sectionFileInfos) {
            sunSize += sectionFileInfo.getFinishIndex() - sectionFileInfo.getStartIndex();
        }
        return sunSize.doubleValue() / fileSize.doubleValue();
    }

    public List<TransmissionSectionFileInfo> getSectionFileInfos() {
        return sectionFileInfos;
    }

    public void setSectionFileInfos(Vector<TransmissionSectionFileInfo> sectionFileInfos) {
        this.sectionFileInfos = sectionFileInfos;
    }
}
