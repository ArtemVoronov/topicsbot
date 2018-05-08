package com.topicsbot.core.services.analysis.text.daemons

import com.topicsbot.core.services.analysis.text.LuceneAnalyzer
import com.topicsbot.core.services.analysis.text.TextAnalyzer
import com.topicsbot.core.utils.DateTimeUtils
import com.topicsbot.model.entities.chat.Chat
import com.topicsbot.model.entities.chat.ChatTest
import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

import java.nio.file.Files
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Author: Artem Voronov
 */
@CompileStatic
@RunWith(MockitoJUnitRunner.Silent.class)
class HistoryCleanerDaemonTest extends GroovyTestCase {

  TextAnalyzer textAnalyzer
  HistoryCleanerDaemon historyCleanerDaemon
  String baseDir = HistoryCleanerDaemon.class.getClassLoader().getResource("").path
  String pathToLuceneIndexesDir = "${baseDir}chat_lucene"
  String pathToWorldLuceneIndexesDir = "${baseDir}world_lucene"
  int historyTimeToLiveInDays = 0

  @Before
  void setUp() {
    super.setUp()
    MockitoAnnotations.initMocks(this)
    historyCleanerDaemon = new HistoryCleanerDaemon(historyTimeToLiveInDays, pathToLuceneIndexesDir, pathToWorldLuceneIndexesDir)
    textAnalyzer = new LuceneAnalyzer(pathToLuceneIndexesDir, pathToWorldLuceneIndexesDir)
  }

  @After
  void shutdown() {
    new File(pathToLuceneIndexesDir).deleteDir()
    new File(pathToWorldLuceneIndexesDir).deleteDir()
  }

  @Test
  void cleanHistory() {
    String messageToday = "hello world"
    String messageTomorrow = "test test"
    LocalDateTime today = LocalDateTime.now()
    LocalDateTime tomorrow = today.plusDays(1)
    Clock tomorrowClock = DateTimeUtils.localDateTime2Clock(tomorrow)
    Chat chat = ChatTest.createChat(rebirthDate: today.toLocalDate())

    textAnalyzer.index(messageToday, chat)
    chat.rebirthDate = tomorrow.toLocalDate()
    textAnalyzer.index(messageTomorrow, chat)
    historyCleanerDaemon.cleanLuceneIndexes(tomorrowClock)

    assertLuceneIndexesDoNotExists(chat, today.toLocalDate())
    assertLuceneIndexesExists(chat, tomorrow.toLocalDate())
  }

  private void assertLuceneIndexesExists(Chat chat, LocalDate localDate) {
    File chatLuceneIndexes = new File(pathToLuceneIndexesDir + "/" + chat.externalId + "_" + localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
    File worldLuceneIndexes = new File(pathToWorldLuceneIndexesDir + "/" + localDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "_" + chat.language.name())
    assertTrue Files.exists(chatLuceneIndexes.toPath())
    assertTrue Files.exists(worldLuceneIndexes.toPath())
    assertTrue FileUtils.sizeOf(chatLuceneIndexes) > 0
    assertTrue FileUtils.sizeOf(worldLuceneIndexes) > 0
  }

  private void assertLuceneIndexesDoNotExists(Chat chat, LocalDate localDate) {
    File chatLuceneIndexes = new File(pathToLuceneIndexesDir + "/" + chat.externalId + "_" + localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
    File worldLuceneIndexes = new File(pathToWorldLuceneIndexesDir + "/" + localDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "_" + chat.language.name())
    assertFalse Files.exists(chatLuceneIndexes.toPath())
    assertFalse Files.exists(worldLuceneIndexes.toPath())
  }
}
