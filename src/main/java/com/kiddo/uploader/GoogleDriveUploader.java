package com.kiddo.uploader;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.File;
import com.kiddo.common.ConfigKey;
import com.kiddo.common.ConfigProvider;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GoogleDriveUploader implements FileUploader {
    private static final String APPLICATION_NAME = "KiddoUploader";
    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final Set<String> SCOPES = DriveScopes.all();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private final Drive drive;
    private final int batchSize;

    public GoogleDriveUploader(ConfigProvider configProvider) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, configProvider.getConfig(ConfigKey.GOOGLE_CREDENTIAL)))
                .setApplicationName(APPLICATION_NAME)
                .build();
        batchSize = Integer.parseInt(configProvider.getConfig(ConfigKey.BATCH_SIZE));
    }

    /**
     * Upload the content of source path to destination path in Google Drive. This copy both files and subfolders.
     * @param sourcePath Local folder.
     * @param destinationPath Destination folder in Drive. It'll be created if it doesn't exists.
     * @throws IOException
     */
    @Override
    public void upload(String sourcePath, String destinationPath) throws IOException {
        String parentId = getParentDestination(destinationPath);
        java.io.File sourceFolder = new java.io.File(sourcePath);
        uploadFilesFromFolder(sourceFolder, parentId);
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, final String credentials)
            throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(credentials.getBytes())) {
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                    clientSecrets, SCOPES).build();
            Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(APPLICATION_NAME);

            return credential;
        }
    }

    /*
     *  Recursively upload files from source folder to destination folder in Google Drive.
     *  The folder hierarchy will be maintained. Parallel processing is used here to cater for cases where there're many
     *  files within a folder. This should help from getting throttled from Google as well.
     */

    private void uploadFilesFromFolder(java.io.File folder, String folderId) throws IOException {
        java.io.File[] files = folder.listFiles();
        final List<String> parent = Collections.singletonList(folderId);
        if (files != null) {
            List<java.io.File> fileList = Arrays.asList(files);
            IntStream.range(0, (int) Math.ceil((double) fileList.size() / batchSize))
                    .parallel()
                    .forEach(batch -> {
                        List<java.io.File> batchList = fileList.subList(batch * batchSize, Math.min((batch + 1) * batchSize, fileList.size()));
                        batchList.forEach(file -> {
                            if (file.isDirectory()) {
                                // Recursive call to handle sub-folder
                                File subFolderMetadata = new File();
                                subFolderMetadata.setName(file.getName());
                                subFolderMetadata.setMimeType(FOLDER_MIME_TYPE);
                                subFolderMetadata.setParents(parent);
                                try {
                                    File subFolder = drive.files().create(subFolderMetadata).execute();
                                    System.out.println("Folder " + file.getName() + " created successfully!");
                                    uploadFilesFromFolder(file, subFolder.getId());
                                } catch (IOException e) {
                                    System.err.println("Error creating sub-folder " + file.getName() + ": " + e.getMessage());
                                }
                            } else {
                                com.google.api.services.drive.model.File fileMetadata = new File();
                                fileMetadata.setName(file.getName());
                                fileMetadata.setParents(parent);
                                FileContent mediaContent = new FileContent(null, file);
                                try {
                                    File uploadedFile = drive.files().create(fileMetadata, mediaContent).execute();
                                    System.out.println("File " + uploadedFile.getName() + " uploaded successfully!");
                                } catch (IOException e) {
                                    System.err.println("Error uploading file " + file.getName() + ": " + e.getMessage());
                                }
                            }
                        });
                    });
        }
    }

    /**
     * Create folder in Google Drive if not exists. Otherwise, get the folder id.
     * @param destinationPath Destination folder in Google Drive.
     * @return folder id of the destination that we'll set as parent folder.
     * @throws IOException
     */
    private String getParentDestination(String destinationPath) throws IOException {
        // Find the folder we want to upload to
        String result = null;
        final FileList files = drive.files().list()
                .setQ("mimeType='" + FOLDER_MIME_TYPE + "' and trashed = false and name='" + destinationPath + "'")
                .execute();

        // TODO: needs to check what happens if there are multiple folders
        for (com.google.api.services.drive.model.File file : files.getFiles()) {
            result = file.getId();
        }

        // Create the folder if it doesn't exist
        if (result == null) {
            com.google.api.services.drive.model.File folder = new com.google.api.services.drive.model.File();
            folder.setName(destinationPath);
            folder.setMimeType(FOLDER_MIME_TYPE);
            com.google.api.services.drive.model.File createdFolder = drive.files().create(folder).execute();
            result = createdFolder.getId();
        }
        return result;
    }
}
