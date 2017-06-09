package com.topicsbot.services.template;

import com.topicsbot.services.cache.CacheInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Artem Voronov
 */
public class TemplateService {
  private final Configuration configuration;

  private enum Templates {
    ADMIN_CONSOLE("admin_console");

    private final String name;

    Templates(String name) {
      this.name = name;
    }
  }

  public TemplateService() {
    configuration = new Configuration() {{
      setOutputEncoding("UTF-8");
      setNumberFormat("0.####");
    }};

    configuration.setClassForTemplateLoading(TemplateService.class, "templates");
  }

  public String getAdminConsolePage(CacheInfo cacheInfo) {
    String templateName = getTemplateName(Templates.ADMIN_CONSOLE);
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("cacheInfo", cacheInfo);
    return processTemplate(templateName, parameters);
  }

  private static String getTemplateName(Templates template) {
    return template.name + ".ftl";
  }

  private Template loadTemplate(String templateName) throws IOException {
    return configuration.getTemplate(templateName, "UTF-8");
  }

  private String processTemplate(String templateName, Map<String, Object> parameters) {
    try {
      final Writer writer = new StringWriter();
      Template template = loadTemplate(templateName);
      template.process(parameters, writer);
      return writer.toString();
    }
    catch (TemplateException e) {
      throw new RuntimeException("Invalid template: " + templateName, e);
    }
    catch (IOException e) {
      throw new RuntimeException("Error during form generation using template: " + templateName, e);
    }
  }
}
