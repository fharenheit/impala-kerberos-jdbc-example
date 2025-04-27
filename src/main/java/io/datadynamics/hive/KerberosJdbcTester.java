package io.datadynamics.hive;

import com.cloudera.hive.jdbc.HS2DataSource;
import io.datadynamics.client.common.DefaultResourceLoader;
import io.datadynamics.client.common.Resource;
import io.datadynamics.client.kerberos.KerberosKeytabUser;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.security.UserGroupInformation;

import javax.sql.DataSource;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KerberosJdbcTester {

    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        KerberosKeytabUser kerberosUser = new KerberosKeytabUser("honggildong", "/home/honggildong/impala.keytab");

        HiveConfigurator configurator = new HiveConfigurator();

        DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();
        Resource coreResource = defaultResourceLoader.getResource("classpath:core-site.xml");
        Resource hdfsResource = defaultResourceLoader.getResource("classpath:hdfs-site.xml");
        Resource hiveResource = defaultResourceLoader.getResource("classpath:hive-site.xml");

        HiveConf hiveConfig = new HiveConf();
        hiveConfig.addResource(coreResource.getURL());
        hiveConfig.addResource(hdfsResource.getURL());
        hiveConfig.addResource(hiveResource.getURL());

        UserGroupInformation ugi = configurator.authenticate(hiveConfig, kerberosUser);
        kerberosUser.checkTGTAndRelogin();

        DataSource dataSource = dataSource();
        Connection conn = ugi.doAs((PrivilegedExceptionAction<Connection>) () -> dataSource.getConnection());
        executeQuery(conn);

        kerberosUser.logout();
    }

    public static DataSource dataSource() {
        HS2DataSource ds = new HS2DataSource();
        ds.setURL("");
        return ds;
    }

    public static void executeQuery(Connection conn) throws SQLException {
        String sql = "SELECT 1";

        long startTime = System.currentTimeMillis();

        PreparedStatement psmt = conn.prepareStatement(sql);
        ResultSet rs = psmt.executeQuery();
        int rows = 0;
        while (rs.next()) {
            rows++;
        }
        long finishTime = System.currentTimeMillis();
        rs.close();
        psmt.close();
        conn.close();

        System.out.println("Elapsed Time (sec)  : " + (finishTime - startTime) / 1000);
    }

}