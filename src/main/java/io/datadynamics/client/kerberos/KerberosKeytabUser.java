package io.datadynamics.client.kerberos;

import org.apache.commons.lang3.Validate;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;

/**
 * Used to authenticate and execute actions when Kerberos is enabled and a keytab is being used.
 */
public class KerberosKeytabUser extends AbstractKerberosUser {

    private final String keytabFile;

    public KerberosKeytabUser(final String principal, final String keytabFile) {
        super(principal);
        this.keytabFile = keytabFile;
        Validate.notBlank(keytabFile);
    }

    @Override
    protected Configuration createConfiguration() {
        return new KeytabConfiguration(principal, keytabFile);
    }

    @Override
    protected CallbackHandler createCallbackHandler() {
        return null;
    }

    /**
     * @return the keytab file for this user
     */
    public String getKeytabFile() {
        return keytabFile;
    }

}
