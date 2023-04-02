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
import com.kiddo.common.ConfigKey;
import com.kiddo.common.ConfigProvider;

import java.io.*;
import java.io.File;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

public class GoogleDriveUploader implements FileUploader {
    private static final String APPLICATION_NAME = "My Application";
    private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private final Drive drive;
    private final int batchSize;

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, final String credentials)
            throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(credentials.getBytes())) {
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("oktaviandi@gmail.com");
        }
    }

    public GoogleDriveUploader(ConfigProvider configProvider) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, configProvider.getConfig(ConfigKey.GOOGLE_CREDENTIAL)))
                .setApplicationName(APPLICATION_NAME)
                .build();
        batchSize = Integer.parseInt(configProvider.getConfig(ConfigKey.BATCH_SIZE));
    }

    @Override
    public void upload(String sourcePath, String destinationPath) throws IOException {
        String parentId = getParentDestination(destinationPath);

        // Upload the folder contents in batches in case the source path has lots of file.
        File folder = new File(sourcePath);
        List<String> parents = Collections.singletonList(parentId);
        List<File> files = Arrays.asList(Objects.requireNonNull(folder.listFiles()));
        int batchCount = (int) Math.ceil((double) files.size() / batchSize);
        for (int i = 0; i < batchCount; i++) {
            int fromIndex = i * batchSize;
            int toIndex = Math.min((i + 1) * batchSize, files.size());
            List<File> batchFiles = new ArrayList<>(files.subList(fromIndex, toIndex));
            List<com.google.api.services.drive.model.File> batchUploadedFiles = batchFiles.stream()
                    .map(f -> {
                        FileContent fileContent = new FileContent(null, f);
                        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                        fileMetadata.setName((f.getName()));
                        fileMetadata.setParents(parents);
                        try {
                            return drive.files().create(fileMetadata, fileContent).execute();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
            System.out.println("Uploaded " + batchUploadedFiles.size() + " files to Google Drive.");
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
