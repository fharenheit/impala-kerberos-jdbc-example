package io.datadynamics.hdfs;

import io.datadynamics.client.common.DefaultResourceLoader;
import io.datadynamics.client.common.Resource;
import io.datadynamics.client.kerberos.FileSystemHelper;
import io.datadynamics.client.kerberos.KerberosKeytabUser;
import io.datadynamics.client.kerberos.KerberosUser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class KerberosHdfsTester {

    public static void main(String[] args) throws IOException {

        DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();
        Resource coreResource = defaultResourceLoader.getResource("classpath:core-site.xml");
        Resource hdfsResource = defaultResourceLoader.getResource("classpath:hdfs-site.xml");
        Resource hiveResource = defaultResourceLoader.getResource("classpath:hive-site.xml");

        Configuration configuration = new Configuration();
        configuration.addResource(coreResource.getURL());
        configuration.addResource(hdfsResource.getURL());
        configuration.addResource(hiveResource.getURL());

        KerberosUser kerberosUser = new KerberosKeytabUser("honggildong", "/home/honggildong/impala.keytab");
        FileSystemHelper helper = FileSystemHelper.create(configuration, kerberosUser);
        FileSystem fs = helper.getFs();

        fs.listFiles(new Path("/"), true);

        helper.closeFileSystem();
        helper.logout();
    }


}
