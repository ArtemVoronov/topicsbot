package com.topicsbot.services.i18n;

import com.topicsbot.model.chat.ChatLanguage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Author: Artem Voronov
 */
public class ResourceBundleService {
  private static final Logger logger = Logger.getLogger("RESOURCE_BUNDLES");
  private static final String RESOURCE_BUNDLE_TEMPLATE = "messages_%s.properties";
  private static final Map<String, PropertyResourceBundle> BUNDLES = new HashMap<>();
  private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(Arrays.stream(ChatLanguage.values()).map(Enum::name).map(String::toLowerCase).toArray(String[]::new));

  public ResourceBundleService() {
    for (String lang : SUPPORTED_LANGUAGES) {
      String path = String.format(RESOURCE_BUNDLE_TEMPLATE, lang);
      try (InputStream is = ResourceBundleService.class.getResourceAsStream(path)) {
        try (InputStreamReader ir = new InputStreamReader(is, "UTF8")) {
          PropertyResourceBundle resourceBundle = new PropertyResourceBundle(ir);
          BUNDLES.put(lang, resourceBundle);
        }

      } catch (IOException e) {
        logger.error("Unable to load resource bundles", e);
      }
    }
  }

  public String getMessage(String lang, String key, String ... args) {
    String message = getMessage(lang, key);
    return args.length > 0 ? String.format(message, args) : message;
  }

  private String getMessage(String lang, String key) {
    PropertyResourceBundle bundle = getResourceBundle(lang);
    return bundle.getString(key);
  }

  private PropertyResourceBundle getResourceBundle(String lang) {
    if (lang == null || !SUPPORTED_LANGUAGES.contains(lang))
      lang = "en";

    return BUNDLES.get(lang);
  }
}
