package com.huellapositiva.infrastructure;

import java.io.InputStream;
import java.net.URL;

public interface StorageService {

    URL upload(String key, InputStream inputStream, String contentType);
}
