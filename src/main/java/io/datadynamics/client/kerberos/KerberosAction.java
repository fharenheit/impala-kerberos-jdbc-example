package io.datadynamics.client.kerberos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Objects;

/**
 * Helper class for processors to perform an action as a KerberosUser.
 */
public class KerberosAction<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractKerberosUser.class);

    private final KerberosUser kerberosUser;
    private final PrivilegedExceptionAction<T> action;
    private final ClassLoader contextClassLoader;

    public KerberosAction(final KerberosUser kerberosUser, final PrivilegedExceptionAction<T> action) {
        this(kerberosUser, action, null);
    }

    public KerberosAction(final KerberosUser kerberosUser,
                          final PrivilegedExceptionAction<T> action,
                          final ClassLoader contextClassLoader) {
        this.kerberosUser = Objects.requireNonNull(kerberosUser);
        this.action = Objects.requireNonNull(action);
        this.contextClassLoader = contextClassLoader;
    }

    public T execute() {
        T result;
        // lazily login the first time the processor executes
        if (!kerberosUser.isLoggedIn()) {
            try {
                kerberosUser.login();
                LOGGER.info("Successful login for {}", kerberosUser.getPrincipal());
            } catch (final KerberosLoginException e) {
                throw new KerberosException("Login failed due to: " + e.getMessage(), e);
            }
        }

        // check if we need to re-login, will only happen if re-login window is reached (80% of TGT life)
        try {
            kerberosUser.checkTGTAndRelogin();
        } catch (final KerberosLoginException e) {
            throw new KerberosException("Relogin check failed due to: " + e.getMessage(), e);
        }

        // attempt to execute the action, if an exception is caught attempt to logout/login and retry
        try {
            if (contextClassLoader == null) {
                result = kerberosUser.doAs(action);
            } else {
                result = kerberosUser.doAs(action, contextClassLoader);
            }
        } catch (final SecurityException se) {
            LOGGER.info("Privileged action failed, attempting relogin and retrying...");
            LOGGER.debug("", se);

            try {
                kerberosUser.logout();
                kerberosUser.login();
                result = kerberosUser.doAs(action);
            } catch (Exception e) {
                throw new KerberosException("Retrying privileged action failed due to: " + e.getMessage(), e);
            }
        } catch (final PrivilegedActionException pae) {
            final Exception cause = pae.getException();
            throw new KerberosException("Privileged action failed due to: " + cause.getMessage(), cause);
        }

        return result;
    }

}
