package io.datadynamics.client.kerberos;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom JAAS Configuration object for a provided principal that already has a ticket in the ticket cache.
 */
public class TicketCacheConfiguration extends Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketCacheConfiguration.class);

    private final String principal;
    private final String ticketCache;

    private final AppConfigurationEntry ticketCacheConfigEntry;

    public TicketCacheConfiguration(final String principal) {
        this(principal, null);
    }

    public TicketCacheConfiguration(final String principal, final String ticketCache) {
        if (StringUtils.isBlank(principal)) {
            throw new IllegalArgumentException("Principal cannot be null");
        }

        this.principal = principal;
        this.ticketCache = ticketCache;

        final Map<String, String> options = new HashMap<>();
        options.put("principal", principal);
        options.put("refreshKrb5Config", "true");
        options.put("useTicketCache", "true");

        if (StringUtils.isNotBlank(ticketCache)) {
            options.put("ticketCache", ticketCache);
        }

        final String krbLoginModuleName = ConfigurationUtil.IS_IBM
                ? ConfigurationUtil.IBM_KRB5_LOGIN_MODULE : ConfigurationUtil.SUN_KRB5_LOGIN_MODULE;

        LOGGER.debug("krbLoginModuleName: {}, configuration options: {}", krbLoginModuleName, options);
        this.ticketCacheConfigEntry = new AppConfigurationEntry(
                krbLoginModuleName, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return new AppConfigurationEntry[]{ticketCacheConfigEntry};
    }

    public String getPrincipal() {
        return principal;
    }

    public String getTicketCache() {
        return ticketCache;
    }

}
