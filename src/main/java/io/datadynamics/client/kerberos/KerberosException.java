package io.datadynamics.client.kerberos;

public class KerberosException extends RuntimeException {

    public KerberosException() {
        super();
    }

    public KerberosException(String message) {
        super(message);
    }

    public KerberosException(String message, Throwable cause) {
        super(message, cause);
    }

    public KerberosException(Throwable cause) {
        super(cause);
    }

    protected KerberosException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
