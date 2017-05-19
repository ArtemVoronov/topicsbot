package com.topicsbot;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * author: Artem Voronov
 */
public class InitListener implements ServletContextListener {

  private static final String PROPERTY_CONFIG_DIR     = "config.dir";
  private static final String DEFAULT_CONFIG_DIR      = "config";
  private static final String PROPERTIES_FILE_NAME    = "config.properties";

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    try {
      final File configDir = getConfigDir();
      final Configurations configs = new Configurations();
      final Configuration config = configs.properties(new File(configDir.getAbsolutePath() + File.separator + PROPERTIES_FILE_NAME));
      initLog4j(configDir);
      initBotContext(config);
    } catch (ConfigurationException cex) {
      throw new RuntimeException("Unable to read config");
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    BotContext.getInstance().shutdown();
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

  private void initLog4j(File configDir) {
    final File log4jProps = new File(configDir, "log4j.properties");
    System.out.println("Log4j conf file: " + log4jProps.getAbsolutePath() + ", exists: " + log4jProps.exists());
    PropertyConfigurator.configureAndWatch(log4jProps.getAbsolutePath(), TimeUnit.MINUTES.toMillis(1));
  }

  private void initBotContext(Configuration config) {
    try {
      BotContext.init(config);
    }
    catch(Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Can't init BotContext", e);
    }
  }
}
