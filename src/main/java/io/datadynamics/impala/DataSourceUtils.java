package io.datadynamics.impala;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DataSourceUtils {

    public static String getUrl(String hostname, String port, String krbRealm, String krbServiceName) {
        String schemaName = "default";
        Map<String, String> urlProperties = new LinkedHashMap<>();
        urlProperties.putAll(getAuthenticationSettings("1", "sasl", hostname, krbRealm, krbServiceName));
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
