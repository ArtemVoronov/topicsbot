package com.topicsbot.core.services.analysis.text

import com.topicsbot.model.entities.chat.Chat
import com.topicsbot.model.entities.chat.ChatLanguage
import com.topicsbot.model.entities.chat.ChatTest
import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

import java.time.LocalDate

/**
 * Author: Artem Voronov
 */
@CompileStatic
@RunWith(MockitoJUnitRunner.Silent.class)
class LuceneAnalyzerTest extends GroovyTestCase {

  TextAnalyzer textAnalyzer
  String baseDir = LuceneAnalyzerTest.class.getClassLoader().getResource("").path
  String pathToLuceneIndexesDir = "${baseDir}chat_lucene"
  String pathToWorldLuceneIndexesDir = "${baseDir}world_lucene"

  @Before
  void setUp() {
    super.setUp()
    MockitoAnnotations.initMocks(this)
    textAnalyzer = new LuceneAnalyzer(pathToLuceneIndexesDir, pathToWorldLuceneIndexesDir)
  }

  @After
  void shutdown() {
    new File(pathToLuceneIndexesDir).deleteDir()
    new File(pathToWorldLuceneIndexesDir).deleteDir()
  }

  @Test
  void messageIndexing() {
    String message = "hello world"
    Chat chat = ChatTest.createChat()

    textAnalyzer.indexMessage(message, chat)

    assertWasMessageIndexed()
  }

  @Test
  void chatKeywords() {
    String message = "hello world hello hunt the wumpus hello world hello world hunt"
    List<String> expectedKeywords = ["hello", "world", "hunt" , "wumpus"]
    Chat chat = ChatTest.createChat()

    textAnalyzer.indexMessage(message, chat)
    List<String> actualKeywords = textAnalyzer.getChatKeywords(chat)

    assertEquals expectedKeywords, actualKeywords
  }


  @Test
  void chatKeywordsExtendedByDate() {
    String message = "hello world hello hunt the wumpus hello world hello world hunt"
    Map<String, Long> expectedKeywords = [hello: 4L, world: 3L, hunt: 2L, wumpus:1L] as Map
    Chat chat = ChatTest.createChat()

    textAnalyzer.indexMessage(message, chat)
    Map<String, Long> actualKeywords = textAnalyzer.getChatKeywordsWithFrequency(chat, chat.rebirthDate)

    assertEquals expectedKeywords, actualKeywords
  }

  @Test
  void chatKeywordsExtendedPeriod() {
    String messageToday = "hello world hello hunt"
    String messageTomorrow = "the wumpus hello world hello world hunt"
    Map<String, Long> expectedKeywords = [hello: 4L, world: 3L, hunt: 2L, wumpus:1L] as Map
    LocalDate today = LocalDate.now()
    LocalDate tomorrow = today.plusDays(1L)
    Chat chat = ChatTest.createChat(rebirthDate: today)

    textAnalyzer.indexMessage(messageToday, chat)
    chat.rebirthDate = tomorrow
    textAnalyzer.indexMessage(messageTomorrow, chat)
    Map<String, Long> actualKeywords = textAnalyzer.getChatKeywordsWithFrequency(chat, today, tomorrow)

    assertEquals expectedKeywords, actualKeywords
  }

  @Test
  void chatHashTags() {
    String message = "hello world #hashtagone #hashtagone #hashtagone #hastagtwo #hastagtwo #hastagthree"
    List<String> expectedHashTags = ["#hashtagone", "#hastagtwo", "#hastagthree"]
    Chat chat = ChatTest.createChat()

    textAnalyzer.indexMessage(message, chat)
    List<String> actualHashTags = textAnalyzer.getChatHashTags(chat)

    assertEquals expectedHashTags, actualHashTags
  }

  @Test
  void worldKeywords() {
    String messageFromChat1 = "hello world hello hunt"
    String messageFromChat2 = "the wumpus hello world hello world hunt"
    String messageFromChat3 = "прювет топикс бот прювет топикс прювет"
    List<String> expectedKeywordsEnglish = ["hello", "world", "hunt" , "wumpus"]
    List<String> expectedKeywordsRussian = ["прювет", "топикс", "бот"]
    LocalDate rebirthDate = LocalDate.now()
    Chat chat1 = ChatTest.createChat(externalId: "-630123892091", rebirthDate: rebirthDate, language: ChatLanguage.EN)
    Chat chat2 = ChatTest.createChat(externalId: "-630123892092", rebirthDate: rebirthDate, language: ChatLanguage.EN)
    Chat chat3 = ChatTest.createChat(externalId: "-630123892093", rebirthDate: rebirthDate, language: ChatLanguage.RU)

    textAnalyzer.indexMessage(messageFromChat1, chat1)
    textAnalyzer.indexMessage(messageFromChat2, chat2)
    textAnalyzer.indexMessage(messageFromChat3, chat3)
    List<String> actualKeywordsEnglish = textAnalyzer.getWorldKeywords(rebirthDate, ChatLanguage.EN)
    List<String> actualKeywordsRussian = textAnalyzer.getWorldKeywords(rebirthDate, ChatLanguage.RU)

    assertEquals expectedKeywordsEnglish, actualKeywordsEnglish
    assertEquals expectedKeywordsRussian, actualKeywordsRussian
  }

  @Test
  void worldHashTags() {
    String messageFromChat1 = "hello #hastagtwo #hastagtwo #hastagthree"
    String messageFromChat2 = "world #hashtagone #hashtagone #hashtagone"
    String messageFromChat3 = "прювет #топиксбот"
    List<String> expectedHashTagsEnglish = ["#hashtagone", "#hastagtwo", "#hastagthree"]
    List<String> expectedHashTagsRussian = ["#топиксбот"]
    LocalDate rebirthDate = LocalDate.now()
    Chat chat1 = ChatTest.createChat(externalId: "-630123892091", rebirthDate: rebirthDate, language: ChatLanguage.EN)
    Chat chat2 = ChatTest.createChat(externalId: "-630123892092", rebirthDate: rebirthDate, language: ChatLanguage.EN)
    Chat chat3 = ChatTest.createChat(externalId: "-630123892093", rebirthDate: rebirthDate, language: ChatLanguage.RU)

    textAnalyzer.indexMessage(messageFromChat1, chat1)
    textAnalyzer.indexMessage(messageFromChat2, chat2)
    textAnalyzer.indexMessage(messageFromChat3, chat3)
    List<String> actualHashTagsEnglish = textAnalyzer.getWorldHashTags(rebirthDate, ChatLanguage.EN)
    List<String> actualHashTagsRussian = textAnalyzer.getWorldHashTags(rebirthDate, ChatLanguage.RU)

    assertEquals expectedHashTagsEnglish , actualHashTagsEnglish
    assertEquals expectedHashTagsRussian , actualHashTagsRussian
  }

  private void assertWasMessageIndexed() {
    assertTrue FileUtils.sizeOf(new File(pathToLuceneIndexesDir)) > 0
    assertTrue FileUtils.sizeOf(new File(pathToWorldLuceneIndexesDir)) > 0
  }

}
