package com.kiddo.parser.input;

import java.io.File;
import java.util.List;

public interface ImageParser {
    List<String> parseImage(File file);
}
