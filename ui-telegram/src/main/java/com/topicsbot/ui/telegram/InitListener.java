package com.topicsbot.ui.telegram;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * author: Artem Voronov
 */
@SuppressWarnings("Duplicates")
public class InitListener implements ServletContextListener {

  private static final String PROD_LOG4J_FILE_NAME         = "prod-log4j-ui-telegram.properties";
  private static final String TEST_LOG4J_FILE_NAME         = "test-log4j-ui-telegram.properties";
  private static final String TEST_PROPERTY                = "test";

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    initLog4j();
    initWebContext();
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> Logger.getLogger("UNCAUGHT_EXCEPTION").fatal("Unexpected error in " + t.getName(), e));
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    WebContext.stop();
  }

  private void initLog4j() {
    try {
      String testMode = System.getProperty(TEST_PROPERTY);
      String fileName = testMode == null || !testMode.equals("true") ? PROD_LOG4J_FILE_NAME : TEST_LOG4J_FILE_NAME;
      ClassLoader classLoader = InitListener.class.getClassLoader();
      URL log4jConfig = classLoader.getResource(fileName);
      final File log4jProps = new File(log4jConfig.toURI());
      System.out.println("Log4j conf file: " + log4jProps.getAbsolutePath() + ", exists: " + log4jProps.exists());
      PropertyConfigurator.configureAndWatch(log4jProps.getAbsolutePath(), TimeUnit.MINUTES.toMillis(1));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Can't init log4j", e);
    }
  }

  private void initWebContext() {
    try {
      WebContext.init();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Can't init WebContext", e);
    }
  }
}
