package com.kiddo.parser;

import java.util.List;

public class ParseResult {
    String filename;
    List<String> words;

    public ParseResult(String filename, List<String> words) {
        this.filename = filename;
        this.words = words;
    }

    public String getFilename() {
        return filename;
    }

    public List<String> getWords() {
        return words;
    }
}
