package com.huellapositiva.infrastructure;

import java.io.File;
import java.net.URL;

public interface StorageService {

    URL upload(File file, String key);

}
