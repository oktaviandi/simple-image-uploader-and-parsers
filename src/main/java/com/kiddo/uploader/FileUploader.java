package com.kiddo.uploader;

import java.io.IOException;

public interface FileUploader {
    void upload(String sourcePath, String destinationPath) throws IOException;
}
