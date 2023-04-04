package com.kiddo.parser;

import com.kiddo.parser.input.GoogleImageParser;
import com.kiddo.parser.input.ImageParser;
import com.kiddo.parser.output.ResponseFormatter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.Files.walk;

public class TextParserService {
    private final ImageParser imageParser;
    private final ResponseFormatter responseFormatter;

    public TextParserService(ImageParser imageParser, ResponseFormatter responseFormatter) {
        this.imageParser = imageParser;
        this.responseFormatter = responseFormatter;
    }

    /**
     * Given an input path, it will process each file in the path and create two files in the output path.
     * An english output that contains words in English.
     * A Chinese output that contains words in Chinese.
     * @param inputPath The path where the result of the image parsing is stored.
     * @param outputPath The supplied user output where the output will be stored.
     */
    public void parseText(String inputPath, String outputPath) {
        final List<ParseResult> result = new ArrayList<>();
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(inputPath);
        try {
            List<File> files = walk(Paths.get(resource.toURI()))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .toList();

            files.forEach(aFile -> {
                List<String> strings = imageParser.parseImage(aFile);
                responseFormatter.format(new ParseResult(aFile.getName(), strings), outputPath);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
