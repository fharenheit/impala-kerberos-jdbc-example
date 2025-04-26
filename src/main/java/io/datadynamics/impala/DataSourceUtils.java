package io.datadynamics.impala;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DataSourceUtils {

    public static final String DRIVER_CLASS_NAME = "com.cloudera.impala.jdbc.Driver";
    public static final String IMPALA_SCHEME = "jdbc:impala";
    public static final String IMPALA_HOST_PORT_SCHEMA_URI_TEMPLATE = "jdbc:impala://%s:%s/%s";
    public static final String IMPALA_HOST_PORT_URI_TEMPLATE = "jdbc:impala://%s:%s";

    public static String getUrl(String hostname, String port) {
        String schemaName = "default";
        Map<String, String> urlProperties = new LinkedHashMap<>();
        urlProperties.putAll(getAuthenticationSettings("1", "sasl", hostname, "DATALAKE.NET", "impala"));
        String additionalParameters = urlProperties.entrySet().stream().map(entry -> String.format(";%s=%s", new Object[]{entry.getKey(), entry.getValue()})).collect(Collectors.joining());
        if (StringUtils.isNoneBlank(new CharSequence[]{schemaName, hostname, port}))
            return String.format("jdbc:impala://%s:%s/%s", new Object[]{hostname, port, schemaName}) + additionalParameters;
        if (StringUtils.isNoneBlank(new CharSequence[]{hostname, port}) && StringUtils.isBlank(schemaName))
            return String.format("jdbc:impala://%s:%s", new Object[]{hostname, port}) + additionalParameters;
        throw new IllegalArgumentException("Invalid JDBC URI format");
    }

    public static Map<String, String> getAuthenticationSettings(String authMech, String transportMode, String krbHostFQDN, String krbRealm, String krbServiceName) {
        Map<String, String> urlProperties = new LinkedHashMap<>();
        urlProperties.put("AuthMech", authMech);
        urlProperties.put("transportMode", transportMode);
        urlProperties.put("KrbHostFQDN", krbHostFQDN);
        urlProperties.put("KrbRealm", krbRealm);
        urlProperties.put("KrbServiceName", krbServiceName);
        return urlProperties;
    }

}
