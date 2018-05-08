package com.topicsbot.core.services.analysis.topics

import com.topicsbot.model.entities.chat.ChatLanguage
import groovy.transform.CompileStatic
import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpStatus
import org.apache.http.HttpVersion
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicStatusLine
import org.apache.http.protocol.HttpContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

import java.nio.charset.StandardCharsets
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when

/**
 * Author: Artem Voronov
 */
@CompileStatic
@RunWith(MockitoJUnitRunner.Silent.class)
class WikiMediaStorageTest extends GroovyTestCase {

  @Mock
  private CloseableHttpClient httpClient

  @Mock
  private CloseableHttpResponse response

  @Mock
  private HttpEntity entity

  TopicsAnalyzer topicsAnalyzer

  @Before
  void setUp() {
    super.setUp()
    MockitoAnnotations.initMocks(this)
    topicsAnalyzer = new WikiMediaStorage(httpClient, 10)
  }

  @After
  void shutdown() {
    //nothing
  }

  @Test
  void keywordsToTopics() {
    List<String> keywords = [ "bee", "ant", "honey",  "football",  "baseball",  "friday",  "hello",  "car",  "money",  "car"]
    Set<String> expectedTopics = ["Honeypot ant", "Africanized bee", "David W. Carter High School", "List of The Eric Andre Show episodes", "Ant-Bee"] as Set
    mockWikiResponses()

    Set<String> actualTopics = topicsAnalyzer.keywordsToTopics(keywords, ChatLanguage.EN)

    assertTrue expectedTopics == actualTopics
  }

  private void mockWikiResponses() {
    when(httpClient.execute(any(HttpGet), any(HttpContext))).thenReturn(response)
    when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"))
    when(entity.getContent())
        .thenReturn(IOUtils.toInputStream(WikiMediaResponseExamples.THREE_TOPICS_XML, StandardCharsets.UTF_8))
        .thenReturn(IOUtils.toInputStream(WikiMediaResponseExamples.TWO_TOPICS_XML, StandardCharsets.UTF_8))
    when(response.getEntity()).thenReturn(entity)
  }
}
