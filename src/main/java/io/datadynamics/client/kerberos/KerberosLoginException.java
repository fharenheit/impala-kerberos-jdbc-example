package io.datadynamics.client.kerberos;

/**
 * Exception thrown by KerberosUser when an error happens during login/logout.
 */
public class KerberosLoginException extends RuntimeException {

    public KerberosLoginException(String message) {
        super(message);
    }

    public KerberosLoginException(String message, Throwable cause) {
        super(message, cause);
    }

}