package com.kiddo.common.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A naive in memory config provider. In reality this will be stored in some sort of stores for example AWS Parameter Store, GCP System Parameters.
 */
public class InmemoryConfigProvider implements ConfigProvider {
    @Override
    public String getConfig(ConfigKey key) {
        switch (key) {
            case GOOGLE_CREDENTIAL -> {
                String fileName = System.getenv("GOOGLE_CREDENTIALS");
                try {
                    return Files.readString(Paths.get(fileName), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case BATCH_SIZE -> {
                return "10";
            }
        }
        throw new RuntimeException("Config " + key.name() + " can not be processed");
    }
}
