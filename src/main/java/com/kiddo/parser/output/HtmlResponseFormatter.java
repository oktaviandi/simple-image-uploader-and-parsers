package com.kiddo.parser.output;

import com.kiddo.parser.ParseResult;
import com.kiddo.parser.verifier.LanguageType;
import com.kiddo.parser.verifier.WordVerifier;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class HtmlResponseFormatter implements ResponseFormatter {
    private final WordVerifier wordVerifier;

    public HtmlResponseFormatter(WordVerifier wordVerifier) {
        this.wordVerifier = wordVerifier;
    }

    @Override
    public void format(ParseResult input , String outputPath) {
        final String fileName = input.getFilename().split("\\.")[0];
        final String chineseName = outputPath + fileName + "_" + "chi.html";
        final String englishName = outputPath + fileName + "_" + "en.html";
        List<String> englishWords = input.getWords().stream().filter(word -> wordVerifier.isWordValid(word, LanguageType.ENGLISH)).toList();
        List<String> chineseWords = input.getWords().stream().filter(word -> wordVerifier.isWordValid(word, LanguageType.CHINESE)).toList();
        writeResponse(englishWords, englishName, true);
        writeResponse(chineseWords, chineseName, false);
    }

    private void writeResponse(List<String> words, String outputFileName, boolean isEnglish) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            writer.write("<html>\n<head>\n<title>Word List</title>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n</head>\n<body>\n<ul>\n");
            if (words.size() > 0) {
                for (String s : words) {
                    // TODO: remove this ugly hack for.
                    if (isEnglish) {
                        if (s.contains("o")) {
                            writer.write("<li style=\"color:blue\">" + s + "</li>\n");
                        } else {
                            writer.write("<li>" + s + "</li>\n");
                        }
                    } else {
                        writer.write("<li>" + s + "</li>\n");
                    }
                }
            } else {
                writer.write("<p>No " + (isEnglish ? "English" : "Chinese") + " word found<p>");
            }
            writer.write("</ul>\n</body>\n</html>");
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file " + outputFileName, e);
        }
    }
}
