package com.kiddo;

import com.kiddo.common.ConfigProvider;
import com.kiddo.common.InmemoryConfigProvider;
import com.kiddo.uploader.FileUploader;
import com.kiddo.uploader.GoogleDriveUploader;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class DriveUpload {
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        // The code needs two arguments; first is the source local folder and the other is the destination folder in Google Drive.
        if (args.length < 2) {
            System.out.println("Please provide source folder and destination folder");
            System.exit(0);
        }
        // Check whether the source folder exists
        File file = new File(args[0]);
        if (!file.exists() || !file.isDirectory()) {
            System.out.println("Source folder doesn't exists in local system");
            System.exit(0);
        }

        ConfigProvider configProvider = new InmemoryConfigProvider();
        FileUploader uploader = new GoogleDriveUploader(configProvider);
        System.out.println("Start Uploading");
        uploader.upload(args[0], args[1]);
        System.out.println("Done uploading");
    }
}
