package send;


import java.util.ArrayList;
import java.util.List;

/**
 * 传输过程中的文件信息
 */
public class TransmissionFileInfo {
    private String fileName;
    private Long fileSize;
    private String fileHash;
    private List<TransmissionSectionFileInfo> sectionFileInfos;

    public TransmissionFileInfo(String fileName, Long fileSize, String fileHash, List<TransmissionSectionFileInfo> sectionFileInfos) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.sectionFileInfos = sectionFileInfos;
    }

    public TransmissionFileInfo(String fileName, Long fileSize, String fileHash) {
        this(fileName, fileSize, fileHash, new ArrayList<>());
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
        Long sunSize = 0l;
        for (TransmissionSectionFileInfo sectionFileInfo : sectionFileInfos) {
            sunSize += sectionFileInfo.getFinishIndex() - sectionFileInfo.getStartIndex();
        }
        return sunSize.doubleValue() / fileSize.doubleValue();
    }

    public List<TransmissionSectionFileInfo> getSectionFileInfos() {
        return sectionFileInfos;
    }

    public void setSectionFileInfos(List<TransmissionSectionFileInfo> sectionFileInfos) {
        this.sectionFileInfos = sectionFileInfos;
    }
}
