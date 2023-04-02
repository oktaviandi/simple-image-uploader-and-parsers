package com.kiddo.common;

/**
 * A naive in memory config provider. In reality this will be stored in some sort of stores for example AWS Parameter Store, GCP System Parameters.
 */
public class InmemoryConfigProvider implements ConfigProvider {
    @Override
    public String getConfig(ConfigKey key) {
        System.out.println("Get config for " + key.name());
        switch (key) {
            case GOOGLE_CREDENTIAL -> {
                return "hahaha";
            }
            case BATCH_SIZE -> {
                return "10";
            }
        }
        return null;
    }
}
