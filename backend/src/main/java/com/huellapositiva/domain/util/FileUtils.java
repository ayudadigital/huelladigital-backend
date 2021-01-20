package com.huellapositiva.domain.util;

public class FileUtils {

    private FileUtils() {

    }

    /**
     * Get the extension of the fileName
     *
     * @param fileName Name of file to upload to extract its extension
     * @return the extension or an empty string when there is no extension
     */
    public static String getExtension(String fileName) {
        if (fileName != null) {
            int index = fileName.lastIndexOf('.');
            return index != -1  && fileName.substring(index).trim().length() > 1 ? fileName.substring(index) : "";
        }
        return "";
    }
}
