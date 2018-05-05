package com.topicsbot.core.services.analysis.text;

import com.topicsbot.core.services.analysis.text.analyzers.*;
import com.topicsbot.core.services.analysis.text.daemons.HistoryCleanerDaemon;
import com.topicsbot.model.entities.chat.Chat;
import com.topicsbot.model.entities.chat.ChatLanguage;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: Artem Voronov
 */
public class LuceneAnalyzer implements TextAnalyzer {

  private static final Logger logger = Logger.getLogger("TEXT_ANALYZER");

  private static final String LUCENE_TEXT_FIELD = "text";
  private static final String LUCENE_CHAT_HASH_TAGS = "chat_hash_tags";
  private static final int KEYWORDS_COUNT = 10;
  private static final int HASHTAGS_COUNT = 10;

  private final Analyzer analyzer;
  private final String pathToLuceneIndexesDir;
  private final String pathToWorldLuceneIndexesDir;

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock read = lock.readLock();
  private final Lock write = lock.writeLock();

  public LuceneAnalyzer(ScheduledExecutorService scheduledExecutorService,
                        String pathToStopWordsDir, String pathToLuceneIndexesDir, String pathToWorldLuceneIndexesDir) {
    CharArraySet stopWords = getStopWords(pathToStopWordsDir);
    KeywordsAnalyzer topicsBotAnalyzer = new KeywordsAnalyzer(stopWords);
    HashTagsAnalyzers hashTagsAnalyzer = new HashTagsAnalyzers(stopWords);
    Map<String, Analyzer> analyzerPerField = new HashMap<>();
    analyzerPerField.put(LUCENE_CHAT_HASH_TAGS, hashTagsAnalyzer);
    this.analyzer = new PerFieldAnalyzerWrapper(topicsBotAnalyzer, analyzerPerField);
    this.pathToLuceneIndexesDir = pathToLuceneIndexesDir;
    this.pathToWorldLuceneIndexesDir = pathToWorldLuceneIndexesDir;

    scheduledExecutorService.scheduleWithFixedDelay(new HistoryCleanerDaemon(32, pathToLuceneIndexesDir, pathToWorldLuceneIndexesDir), 60L, 432_000L, TimeUnit.SECONDS);//once per 5 days
  }

