package org.yello.labs

import io.github.cdimascio.dotenv.Dotenv
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Implementation of the gradle plugin interface for anything that can call the apply method.
 * Can be used in either the build.gradle or the settings.gradle.
 *
 * Use the gradle property that is set when you apply the plugin
 * {@code keyring::getSecret(domain, username)}
 *
 * Use the static class that has public static methods directly from the plugin class
 * {@code import org.yellolab.KeyringPlugin;}
 * {@code KeyringPlugin.getSecret(domain,username)}
 */

public class KeyringPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(KeyringPlugin.class);
    private static boolean sourceFromEnv;
    private static Dotenv dotenv;
    @Override
    void apply(Object parent) {
        KeyringPlugin Keyring = new KeyringPlugin()

        if (parent instanceof Project) {
            Project project = parent;
            project.rootProject.allprojects { prj ->
                project.ext.keyring = Keyring
            }
        } else if (parent instanceof Settings) {
            Settings settings = parent
            settings.ext.keyring = Keyring
        }

        sourceFromEnv = parent.hasProperty(Const.SOURCE_ENV_KEY)
        if(sourceFromEnv){
            logger.info('Configuring Dotenv to source from directory {}', parent['rootDir'])
            dotenv = Dotenv.configure().directory(parent['rootDir'].toString()).load()
        }

    }

    /**
     * Attempts to retrieve a secret using the Keyring library.
     *
     * @param host : Target hostname
     * @param userName : Target username
     * @return the String value returned from the Keyring library
     */
    public static String getSecret(String host, String userName) {
        logger.info('Retrieving secret')

        logger.debug('If you need to debug your output, do it in trace.  Gradle does NOT log trace')

        if (sourceFromEnv) {
            logger.info("Sourcing secret information from .env")
            return dotenv.get(host+"_"+userName)
        } else {
            return Keyring.getSecret(host, userName)
        }
    }

    /**
     * Attempts to set a secret using the Keyring library.
     *
     * @param host : Target hostname
     * @param userName : Target username
     * @param secret : String value of a secret
     * @return boolean to be interpreted as success performing action
     */
    public static boolean setSecret(String host, String userName, String secret) {
        logger.info("Setting secret")

        if (sourceFromEnv){
            logger.warn("Cannot set password in .env, you must maintain that file.")
        }
        Keyring.setSecret(host, userName, secret)

        logger.trace("If you need to debug your output, do it in trace.  Gradle does NOT log trace")

        return true
    }

    /**
     * Attempts to delete a secret using the Keyring library.
     *
     * @param host : Target hostname
     * @param userName : Target username
     * @return boolean to be interpreted as success performing action
     */
    public static boolean deleteSecret(String host, String userName) {
        logger.info("Setting secret")

        Keyring.deleteSecret(host, userName)

        logger.trace("If you need to debug your output, do it in trace.  Gradle does NOT log trace")

        return true;
    }

    static boolean isSourceFromEnv(){
        return sourceFromEnv
    }
}
