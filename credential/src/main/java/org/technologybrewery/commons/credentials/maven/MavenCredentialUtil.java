package org.technologybrewery.commons.credentials.maven;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
import org.technologybrewery.commons.credentials.CredentialException;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Contains utility functionality for Habushu, including bash script execution
 * and accessing username/password credentials that may be defined within the
 * relevant settings.xml configuration.
 */
public final class MavenCredentialUtil {

    private MavenCredentialUtil() {}

    /**
     * Find the username for a given server in Maven's user settings.
     *
     * @return the username for the server specified in Maven's settings.xml
     */
    public static String findUsernameForServer(Settings settings, String serverId) {
        Server server = settings.getServer(serverId);
        return server != null ? server.getUsername() : null;
    }

    /**
     * Find the plain-text server password, without decryption steps, extracted from Maven's user settings.
     *
     * @return the password for the specified server from Maven's settings.xml
     */
    public static String findPlaintextPasswordForServer(Settings settings, String serverId) {
        Server server = settings.getServer(serverId);
        return server != null ? server.getPassword() : null;
    }

    /**
     * Simple utility method to decrypt a stored password for a server.
     *
     * @param serverId the id of the server to decrypt the password for
     */
    public static String decryptServerPassword(Settings settings, String serverId) {
        String decryptedPassword;

        try {
            decryptedPassword = MavenPasswordDecoder.decryptPasswordForServer(settings, serverId);
        } catch (PlexusCipherException | SecDispatcherException e) {
            throw new CredentialException("Unable to decrypt stored passwords. Make sure you have a master password " +
                    "set in settings-security.xml and your server password is encrypted with it in settings.xml. See " +
                    "https://maven.apache.org/guides/mini/guide-encryption.html for more details.", e);
        }

        return decryptedPassword;
    }
}
