package com.topicsbot.core.services.analysis.topics;

import com.topicsbot.core.utils.HttpClientFactory;
import com.topicsbot.core.utils.XmlUtil;
import com.topicsbot.model.entities.chat.ChatLanguage;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.*;


/**
 * Author: Artem Voronov
 */
public class WikiMediaStorage implements TopicsAnalyzer {

  private static final Logger logger = Logger.getLogger("TOPICS_ANALYZER");
  private static final String WIKI_MEDIA_URL_PATH = "https://%s.wikipedia.org/w/api.php?";
  private static final String WIKI_MEDIA_URL_PARAMETERS = "%saction=query&list=search&srwhat=text&format=xml&srsearch=%s&srlimit=%s";
  private static final String USER_AGENT = "Topics Bot (www.topicsbot.com, topicsbot@gmail.com)";

  private final CloseableHttpClient client;
  private final int keywordsLimit; //min keywords amount required for getting of topics

  public WikiMediaStorage() {
    this.client = HttpClientFactory.initHttpClient(20, 2, 5000, 30000, 30000, 30000, USER_AGENT); //TODO: read from config
    this.keywordsLimit = 10;
  }

  @Override
  public Set<String> keywordsToTopics(List<String> keywords, ChatLanguage language) throws IOException {
    if (keywords == null || keywords.size() != keywordsLimit)
      return null;

    Set<String> result = new HashSet<>(5);

    result.addAll(keywordsToTopics(keywords, language, 0, 3, 3));
    result.addAll(keywordsToTopics(keywords, language, 2, 6, 2));

    if (result.isEmpty()) {//возможно так будет больше топиков
      result.addAll(keywordsToTopics(keywords, language, 0, 4, 1));
      result.addAll(keywordsToTopics(keywords, language, 6, 8, 2));
    }

    return result;
  }

  private List<String> keywordsToTopics(List<String> keywords, ChatLanguage language, int beginIndex, int endIndex, int maxTopics) throws IOException {
    String response = sendRequest(keywords, language, beginIndex, endIndex, maxTopics);
    return parseResponse(response);
  }

  private String sendRequest(List<String> keywords, ChatLanguage language, int beginIndex, int endIndex, int maxTopics) throws IOException {
    String root = getWikiRoot(language);
    String keywordsParam = String.join("+", keywords.subList(beginIndex, endIndex));
    String url = String.format(WIKI_MEDIA_URL_PARAMETERS, root, keywordsParam, maxTopics);
    HttpContext context = HttpClientContext.create();
    HttpGet httpGet = new HttpGet(url);

    try (CloseableHttpResponse response = client.execute(httpGet, context)) {
      int code = response.getStatusLine().getStatusCode();
      if (code != 200)
        throw new IllegalStateException("Wrong response code: " + code);

      HttpEntity entity = response.getEntity();
      return EntityUtils.toString(entity);
    }
  }

  private static List<String> parseResponse(String response) {
    try {
      List<String> result = new ArrayList<>();
      Document doc = XmlUtil.string2xml(response);

      XmlUtil.normalize(doc);
      NodeList searchResult = doc.getElementsByTagName("search");

      if (searchResult == null || searchResult.getLength() < 1)
        return  Collections.emptyList();

      for (Node item : XmlUtil.asList(searchResult.item(0).getChildNodes())) {
        Node title = item.getAttributes().getNamedItem("title");
        result.add(title.getNodeValue());
      }

      return result;

    } catch (Exception ex) {
      logger.error("Error at parsing response from wikimedia", ex);
      return Collections.emptyList();
    }
  }

  private static String getWikiRoot(ChatLanguage language) {
    if (language == null)
      language = ChatLanguage.EN;

    return String.format(WIKI_MEDIA_URL_PATH, language.name().toLowerCase());
  }

  //TODO
  public static void main(String[] args) throws IOException {
    TopicsAnalyzer generator = new WikiMediaStorage();

    List<String> keywords = new ArrayList<>();
    keywords.add("bee");
    keywords.add("ant");
    keywords.add("honey");
    keywords.add("football");
    keywords.add("baseball");
    keywords.add("friday");
    keywords.add("hello");
    keywords.add("car");
    keywords.add("money");
    keywords.add("car");

    Set<String> res = generator.keywordsToTopics(keywords, ChatLanguage.EN);

    res.forEach(System.out::println);
  }
}
