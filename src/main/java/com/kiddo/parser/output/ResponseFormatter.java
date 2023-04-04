package com.kiddo.parser.output;

import com.kiddo.parser.ParseResult;

import java.util.List;

public interface ResponseFormatter {
    void format(ParseResult input, String outputPath);
}
