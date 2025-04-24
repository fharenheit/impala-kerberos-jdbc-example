package io.datadynamics.client.kerberos;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.util.HashMap;

/**
 * JAAS Configuration to use when logging in with username/password.
 */
public class PasswordConfiguration extends Configuration {

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        HashMap<String, String> options = new HashMap<>();
        options.put("storeKey", "true");
        options.put("refreshKrb5Config", "true");

        final String krbLoginModuleName = ConfigurationUtil.IS_IBM
                ? ConfigurationUtil.IBM_KRB5_LOGIN_MODULE : ConfigurationUtil.SUN_KRB5_LOGIN_MODULE;

        return new AppConfigurationEntry[]{
                new AppConfigurationEntry(
                        krbLoginModuleName,
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        options
                )
        };
    }

}
