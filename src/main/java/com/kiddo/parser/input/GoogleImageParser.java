package com.kiddo.parser.input;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will parse the text in a given image using Google Vision API.
 * To save time, we've earlier read some images using vision API and store the results in visian-api-results folder.
 */
public class GoogleImageParser implements ImageParser {
    @Override
    public List<String> parseImage(File file) {
        final List<String> result = new ArrayList<>();
        try (FileReader reader = new FileReader(file)) {
            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(reader);
            JsonArray textAnnotations = root.getAsJsonObject().get("responses").getAsJsonArray().get(0).getAsJsonObject().get("textAnnotations").getAsJsonArray();
            boolean firstLine = false;
            for (JsonElement text : textAnnotations) {
                // skip the first line as it seems like it's the combination of everything.
                if (!firstLine) {
                    firstLine = true;
                    continue;
                }
                result.add(text.getAsJsonObject().get("description").getAsString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
