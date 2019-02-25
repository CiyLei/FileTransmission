package client;

import java.io.File;

public class FileInfo {
    private File file;
    private String fileHashValue;

    public FileInfo(File file, String fileHashValue) {
        this.file = file;
        this.fileHashValue = fileHashValue;
    }

    public File getFile() {
        return file;
    }

    public String getFileHashValue() {
        return fileHashValue;
    }
}
