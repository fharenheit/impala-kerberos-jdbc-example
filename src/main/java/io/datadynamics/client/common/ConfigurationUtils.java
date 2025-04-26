package io.datadynamics.client.common;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;
import java.util.*;

/**
 * Reusable utility class for common {@link Configuration} operations.
 */
public abstract class ConfigurationUtils {

    /**
     * Adds the specified properties to the given {@link Configuration} object.
     *
     * @param configuration configuration to manipulate. Should not be null.
     * @param properties    properties to add to the configuration. May be null.
     */
    public static void addProperties(Configuration configuration, Properties properties) {
        Assert.notNull(configuration, "A non-null configuration is required");
        if (properties != null) {
            Enumeration<?> props = properties.propertyNames();
            while (props.hasMoreElements()) {
                String key = props.nextElement().toString();
                configuration.set(key, properties.getProperty(key));
            }
        }
    }

    /**
     * Creates a new {@link Configuration} based on the given arguments.
     *
     * @param original   initial configuration to read from. May be null.
     * @param properties properties object to add to the newly created configuration. May be null.
     * @return newly created configuration based on the input parameters.
     */
    public static Configuration createFrom(Configuration original, Properties properties) {
        Configuration cfg = null;
        if (original != null) {
            cfg = new Configuration(original);
            addProperties(cfg, properties);
        } else {
            cfg = new Configuration();
            addProperties(cfg, properties);
        }
        return cfg;
    }

    /**
     * Returns a static {@link Properties} copy of the given configuration.
     *
     * @param configuration Hadoop configuration
     * @return properties
     */
    public static Properties asProperties(Configuration configuration) {
        Properties props = new Properties();

        if (configuration != null) {
            for (Map.Entry<String, String> entry : configuration) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
        }

        return props;
    }

    /**
     * Creates a new {@link Configuration} by merging the given configurations.
     * Ordering is important - the second configuration overriding values in the first.
     *
     * @param one configuration to read from. May be null.
     * @param two configuration to read from. May be null.
     * @return the result of merging the two configurations.
     */
    public static Configuration merge(Configuration one, Configuration two) {
        if (one == null) {
            if (two == null) {
                return new Configuration();
            }
            return new Configuration(two);
        }

        Configuration c = new Configuration(one);

        if (two == null) {
            return c;
        }

        for (Map.Entry<String, String> entry : two) {
            c.set(entry.getKey(), entry.getValue());
        }

        return c;
    }

    public static void addLibs(Configuration configuration, Resource... libs) {
        addResource(configuration, libs, "-libjars");
    }

    public static void addFiles(Configuration configuration, Resource... files) {
        addResource(configuration, files, "-files");
    }

    public static void addArchives(Configuration configuration, Resource... archives) {
        addResource(configuration, archives, "-archives");
    }

    private static void addResource(Configuration cfg, Resource[] args, String name) {
        Assert.notNull(cfg, "a non-null configuration is required");

        List<String> list = new ArrayList<String>();

        try {
            if (!ObjectUtils.isEmpty(args)) {
                int count = args.length;
                list.add(name);

                StringBuilder sb = new StringBuilder();
                for (Resource res : args) {
                    sb.append(res.getURI().toString());
                    if (--count > 0) {
                        sb.append(",");
                    }
                }
                list.add(sb.toString());
            }

            new GenericOptionsParser(cfg, list.toArray(new String[list.size()]));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}