  @Override
  public void index(String text, Chat chat) {
    indexMessageToChat(chat.getExternalId(), text, chat.getRebirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
    indexMessageToWorld(text, LocalDate.now().toString(), chat.getLanguage() );
  }

  @Override
  public List<String> getChatKeywords(Chat chat) {
    return getChatKeywordsFrequency(chat.getExternalId(), chat.getRebirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
  }

  @Override
  public List<String> getChatHashTags(Chat chat) {
    return getChatHashTagsFrequency(chat.getExternalId(), chat.getRebirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
  }

  @Override
  public List<String> getWorldKeywords(String dateIsoFormatted, ChatLanguage language) {
    return getWorldKeywordsFrequency(dateIsoFormatted, language);
  }

  @Override
  public List<String> getWorldHashTags(String dateIsoFormatted, ChatLanguage language) {
    return getWorldHashTagsFrequency(dateIsoFormatted, language);
  }

  @Override
  public Map<String, Long> getChatKeywordsExtended(String chatExternalId, LocalDate date) {
    return getChatKeywordsFrequencyExtended(chatExternalId, date.format(DateTimeFormatter.ISO_LOCAL_DATE));
  }

  @Override
  public Map<String, Long> getChatKeywordsExtended(String chatExternalId, LocalDate from, LocalDate till) {
    Map<String, Long> result = new HashMap<>();
    for (LocalDate dateIterator = from; !dateIterator.isAfter(till); dateIterator = dateIterator.plusDays(1)) {
      Map<String, Long> part = getChatKeywordsFrequencyExtended(chatExternalId, dateIterator.format(DateTimeFormatter.ISO_LOCAL_DATE));
      if (part != null) {
        result = Stream.concat(result.entrySet().stream(), part.entrySet().stream()).collect(Collectors.toMap(
            entry -> entry.getKey(),
            entry -> entry.getValue(),
            (v1, v2) -> v1 + v2));
      }
    }
    return result;
  }



  private void indexMessageToChat(String chatId, String text, String chatBirthday) {
    if (text == null || text.isEmpty())
      return;

    String filename = pathToLuceneIndexesDir + "/" + chatId + "_" + chatBirthday;
    try {
      createLuceneIndex(filename, text);
    } catch (IOException ex) {
      logger.error("unable to add chat index: " + ex.getMessage(), ex);
    }
  }

  private void indexMessageToWorld(String text, String dateIsoFormatted, ChatLanguage language) {
    if (text == null || text.isEmpty())
      return;

    String filename = pathToWorldLuceneIndexesDir + "/" + dateIsoFormatted + "_" + language;
    try {
      createLuceneIndex(filename, text);
    } catch (IOException ex) {
      logger.error("unable to add world index: " + ex.getMessage(), ex);
    }
  }

  private void createLuceneIndex(String filename, String text) throws IOException {
    try {
      write.lock();
      Directory directory = FSDirectory.open(Paths.get(filename));
      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      IndexWriter writer = new IndexWriter(directory, config);
      addDoc(writer, text);
      writer.close();
    } finally {
      write.unlock();
    }
  }

  private static void addDoc(IndexWriter writer, String text) throws IOException {
    org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
    doc.add(new TextField(LUCENE_TEXT_FIELD, text, Field.Store.YES));
    doc.add(new TextField(LUCENE_CHAT_HASH_TAGS, text, Field.Store.YES));
    writer.addDocument(doc);
  }

  private List<String> getChatKeywordsFrequency(String chatId, String chatBirthday) {
    String filename = pathToLuceneIndexesDir + "/" + chatId + "_" + chatBirthday;
    try {
      return readTermFrequency(filename, LUCENE_TEXT_FIELD, KEYWORDS_COUNT);
    } catch (IOException ex) {
      logger.error("unable to get chat keywords frequency: " + ex.getMessage(), ex);
      return null;
    }
  }

  private Map<String, Long> getChatKeywordsFrequencyExtended(String chatId, String chatBirthday) {
    String filename = pathToLuceneIndexesDir + "/" + chatId + "_" + chatBirthday;
    try {
      return readTermFrequency(filename, LUCENE_TEXT_FIELD);
    } catch (IOException ex) {
      logger.error("unable to get chat keywords frequency: " + ex.getMessage(), ex);
      return null;
    }
  }

  private List<String> getWorldKeywordsFrequency(String dateIsoFormatted, ChatLanguage language) {
    String filename = pathToWorldLuceneIndexesDir + "/" + dateIsoFormatted + "_" + language;
    try {
      return readTermFrequency(filename, LUCENE_TEXT_FIELD, KEYWORDS_COUNT);
    } catch (IOException ex) {
      logger.error("unable to get world keywords frequency: " + ex.getMessage(), ex);
      return null;
    }
  }

  private List<String> getChatHashTagsFrequency(String chatId, String chatBirthday) {
    String filename = pathToLuceneIndexesDir + "/" + chatId + "_" + chatBirthday;
    try {
      return readTermFrequency(filename, LUCENE_CHAT_HASH_TAGS, HASHTAGS_COUNT);
    } catch (IOException ex) {
      logger.error("unable to get chat hashtags frequency: " + ex.getMessage(), ex);
      return null;
    }
  }

  private List<String> getWorldHashTagsFrequency(String dateIsoFormatted, ChatLanguage language) {
    String filename = pathToWorldLuceneIndexesDir + "/" + dateIsoFormatted + "_" + language;
    try {
      return readTermFrequency(filename, LUCENE_CHAT_HASH_TAGS, HASHTAGS_COUNT);
    } catch (IOException ex) {
      logger.error("unable to get world hashtags frequency: " + ex.getMessage(), ex);
      return null;
    }
  }

  private List<String> readTermFrequency(String filename, String luceneFieldName, int amount) throws IOException {

    try {
      Map<String, Long> freq = readTermFrequency(filename, luceneFieldName);

      if (freq == null || freq.isEmpty())
        return null;

      List<String> keywords = new ArrayList<>(amount);
      freq.entrySet().stream()
          .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
          .limit(amount)
          .map(e -> e.getKey())
          .forEach(keywords::add);
      return keywords;
    } catch (Exception ex) {
      logger.error("Error at reading terms ", ex);
      return null;
    }
  }

  private Map<String, Long> readTermFrequency(String filename, String luceneFieldName) throws IOException {
    try {
      read.lock();
      Directory directory = FSDirectory.open(Paths.get(filename));
      IndexReader reader = DirectoryReader.open(directory);
      Terms terms = MultiFields.getTerms(reader, luceneFieldName);

      if (terms == null)
        return null;

      TermsEnum it = terms.iterator();
      Map<String, Long> result = new HashMap<>();
      while (it.next() != null) {
        String termName = it.term().utf8ToString();
        Term termInstance = new Term(luceneFieldName, termName);
        long termFreq = reader.totalTermFreq(termInstance);
        result.put(termName, termFreq);
      }
      return result;
    } catch (IndexNotFoundException ex) {
      return null;
    } catch (Exception ex) {
      logger.error("Error at reading terms ", ex);
      return null;
    } finally {
      read.unlock();
    }
  }

  private static CharArraySet getStopWords(String pathToStopWordsDir) {
    String[] languages = Arrays.stream(ChatLanguage.values()).map(Enum::name).map(String::toLowerCase).toArray(String[]::new);
    List<String> files = new ArrayList<>(ChatLanguage.values().length);

    for (String s : languages) {
      files.add(pathToStopWordsDir + "/" + s + ".txt");
    }
    CharArraySet result = new CharArraySet(2659, true);

    for (String path : files) {
      try (Stream<String> stream = Files.lines(Paths.get(path))) {
        stream.forEach(result::add);
      } catch (IOException ex) {
        throw new IllegalStateException("unable to read stop words");
      }
    }

    return result;
  }
}
