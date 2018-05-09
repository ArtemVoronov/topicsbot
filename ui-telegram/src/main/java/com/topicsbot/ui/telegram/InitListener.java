package com.topicsbot.ui.telegram;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * author: Artem Voronov
 */
@SuppressWarnings("Duplicates")
public class InitListener implements ServletContextListener {

  private static final String PROPERTY_CONFIG_DIR     = "config.dir";
  private static final String DEFAULT_CONFIG_DIR      = "conf";
  private static final String PROPERTIES_FILE_NAME    = "config.xml";
  private static final String LOG4J_FILE_NAME         = "log4j-topics-bot-ui-telegram.properties";

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    final File configDir = getConfigDir();
    final Configuration config = readConfig(configDir);
    initLog4j(configDir);
    initWebContext(config);

    Thread.setDefaultUncaughtExceptionHandler((t, e) -> Logger.getLogger("UNCAUGHT_EXCEPTION").fatal("Unexpected error in " + t.getName(), e));

  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    WebContext.stop();
  }

  private File getConfigDir() {
    String configDir = System.getProperty(PROPERTY_CONFIG_DIR);
    if (configDir == null) {
      configDir = DEFAULT_CONFIG_DIR;
      System.err.println("System property '" + PROPERTY_CONFIG_DIR + "' is not set. Using default value: " + configDir);
    }
    File cfgDir = new File(configDir);

    if (!cfgDir.exists())
      throw new RuntimeException("Config directory '" + cfgDir.getAbsolutePath() + "' does not exist");

    System.out.println("Using properties directory '" + cfgDir.getAbsolutePath() + "'");
    return cfgDir;
  }

  private Configuration readConfig(File configDir) {
    try {
      final Configurations configs = new Configurations();
      return configs.properties(new File(configDir.getAbsolutePath() + File.separator + PROPERTIES_FILE_NAME));
    } catch (ConfigurationException cex) {
      throw new RuntimeException("Unable to read config");
    }
  }

  private void initLog4j(File configDir) {
    final File log4jProps = new File(configDir, LOG4J_FILE_NAME);
    System.out.println("Log4j conf file: " + log4jProps.getAbsolutePath() + ", exists: " + log4jProps.exists());
    PropertyConfigurator.configureAndWatch(log4jProps.getAbsolutePath(), TimeUnit.MINUTES.toMillis(1));
  }

  private void initWebContext(Configuration config) {
    try {
      WebContext.init(config);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Can't init WebContext", e);
    }
  }
}
