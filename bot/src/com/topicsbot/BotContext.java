package com.topicsbot;

import com.topicsbot.services.Services;
import com.topicsbot.services.analysis.AnalysisProvider;
import com.topicsbot.services.api.telegram.TelegramApiProvider;
import com.topicsbot.services.cache.CacheService;
import com.topicsbot.services.db.DBService;
import com.topicsbot.services.i18n.ResourceBundleService;
import com.topicsbot.services.template.TemplateService;
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

  static synchronized void init(Configuration config, String version) throws Exception {
    if (BotContext.services == null) {
      BotContext.services = new Services(config);
      BotContext.version = version;
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
  public ResourceBundleService getResourceBundleService() {
    return services.getResourceBundleService();
  }

  @Produces
  public AnalysisProvider getAnalysisProvider() {
    return services.getAnalysisProvider();
  }

  @Produces
  public CacheService getCacheService() {
    return services.getCacheService();
  }

  @Produces
  public TemplateService getTemplateService() {
    return services.getTemplateService();
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