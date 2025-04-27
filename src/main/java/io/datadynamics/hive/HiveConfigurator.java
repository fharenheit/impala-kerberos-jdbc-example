package io.datadynamics.hive;

import io.datadynamics.client.kerberos.KerberosUser;
import io.datadynamics.client.kerberos.SecurityUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class HiveConfigurator {

    /**
     * Loads a HiveConf configuration from a set of configuration files.
     * Each file path in the provided input is processed and loaded into the
     * HiveConf instance.
     *
     * @param configFiles Comma-separated list of configuration file paths.
     *                    Each file is expected to be a valid Hadoop configuration resource.
     *                    If the input is blank or null, no files are loaded.
     * @return A HiveConf instance populated with settings from the specified configuration files.
     */
    public HiveConf getConfigurationFromFiles(final String configFiles) {
        final HiveConf hiveConfig = new HiveConf();
        if (StringUtils.isNotBlank(configFiles)) {
            for (final String configFile : configFiles.split(",")) {
                hiveConfig.addResource(new Path(configFile.trim()));
            }
        }
        return hiveConfig;
    }

    /**
     * Preloads the given Hadoop {@link Configuration} by initializing necessary resources
     * and setting the configuration to the security context.
     * This method ensures that the {@link FileSystem} and {@link UserGroupInformation}
     * are properly configured for the provided configuration to prevent potential issues
     * during its usage.
     *
     * @param configuration The Hadoop configuration object to preload. This configuration
     *                      will be set to the {@link UserGroupInformation} and the associated
     *                      {@link FileSystem} resources will be initialized and closed.
     */
    public void preload(Configuration configuration) {
        try {
            FileSystem.get(configuration).close();
            UserGroupInformation.setConfiguration(configuration);
        } catch (IOException ioe) {
            // Suppress exception as future uses of this configuration will fail
        }
    }

    /**
     * Acquires a {@link UserGroupInformation} using the given {@link Configuration} and {@link KerberosUser}.
     *
     * @param hiveConfig   The Configuration to apply to the acquired UserGroupInformation
     * @param kerberosUser The KerberosUser to authenticate
     * @return A UserGroupInformation instance created using the Subject of the given KerberosUser
     * @throws AuthenticationFailedException if authentication fails
     * @see SecurityUtil#getUgiForKerberosUser(Configuration, KerberosUser)
     */
    public UserGroupInformation authenticate(final Configuration hiveConfig, KerberosUser kerberosUser) throws AuthenticationFailedException {
        try {
            return SecurityUtil.getUgiForKerberosUser(hiveConfig, kerberosUser);
        } catch (IOException ioe) {
            throw new AuthenticationFailedException("Kerberos Authentication for Hive failed", ioe);
        }
    }
}
