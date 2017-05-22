package com.topicsbot.services.analysis.generators;

import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.utils.XmlUtil;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Author: Artem Voronov
 */
public class WikiMediaClient implements TopicsGenerator {

  private static final Logger logger = Logger.getLogger("WIKI_MEDIA_CLIENT");

  private static final String WIKI_EN = "https://en.wikipedia.org/w/api.php?";
  private static final String WIKI_RU = "https://ru.wikipedia.org/w/api.php?";
  private static final String WIKI_ES = "https://es.wikipedia.org/w/api.php?";
  private static final String WIKI_JA = "https://ja.wikipedia.org/w/api.php?";
  private static final String WIKI_DE = "https://de.wikipedia.org/w/api.php?";
  private static final String WIKI_FR = "https://fr.wikipedia.org/w/api.php?";
  private static final String WIKI_IT = "https://it.wikipedia.org/w/api.php?";
  private static final String WIKI_PT = "https://pt.wikipedia.org/w/api.php?";
  private static final String WIKI_PL = "https://pl.wikipedia.org/w/api.php?";
  private static final String WIKI_ZH = "https://zh.wikipedia.org/w/api.php?";
  private static final String WIKI_AR = "https://ar.wikipedia.org/w/api.php?";
  private static final String WIKI_HI = "https://hi.wikipedia.org/w/api.php?";
  private static final String WIKI_CS = "https://cs.wikipedia.org/w/api.php?";
  private static final String WIKI_KO = "https://ko.wikipedia.org/w/api.php?";
  private static final String WIKI_TR = "https://tr.wikipedia.org/w/api.php?";

  private static final String paramHead = "action=query&list=search&srwhat=text&format=xml&srsearch=";
  private static final String paramTail = "&srlimit=";

  private static final String USER_AGENT = "TopicsBot/2.0 (Author: Artem Voronov voronov54@gmail.com) BasedOnJerseyClient/2.22.1";

  private final Client client;
  private final int keywordsCount;

  public WikiMediaClient(int keywordsCount) {
    this.client = initClient();
    this.keywordsCount = keywordsCount;
  }

  @Override
  public Set<String> getTopics(List<String> keywords, ChatLanguage language) {
    if (keywords != null && keywords.size() == keywordsCount) {
      String root = getTargetRoot(language);

      //TODO: придумать какой-то другой способ выборки, чтобы хоть чёто возвращалось при "плохих" словах
      String target1 = root + paramHead + String.join("+", keywords.subList(0, 3)) + paramTail + 3;
      String target2 = root + paramHead + String.join("+", keywords.subList(2, 6)) + paramTail + 2;

      Response response1 = client.target(target1).request().header("User-Agent", USER_AGENT).buildGet().invoke();
      Response response2 = client.target(target2).request().header("User-Agent", USER_AGENT).buildGet().invoke();

      Set<String> topics = new HashSet<>();

      List<String> parsedTopics = parseResponse(response1);
      collectTopics(topics, parsedTopics);
      parsedTopics = parseResponse(response2);
      collectTopics(topics, parsedTopics);

      if (topics.isEmpty()) {//возможно так будет больше топиков
        target1 = root + paramHead + String.join("+", keywords.get(0), keywords.get(4)) + paramTail + 1;
        target2 = root + paramHead + String.join("+", keywords.subList(6, 8)) + paramTail + 2;
        response1 = client.target(target1).request().header("User-Agent", USER_AGENT).buildGet().invoke();
        response2 = client.target(target2).request().header("User-Agent", USER_AGENT).buildGet().invoke();

        parsedTopics = parseResponse(response1);
        collectTopics(topics, parsedTopics);
        parsedTopics = parseResponse(response2);
        collectTopics(topics, parsedTopics);
      }

      return topics;
    } else {
      return null;
    }
  }

  private static void collectTopics(Set<String> topics, List<String> parsedTopics) {
    if (parsedTopics != null && !parsedTopics.isEmpty()) {
      topics.addAll(parsedTopics);
    }
  }

  private static List<String> parseResponse(Response response) {
    try {

      String entity = "";
      if (response.hasEntity()) {
        entity = response.readEntity(String.class);
      }

      Document doc = XmlUtil.string2xml(entity);
      XmlUtil.normalize(doc);
      NodeList searchResult = doc.getElementsByTagName("search");

      if (searchResult == null || searchResult.getLength() < 1) {
        return null;
      }

      List<String> topics = new ArrayList<>();
      for (Node item : XmlUtil.asList(searchResult.item(0).getChildNodes())) {
        Node title = item.getAttributes().getNamedItem("title");
        topics.add(title.getNodeValue());
      }

      return topics;

    } catch (Exception ex) {
      logger.error("Error at parsing response from wikimedia", ex);
      return null;
    }
  }

  private static Client initClient() {
    ClientConfig config = new ClientConfig();
    config.property(ClientProperties.CONNECT_TIMEOUT, 30000);
    config.property(ClientProperties.READ_TIMEOUT, 30000);

    Client client = ClientBuilder.newBuilder().withConfig(config).build();

    return client;
  }

  private static String getTargetRoot(ChatLanguage language) {
    if (language == null) {
      return WIKI_EN;
    }

    switch (language) {
      case EN:
        return WIKI_EN;
      case RU:
        return WIKI_RU;
      case ES:
        return WIKI_ES;
      case JA:
        return WIKI_JA;
      case DE:
        return WIKI_DE;
      case FR:
        return WIKI_FR;
      case IT:
        return WIKI_IT;
      case PT:
        return WIKI_PT;
      case PL:
        return WIKI_PL;
      case ZH:
        return WIKI_ZH;
      case AR:
        return WIKI_AR;
      case HI:
        return WIKI_HI;
      case CS:
        return WIKI_CS;
      case TR:
        return WIKI_TR;
      case KO:
        return WIKI_KO;
      default:
        logger.error("Unknown language: " + language);
        return WIKI_EN;//try to find at English Wikipedia
    }
  }
}
