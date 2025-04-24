package io.datadynamics.impala;

import com.cloudera.impala.jdbc.DataSource;
import io.datadynamics.client.kerberos.KerberosAction;
import io.datadynamics.client.kerberos.KerberosKeytabUser;

import java.sql.*;

public class KerberosJdbcTester {

    public static void main(String[] args) throws SQLException {
        KerberosKeytabUser kerberosKeytabUser = new KerberosKeytabUser("honggildong", "/home/honggildong/impala.keytab");

        DataSource ds = new DataSource();
        ds.setURL(DataSourceUtils.getUrl("coor1.datalake.net", "21050"));

        KerberosAction<Connection> kerberosAction = new KerberosAction<>(kerberosKeytabUser, ds::getConnection);
        Connection conn = kerberosAction.execute();
        executeQuery(conn);

        kerberosKeytabUser.logout();
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
