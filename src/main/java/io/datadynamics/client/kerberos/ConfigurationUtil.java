package io.datadynamics.client.kerberos;

public interface ConfigurationUtil {

    boolean IS_IBM = System.getProperty("java.vendor", "").contains("IBM");
    String IBM_KRB5_LOGIN_MODULE = "com.ibm.security.auth.module.Krb5LoginModule";
    String SUN_KRB5_LOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule";

}
