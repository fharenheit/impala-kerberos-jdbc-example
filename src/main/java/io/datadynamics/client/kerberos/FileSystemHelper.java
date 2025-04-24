package io.datadynamics.client.kerberos;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.PrivilegedExceptionAction;

/**
 * Utility class to assist with operations related to Hadoop {@link FileSystem}.
 * This class provides methods for secure and non-secure authentication, obtaining
 * file system instances, and managing resources such as statistics threads.
 */
public class FileSystemHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemHelper.class);

    private static final Object RESOURCES_LOCK = new Object();

    FileSystem fs;

    UserGroupInformation ugi;

    KerberosUser kerberosUser;

    private FileSystemHelper() {
    }

    /**
     * Creates a new instance of {@code FileSystemHelper} configured with the
     * provided {@link Configuration} and optionally a {@link KerberosUser}.
     * The method handles both secure and simple authentication setups based
     * on the configuration and initializes the file system accordingly.
     *
     * @param config       the Hadoop {@link Configuration} object containing settings
     *                     for the file system and security configurations.
     * @param kerberosUser the {@link KerberosUser} representing a keytab-based user;
     *                     can be {@code null} if not using secure authentication.
     * @return a newly created and configured {@code FileSystemHelper} instance.
     * @throws IOException if there is an issue configuring or initializing the file system.
     */
    public static FileSystemHelper create(Configuration config, KerberosUser kerberosUser) throws IOException {
        FileSystemHelper helper = new FileSystemHelper();

        synchronized (RESOURCES_LOCK) {
            if (SecurityUtil.isSecurityEnabled(config)) {
                helper.kerberosUser = kerberosUser;
                helper.ugi = SecurityUtil.getUgiForKerberosUser(config, kerberosUser);
            } else {
                config.set("ipc.client.fallback-to-simple-auth-allowed", "true");
                config.set("hadoop.security.authentication", "simple");
                helper.ugi = SecurityUtil.loginSimple(config);
                helper.kerberosUser = null;
            }
            helper.fs = helper.getFileSystemAsUser(config, helper.ugi);
        }
        return helper;
    }

    /**
     * This exists in order to allow unit tests to override it so that they don't take several minutes waiting for UDP packets to be received
     *
     * @param config the configuration to use
     * @return the FileSystem that is created for the given Configuration
     * @throws IOException if unable to create the FileSystem
     */
    public FileSystem getFileSystem(final Configuration config) throws IOException {
        return FileSystem.get(config);
    }

    /**
     * Retrieves a {@link FileSystem} instance using the provided configuration and executes the operation
     * as a specified user. This method ensures that the file system is created within the context of the
     * given user credentials, utilizing the privileges associated with that user.
     *
     * @param config the Hadoop {@link Configuration} object which specifies the settings for the file system.
     * @param ugi    the {@link UserGroupInformation} representing the user under whose privileges the file system
     *               will be accessed.
     * @return a {@link FileSystem} instance configured with the specified configuration and accessed as
     * the given user.
     * @throws IOException if there is an error creating the file system or the operation is interrupted.
     */
    public FileSystem getFileSystemAsUser(final Configuration config, UserGroupInformation ugi) throws IOException {
        try {
            return ugi.doAs((PrivilegedExceptionAction<FileSystem>) () -> FileSystem.get(config));
        } catch (InterruptedException e) {
            throw new IOException("Unable to create file system: " + e.getMessage());
        }
    }

    /**
     * Closes the provided {@link FileSystem} and interrupts its associated statistics thread.
     * Safe cleanup operations are performed to release all resources associated with the
     * specified file system.
     *
     * @param fileSystem the {@link FileSystem} to be closed; if null, no operation is performed
     */
    public void closeFileSystem(final FileSystem fileSystem) {
        try {
            interruptStatisticsThread(fileSystem);
        } catch (Exception e) {
            LOGGER.warn("Error stopping FileSystem statistics thread: " + e.getMessage());
            LOGGER.debug("", e);
        } finally {
            if (fileSystem != null) {
                try {
                    fileSystem.close();
                } catch (IOException e) {
                    LOGGER.warn("Error close FileSystem: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Closes the provided {@link FileSystem} and interrupts its associated statistics thread.
     * Safe cleanup operations are performed to release all resources associated with the
     * specified file system.
     */
    public void closeFileSystem() {
        try {
            interruptStatisticsThread(this.fs);
        } catch (Exception e) {
            LOGGER.warn("Error stopping FileSystem statistics thread: " + e.getMessage());
            LOGGER.debug("", e);
        } finally {
            if (this.fs != null) {
                try {
                    this.fs.close();
                } catch (IOException e) {
                    LOGGER.warn("Error close FileSystem: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Interrupts the statistics thread associated with a given {@link FileSystem}.
     * This is primarily used to stop the background thread that gathers file system
     * statistics to free up resources.
     *
     * @param fileSystem the {@link FileSystem} whose statistics thread is to be interrupted
     * @throws NoSuchFieldException   if the required fields are not found in the {@link FileSystem} class
     * @throws IllegalAccessException if the fields cannot be accessed due to Java access control rules
     */
    public void interruptStatisticsThread(final FileSystem fileSystem) throws NoSuchFieldException, IllegalAccessException {
        final Field statsField = FileSystem.class.getDeclaredField("statistics");
        statsField.setAccessible(true);

        final Object statsObj = statsField.get(fileSystem);
        if (statsObj instanceof FileSystem.Statistics) {
            final FileSystem.Statistics statistics = (FileSystem.Statistics) statsObj;

            final Field statsThreadField = statistics.getClass().getDeclaredField("STATS_DATA_CLEANER");
            statsThreadField.setAccessible(true);

            final Object statsThreadObj = statsThreadField.get(statistics);
            if (statsThreadObj instanceof Thread) {
                final Thread statsThread = (Thread) statsThreadObj;
                try {
                    statsThread.interrupt();
                } catch (Exception e) {
                    LOGGER.warn("Error interrupting thread: " + e.getMessage(), e);
                }
            }
        }
    }

    public FileSystem getFs() {
        return fs;
    }

    public UserGroupInformation getUgi() {
        return ugi;
    }

    public KerberosUser getKerberosUser() {
        return kerberosUser;
    }

    public void logout() {
        if (kerberosUser.isLoggedIn()) {
            kerberosUser.logout();
        }
    }
}
