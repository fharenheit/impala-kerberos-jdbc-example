package io.datadynamics.client.kerberos;

import org.apache.commons.lang3.Validate;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;

/**
 * KerberosUser that authenticates via username and password instead of keytab.
 */
public class KerberosPasswordUser extends AbstractKerberosUser {

    private final String password;

    public KerberosPasswordUser(final String principal, final String password) {
        super(principal);
        this.password = Validate.notBlank(password);
    }

    @Override
    protected Configuration createConfiguration() {
        return new PasswordConfiguration();
    }

    @Override
    protected CallbackHandler createCallbackHandler() {
        return new UsernamePasswordCallbackHandler(principal, password);
    }

}
