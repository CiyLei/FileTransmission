package com.dj.transmission.utils;

import com.dj.transmission.file.TransmissionFileInfo;
import com.dj.transmission.file.TransmissionFileSectionInfo;
import org.json.JSONArray;
import org.json.JSONObject;

public class TransmissionJsonConverter {

    /**
     * 封装文件信息为json
     * @param fileInfo
     * @param commandPort
     * @return
     */
    public static String converterFileInfo2Json(TransmissionFileInfo fileInfo, int commandPort) {
        JSONObject jo = new JSONObject();
        jo.put("type", 1);
        JSONObject jo_data = new JSONObject();
        jo_data.put("flieName", fileInfo.getFileName());
        jo_data.put("fileSize", fileInfo.getFileSize());
        jo_data.put("fileHash", fileInfo.getFileHash());
        jo_data.put("commandPort", commandPort);
        jo.put("data", jo_data);
        return jo.toString();
    }

    /**
     * 封装是否同意信息为json
     * @param accept
     * @param sendPort
     * @return
     */
    public static String converterAcceptInfo2Json(boolean accept, int sendPort) {
        JSONObject jo = new JSONObject();
        jo.put("type", 2);
        JSONObject jo_data = new JSONObject();
        jo_data.put("isAccept", accept);
        jo_data.put("sendPort", sendPort);
        jo.put("data", jo_data);
        return jo.toString();
    }

    /**
     * 封装继续传输信息为json
     * @param fileInfo
     * @return
     */
    public static String converterContinueInfo2Json(TransmissionFileInfo fileInfo) {
        JSONObject jo = new JSONObject();
        jo.put("type", 3);
        JSONObject jo_data = new JSONObject();
        jo_data.put("fileHash", fileInfo.getFileHash());
        jo.put("data", jo_data);
        return jo.toString();
    }

    /**
     * 封装传输分块信息为json
     * @param fileInfo
     * @return
     */
    public static String converterSectionInfo2Json(TransmissionFileInfo fileInfo) {
        JSONObject jo = new JSONObject();
        jo.put("type", 4);
        JSONObject jo_data = new JSONObject();
        jo_data.put("fileHash", fileInfo.getFileHash());
        JSONArray ja_sections = new JSONArray();
        for (TransmissionFileSectionInfo sectionInfo : fileInfo.getSectionInfos()) {
            JSONObject jo_section = new JSONObject();
            jo_section.put("startIndex", sectionInfo.getStartIndex());
            jo_section.put("endIndex", sectionInfo.getEndIndex());
            jo_section.put("finishIndex", sectionInfo.getFinishIndex());
            ja_sections.put(jo_section);
        }
        jo_data.put("sections", ja_sections);
        jo.put("data", jo_data);
        return jo.toString();
    }
}
