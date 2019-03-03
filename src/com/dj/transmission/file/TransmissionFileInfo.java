package com.dj.transmission.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class TransmissionFileInfo {
    private String fileName;
    private Long fileSize;
    private String fileHash;
    private File file;
    private final Vector<TransmissionFileSectionInfo> sectionInfos = new Vector<>();

    /**
     * 接收端是不知道具体目录的，只知道文件名称和大小，所以用这个构造方法
     * @param fileName
     * @param fileSize
     * @param fileHash
     */
    public TransmissionFileInfo(String fileName, Long fileSize, String fileHash) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
    }

    public TransmissionFileInfo(File file, String fileHash) {
        this(file.getName(), file.length(), fileHash);
        this.file = file;
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

    public List<TransmissionFileSectionInfo> getSectionInfos() {
        return sectionInfos;
    }

    public File getFile() {
        return file;
    }

    public double getProgress() {
        Long sunSize = 0l;
        synchronized (sectionInfos) {
            for (TransmissionFileSectionInfo sectionFileInfo : sectionInfos) {
                sunSize += sectionFileInfo.getFinishIndex() - sectionFileInfo.getStartIndex();
            }
        }
        return sunSize.doubleValue() / fileSize.doubleValue();
    }
}
