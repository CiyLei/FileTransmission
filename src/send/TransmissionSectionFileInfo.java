package send;

/**
 * 被分割文件的信息
 */
public class TransmissionSectionFileInfo {
    private Long startIndex;
    private Long endIndex;
    private Long finishIndex;

    public TransmissionSectionFileInfo(Long startIndex, Long endIndex, Long finishIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.finishIndex = finishIndex;
    }

    public Long getStartIndex() {
        return startIndex;
    }

    public Long getEndIndex() {
        return endIndex;
    }

    public Long getFinishIndex() {
        return finishIndex;
    }

    public void setFinishIndex(Long finishIndex) {
        this.finishIndex = finishIndex;
    }

    @Override
    public String toString() {
        return "TransmissionSectionFileInfo{" +
                "startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                ", finishIndex=" + finishIndex +
                '}';
    }
}
