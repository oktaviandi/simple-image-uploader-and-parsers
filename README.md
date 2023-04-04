# About
The repository has two main classes:
- DriveUpload. This program uploads a directory in the local computer to a folder in Google Drive. 
- ParseTextFromImage. This program reads the result of parsing text from images and output two files; a file containing chinese and english words.

# Project Setup
- The project is created using IntelliJ IDEA.
- The JDK used for this was 20; IntelliJ downloaded the latest when I added the SDK :) It should work for ealier JDK say 11, 17 as there's no newer language construct used. I haven't coded in Java in a while; so take this with a grain of salt :)

## Drive Upload
The program accepts two command line arguments:
- Source folder. The folder in the local computer where we will read the file from.
- Destination folder. The folder to which the file is written.

The source folder can contain any number of files including any number of sub folders. The program will recursively write the files and folders to the destination in Google Drive. If the destination folder doesn't exist, the program will create it.

Uploading the files and / or folder to Google is batched with batch size of 10. This should help if the folder has arbitrary huge numbers of files and / or subfolder. In the future, we can add some sort of wait between each batch to prevent throttling from Google side. Also, batch is stored as an in memory config. Ideally, this will be stored in proper config manager like AWS Parameter Store or GCP Secret Manager.

Running the program multiple times will create multiple files in Google Drive. It's just the way Google Drive works where filename is not unique.

### Prerequisite
- The user needs to create a project in GCP with Google Drive API enabled.
- The user needs to create a credential in GCP, download it and create an environment variable named `GOOGLE_CREDENTIALS` that points to the file. In production environment, this should be stored in config manager like AWS Parameter Store or GCP Secret Manager.

### Running the code
- The code was created using IntelliJ IDEA.
- Just open this in IntelliJ and set the followings:
  - The two command line arguments for source and destination folders.
  - The environment variable that points to Google API credential file.

## ParseTextFromImage
This parses the image from a given folders. Initially, I tried to parse some images in `resources -> original-images` folder using [tesseract](https://github.com/tesseract-ocr/tesseract) which failed in the default configuration to provide any meaningful result. The images are hard to parse in the default configuration due to its complex layout. Here are some examples of parsing the image using Tesseract:

Next, I tried to use Google Vision API to parse the image. I stored the results in the `resources -> vision-api-results` with the naming using the convention `original-file-name.json`.

The code simply accepts an input which is the destination in the local file system where the output will be written. For each input, the program will create two outputs:
- Filename_en.html where filename is the input filename. This contains all the english word parsed from the images. We're using a very simple definitions for English words:
  - Only the character `a` is accepted for a 1 character word.
  - The word cannot consist of any digits or any non character symbol.
- Filename_en.html where filename is the input filename. This contains all the Chinese words parsed from the image. We're using this simple definitions for Chinese words:
  - Use UnicodeBlock class to check if a given word contains any Chinese characters.

Fair to say both word checkers are naive. It'll be simple to swap the implementations with something much better.

### Running the code
- The code was created using IntelliJ IDEA.
- Just open this in IntelliJ and set the followings:
    - The command line argument for the destination folder.

# Improvements
- We could easily add some sort of CLI shell like Spring Shell or [Picocli](https://picocli.info/) to make it easier to run the code.
