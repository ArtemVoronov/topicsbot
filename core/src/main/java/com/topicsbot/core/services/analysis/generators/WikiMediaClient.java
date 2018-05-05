package com.topicsbot.core.services.analysis.generators;

import com.topicsbot.core.utils.XmlUtil;
import com.topicsbot.model.entities.chat.ChatLanguage;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
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

  private static final String USER_AGENT = "TopicsBot (Author: Artem Voronov voronov54@gmail.com)";

  private final CloseableHttpClient client;
  private final int keywordsCount;

  public WikiMediaClient(int keywordsCount) {
    this.client = initHttpClient(20, 2, 5000, 30000, 30000, 30000, USER_AGENT); //TODO: read from config
    this.keywordsCount = keywordsCount;
  }

  private String doGet(HttpGet httpGet, HttpContext context) throws IOException {
    return doHttpRequest(httpGet, context);
  }

  private String doHttpRequest(HttpUriRequest httpRequest, HttpContext context) throws IOException {
    try (CloseableHttpResponse response = client.execute(httpRequest, context)) {
      int code = response.getStatusLine().getStatusCode();
      if (code != 200)
        throw new IllegalStateException("Wrong response code: " + code);

      HttpEntity entity = response.getEntity();
      return EntityUtils.toString(entity);
    }
  }

  @Override
  public Set<String> getTopics(List<String> keywords, ChatLanguage language) throws IOException {
    if (keywords != null && keywords.size() == keywordsCount) {//TODO: greater or equal
      String root = getTargetRoot(language);

      //TODO: придумать какой-то другой способ выборки, чтобы хоть чёто возвращалось при "плохих" словах
      String target1 = root + paramHead + String.join("+", keywords.subList(0, 3)) + paramTail + 3;
      String target2 = root + paramHead + String.join("+", keywords.subList(2, 6)) + paramTail + 2;

      HttpContext context1 = HttpClientContext.create();
      HttpGet httpGet1 = new HttpGet(target1);
      String res1 = doGet(httpGet1, context1);

      HttpContext context2 = HttpClientContext.create();
      HttpGet httpGet2 = new HttpGet(target2);
      String res2 = doGet(httpGet2, context2);

      Set<String> topics = new HashSet<>();

      List<String> parsedTopics = parseTopics(res1);
      collectTopics(topics, parsedTopics);
      parsedTopics = parseTopics(res2);
      collectTopics(topics, parsedTopics);

      if (topics.isEmpty()) {//возможно так будет больше топиков
        target1 = root + paramHead + String.join("+", keywords.get(0), keywords.get(4)) + paramTail + 1;
        target2 = root + paramHead + String.join("+", keywords.subList(6, 8)) + paramTail + 2;

        context1 = HttpClientContext.create();
        httpGet1 = new HttpGet(target1);
        res1 = doGet(httpGet1, context1);

        context2 = HttpClientContext.create();
        httpGet2 = new HttpGet(target2);
        res2 = doGet(httpGet2, context2);

        parsedTopics = parseTopics(res1);
        collectTopics(topics, parsedTopics);
        parsedTopics = parseTopics(res2);
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

  private static List<String> parseTopics(String entity) {
    try {

      Document doc = XmlUtil.string2xml(entity);
      XmlUtil.normalize(doc);
      NodeList searchResult = doc.getElementsByTagName("search");//TODO: null pointer

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

  private static CloseableHttpClient initHttpClient(int connectionPoolSize, int maxConnectionsPerRoute, long keepAliveMillis,
                                                   int requestTimeoutInMillis, int connectionTimeoutInMillis, int socketTimeoutInMillis,
                                                   String userAgent) {
    PoolingHttpClientConnectionManager connectionPoolManager = new PoolingHttpClientConnectionManager();
    connectionPoolManager.setMaxTotal(connectionPoolSize);
    connectionPoolManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

    ConnectionKeepAliveStrategy keepAliveStrategy = (response, context) -> {
      HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
      while (it.hasNext()) {
        HeaderElement he = it.nextElement();
        String param = he.getName();
        String value = he.getValue();
        if (value != null && param.equalsIgnoreCase("timeout")) {
          try {
            return Long.parseLong(value) * 1000;
          } catch(NumberFormatException ignore) {
          }
        }
      }
      return keepAliveMillis;
    };

    RequestConfig requestConfig = RequestConfig.custom().
        setConnectionRequestTimeout(requestTimeoutInMillis)
        .setConnectTimeout(connectionTimeoutInMillis)
        .setSocketTimeout(socketTimeoutInMillis)
        .build();

    return HttpClients.custom()
        .setConnectionManager(connectionPoolManager)
        .setKeepAliveStrategy(keepAliveStrategy)
        .setDefaultRequestConfig(requestConfig)
        .setUserAgent(userAgent)
        .build();
  }

  public static void main(String[] args) throws IOException {
    TopicsGenerator generator = new WikiMediaClient(10);

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

    Set<String> res = generator.getTopics(keywords, ChatLanguage.EN);

    res.forEach(System.out::println);
  }
}
