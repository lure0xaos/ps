package gargoyle.ps.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class Config {

    private Properties properties = new Properties();

    private Config(URL config) {
        properties = new Properties();
        try (InputStream stream = config.openStream()) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Config load(URL config) {
        return new Config(config);
    }

    public String get(String key, String def) {
        return properties.getProperty(key, def);
    }

    public int get(String key, int def) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
