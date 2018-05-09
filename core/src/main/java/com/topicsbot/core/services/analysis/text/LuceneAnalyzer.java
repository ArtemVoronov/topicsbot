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
import java.net.URL;
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
  private final String chatLucenePath;
  private final String worldLucenePath;

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock read = lock.readLock();
  private final Lock write = lock.writeLock();

  public LuceneAnalyzer(String chatLucenePath, String worldLucenePath, ScheduledExecutorService scheduledExecutorService, int historyTimeToLiveInDays) {
    this(chatLucenePath, worldLucenePath);

    scheduledExecutorService.scheduleWithFixedDelay(new HistoryCleanerDaemon(historyTimeToLiveInDays, chatLucenePath, worldLucenePath), 120L, 86400L, TimeUnit.SECONDS);
  }

  public LuceneAnalyzer(String chatLucenePath, String worldLucenePath) {
    CharArraySet stopWords = getStopWords();
    KeywordsAnalyzer topicsBotAnalyzer = new KeywordsAnalyzer(stopWords);
    HashTagsAnalyzers hashTagsAnalyzer = new HashTagsAnalyzers(stopWords);
    Map<String, Analyzer> analyzerPerField = new HashMap<>();
    analyzerPerField.put(LUCENE_CHAT_HASH_TAGS, hashTagsAnalyzer);
    this.analyzer = new PerFieldAnalyzerWrapper(topicsBotAnalyzer, analyzerPerField);
    this.chatLucenePath = chatLucenePath;
    this.worldLucenePath = worldLucenePath;
  }

  @Override
  public void indexMessage(String message, Chat chat) {
    if (message == null || message.isEmpty())
      return;

    indexMessageToChat(message, chat);
    indexMessageToWorld(message, chat);
  }

  @Override
  public List<String> getChatKeywords(Chat chat) {
    return getChatKeywordsFrequency(chat.getExternalId(), chat.getRebirthDate());
  }

  @Override
  public List<String> getChatHashTags(Chat chat) {
    return getChatHashTagsFrequency(chat.getExternalId(), chat.getRebirthDate());
  }

  @Override
  public List<String> getWorldKeywords(LocalDate date, ChatLanguage language) {
    return getWorldKeywordsFrequency(date, language);
  }

  @Override
  public List<String> getWorldHashTags(LocalDate date, ChatLanguage language) {
    return getWorldHashTagsFrequency(date, language);
  }

  @Override
  public Map<String, Long> getChatKeywordsWithFrequency(Chat chat, LocalDate date) {
    return getChatKeywordsFrequencyExtended(chat.getExternalId(), date);
  }

  @Override
  public Map<String, Long> getChatKeywordsWithFrequency(Chat chat, LocalDate from, LocalDate till) {
    Map<String, Long> result = new HashMap<>();
    for (LocalDate dateIterator = from; !dateIterator.isAfter(till); dateIterator = dateIterator.plusDays(1)) {
      Map<String, Long> part = getChatKeywordsFrequencyExtended(chat.getExternalId(), dateIterator);
      if (part != null) {
        result = Stream.concat(result.entrySet().stream(), part.entrySet().stream()).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1 + v2));
      }
    }
    return result;
  }

  private void indexMessageToChat(String message, Chat chat) {
    String filename = chatLucenePath + "/" + chat.getExternalId() + "_" + format(chat.getRebirthDate());
    try {
      createLuceneIndex(filename, message);
    } catch (IOException ex) {
      logger.error("unable to index message to chat: " + ex.getMessage(), ex);
    }
  }

  private void indexMessageToWorld(String message, Chat chat) {
    String filename = getWorldLuceneIndexesPathDir(chat.getLanguage(), chat.getRebirthDate());
    try {
      createLuceneIndex(filename, message);
    } catch (IOException ex) {
      logger.error("unable to index message to world: " + ex.getMessage(), ex);
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

  private List<String> getChatKeywordsFrequency(String chatExternalId, LocalDate date) {
    String filename = getChatLuceneIndexesPathDir(chatExternalId, date);
    return readTermFrequency(filename, LUCENE_TEXT_FIELD, KEYWORDS_COUNT);
  }

  private Map<String, Long> getChatKeywordsFrequencyExtended(String chatExternalId, LocalDate date) {
    String filename = getChatLuceneIndexesPathDir(chatExternalId, date);
    return readTermFrequency(filename, LUCENE_TEXT_FIELD);
  }

  private List<String> getChatHashTagsFrequency(String chatExternalId, LocalDate date) {
    String filename = getChatLuceneIndexesPathDir(chatExternalId, date);
    return readTermFrequency(filename, LUCENE_CHAT_HASH_TAGS, HASHTAGS_COUNT);
  }

  private List<String> getWorldKeywordsFrequency(LocalDate date, ChatLanguage language) {
    String filename = getWorldLuceneIndexesPathDir(language, date);
    return readTermFrequency(filename, LUCENE_TEXT_FIELD, KEYWORDS_COUNT);
  }

  private List<String> getWorldHashTagsFrequency(LocalDate date, ChatLanguage language) {
    String filename = getWorldLuceneIndexesPathDir(language, date);
    return readTermFrequency(filename, LUCENE_CHAT_HASH_TAGS, HASHTAGS_COUNT);
  }

  private String getChatLuceneIndexesPathDir(String chatExternalId, LocalDate date) {
    return chatLucenePath + "/" + chatExternalId + "_" + format(date);
  }

  private String getWorldLuceneIndexesPathDir(ChatLanguage language, LocalDate date) {
    return worldLucenePath + "/" + format(date) + "_" + language.name();
  }

  private List<String> readTermFrequency(String filename, String luceneFieldName, int amount) {
    Map<String, Long> frequency = readTermFrequency(filename, luceneFieldName);

    if (frequency == null || frequency.isEmpty())
      return Collections.emptyList();

    List<String> result = new ArrayList<>(amount);
    frequency.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(amount)
        .map(Map.Entry::getKey)
        .forEach(result::add);
    return result;
  }

  private Map<String, Long> readTermFrequency(String luceneIndexesPathDir, String luceneFieldName) {
    try {
      read.lock();
      Directory directory = FSDirectory.open(Paths.get(luceneIndexesPathDir));
      IndexReader reader = DirectoryReader.open(directory);
      Terms terms = MultiFields.getTerms(reader, luceneFieldName);

      if (terms == null)
        return Collections.emptyMap();

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
      return Collections.emptyMap();
    } catch (Exception ex) {
      logger.error("Error at reading terms ", ex);
      return Collections.emptyMap();
    } finally {
      read.unlock();
    }
  }

  private static CharArraySet getStopWords() {
    URL url = LuceneAnalyzer.class.getClassLoader().getResource("stopwords");

    if (url == null)
      throw new IllegalStateException("unable to find stop words dir");

    String pathToStopWordsDir = url.getPath();


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

  private static String format(LocalDate date) {
    return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }
}
