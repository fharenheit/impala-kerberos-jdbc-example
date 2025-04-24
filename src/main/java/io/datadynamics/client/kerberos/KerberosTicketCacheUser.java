package io.datadynamics.client.kerberos;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;

/**
 * Used to perform actions as a Subject that already has a ticket in the ticket cache.
 */
public class KerberosTicketCacheUser extends AbstractKerberosUser {

    private final String ticketCache;

    public KerberosTicketCacheUser(final String principal) {
        this(principal, null);
    }

    public KerberosTicketCacheUser(final String principal, final String ticketCache) {
        super(principal);
        this.ticketCache = ticketCache;
    }

    @Override
    protected Configuration createConfiguration() {
        return new TicketCacheConfiguration(principal, ticketCache);
    }

    @Override
    protected CallbackHandler createCallbackHandler() {
        return null;
    }
}
