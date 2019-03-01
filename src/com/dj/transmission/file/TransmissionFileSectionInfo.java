package com.dj.transmission.file;

public class TransmissionFileSectionInfo {
    private Long startIndex;
    private Long endIndex;
    private Long finishIndex;

    public TransmissionFileSectionInfo(Long startIndex, Long endIndex, Long finishIndex) {
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
}
