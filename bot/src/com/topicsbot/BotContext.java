package com.topicsbot;

import com.topicsbot.services.Services;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.db.DBService;
import org.apache.commons.configuration2.Configuration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Dependent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.enterprise.inject.Produces;

/**
 * author: Artem Voronov
 */
@Named
@ApplicationScoped
public class BotContext {

  private static Services services;
  private static String version;
  private static String googleAnalyticsId;
  private static String deployUrl;
  private static String token;

  static synchronized void init(Configuration config, String version, String googleAnalyticsId, String deployUrl, String token) throws Exception {
    if (BotContext.services == null) {
      BotContext.services = new Services(config);
      BotContext.version = version;
      BotContext.googleAnalyticsId = googleAnalyticsId;
      BotContext.deployUrl = deployUrl;
      BotContext.token = token;
    }
  }

  static synchronized void shutdown() {
    if (services != null)
      services.shutdown();
  }

  public static Services getServices() {
    return services;
  }

  public static String getVersion() {
    return version;
  }

  @Produces
  public DBService getDBService() {
    return services.getDbService();
  }

  @Produces
  public TelegramApiProvider getTelegramApiProvider() {
    return services.getTelegramApiProvider();
  }

  @Produces
  @Named("googleAnalyticsId")
  public String getGoogleAnalyticsTargetingId() {
    return googleAnalyticsId;
  }

  @Produces
  @Named("deployUrl")
  public String getDeployUrl() {
    return deployUrl;
  }

  @Produces
  @Named("token")
  public static String getToken() {
    return token;
  }

  @Produces
  @Dependent
  @SuppressWarnings("UnusedDeclaration")
  public FacesContext getFacesContext() {
    final FacesContext ctx = FacesContext.getCurrentInstance();
    if (ctx == null) {
      throw new ContextNotActiveException("FacesContext is not available");
    }
    return ctx;
  }

  @Produces
  @Dependent
  @SuppressWarnings("UnusedDeclaration")
  public ExternalContext getExternalContext(FacesContext facesContext) {
    return facesContext.getExternalContext();
  }

}