package com.kiddo;


import java.io.File;
import java.io.FileNotFoundException;

import com.kiddo.parser.TextParserService;
import com.kiddo.parser.input.GoogleImageParser;
import com.kiddo.parser.input.ImageParser;
import com.kiddo.parser.output.HtmlResponseFormatter;
import com.kiddo.parser.output.ResponseFormatter;
import com.kiddo.parser.verifier.NaiveWordVerifier;
import com.kiddo.parser.verifier.WordVerifier;

public class ParseTextFromImage {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 1) {
            System.out.println("Please provide destination folder to which output will be written");
            System.exit(0);
        }
        // Check whether the source folder exists
        File file = new File(args[0]);
        if (!file.exists() || !file.isDirectory()) {
            System.out.println("Destination folder doesn't exists in local system");
            System.exit(0);
        }

        ImageParser imageParser = new GoogleImageParser();
        WordVerifier wordVerifier = new NaiveWordVerifier();
        ResponseFormatter responseFormatter = new HtmlResponseFormatter(wordVerifier);
        TextParserService service = new TextParserService(imageParser, responseFormatter);
        service.parseText("vision-api-results", args[0]);
    }
}
