package io.datadynamics.client.kerberos;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom JAAS Configuration object for a provided principal and keytab.
 */
public class KeytabConfiguration extends Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeytabConfiguration.class);

    private final String principal;
    private final String keytabFile;

    private final AppConfigurationEntry kerberosKeytabConfigEntry;

    public KeytabConfiguration(final String principal, final String keytabFile) {
        if (StringUtils.isBlank(principal)) {
            throw new IllegalArgumentException("Principal cannot be null");
        }

        if (StringUtils.isBlank(keytabFile)) {
            throw new IllegalArgumentException("Keytab file cannot be null");
        }

        this.principal = principal;
        this.keytabFile = keytabFile;

        final Map<String, String> options = new HashMap<>();
        options.put("principal", principal);
        options.put("refreshKrb5Config", "true");

        if (ConfigurationUtil.IS_IBM) {
            options.put("useKeytab", keytabFile);
            options.put("credsType", "both");
        } else {
            options.put("keyTab", keytabFile);
            options.put("useKeyTab", "true");
            options.put("isInitiator", "true");
            options.put("doNotPrompt", "true");
            options.put("storeKey", "true");
        }

        final String krbLoginModuleName = ConfigurationUtil.IS_IBM
                ? ConfigurationUtil.IBM_KRB5_LOGIN_MODULE : ConfigurationUtil.SUN_KRB5_LOGIN_MODULE;

        LOGGER.debug("krbLoginModuleName: {}, configuration options: {}", krbLoginModuleName, options);
        this.kerberosKeytabConfigEntry = new AppConfigurationEntry(
                krbLoginModuleName, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return new AppConfigurationEntry[]{kerberosKeytabConfigEntry};
    }

    public String getPrincipal() {
        return principal;
    }

    public String getKeytabFile() {
        return keytabFile;
    }

}
