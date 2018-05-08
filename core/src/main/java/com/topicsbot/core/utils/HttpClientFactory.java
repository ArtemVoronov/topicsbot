package com.topicsbot.core.utils;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;

/**
 * Author: Artem Voronov
 */
public class HttpClientFactory {

  public static CloseableHttpClient initHttpClient(int connectionPoolSize, int maxConnectionsPerRoute, long keepAliveMillis,
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
}
