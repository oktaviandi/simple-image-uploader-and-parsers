package com.kiddo;

import com.kiddo.common.ConfigProvider;
import com.kiddo.common.InmemoryConfigProvider;
import com.kiddo.uploader.FileUploader;
import com.kiddo.uploader.GoogleDriveUploader;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class DriveUpload {
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        ConfigProvider configProvider = new InmemoryConfigProvider();
        FileUploader uploader = new GoogleDriveUploader(configProvider);
        System.out.println("Start Uploading");
        uploader.upload("/Users/oktaviandi/Desktop/DriveUploader", "DriveUploader");
        System.out.println("Done uploading");
    }
}
