package io.datadynamics.client.kerberos;

import javax.security.auth.callback.*;
import java.io.IOException;

/**
 * CallbackHandler that provides the given username and password.
 */
public class UsernamePasswordCallbackHandler implements CallbackHandler {

    private final String username;
    private final String password;

    public UsernamePasswordCallbackHandler(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (final Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                final NameCallback nameCallback = (NameCallback) callback;
                nameCallback.setName(username);
            } else if (callback instanceof PasswordCallback) {
                final PasswordCallback passwordCallback = (PasswordCallback) callback;
                passwordCallback.setPassword(password.toCharArray());
            } else {
                throw new IllegalStateException("Unexpected callback type: " + callback.getClass().getCanonicalName());
            }
        }
    }

}
