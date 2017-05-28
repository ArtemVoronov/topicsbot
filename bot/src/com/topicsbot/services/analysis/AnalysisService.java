package com.topicsbot.services.analysis;

import com.topicsbot.model.chat.Chat;
import com.topicsbot.model.chat.ChatLanguage;
import com.topicsbot.services.analysis.analyzers.HashTagsAnalyzers;
import com.topicsbot.services.analysis.analyzers.TopicBotAnalyzer;
import com.topicsbot.services.analysis.generators.TopicsGenerator;
import com.topicsbot.services.analysis.generators.WikiMediaClient;
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
import java.util.stream.Stream;

/**
 * Author: Artem Voronov
 */
public class AnalysisService implements AnalysisProvider {

  private static final Logger logger = Logger.getLogger("ANALYSIS_SERVICE");
  private static final String LUCENE_TEXT_FIELD = "text";
  private static final String LUCENE_CHAT_HASH_TAGS = "chat_hash_tags";
  private static final int KEYWORDS_COUNT = 10;
  private static final int HASHTAGS_COUNT = 10;

  private final Analyzer analyzer;
  private final TopicsGenerator topicsGenerator;
  private final String pathToLuceneIndexesDir;
  private final String pathToWorldLuceneIndexesDir;

  public AnalysisService(String pathToStopWordsDir, String pathToLuceneIndexesDir, String pathToWorldLuceneIndexesDir) {
    CharArraySet stopWords = getStopWords(pathToStopWordsDir);
    TopicBotAnalyzer topicsBotAnalyzer = new TopicBotAnalyzer(stopWords);
    HashTagsAnalyzers hashTagsAnalyzer = new HashTagsAnalyzers(stopWords);

    Map<String, Analyzer> analyzerPerField = new HashMap<>();
    analyzerPerField.put(LUCENE_CHAT_HASH_TAGS, hashTagsAnalyzer);

    this.analyzer = new PerFieldAnalyzerWrapper(topicsBotAnalyzer, analyzerPerField);
    this.topicsGenerator = new WikiMediaClient(KEYWORDS_COUNT);
    this.pathToLuceneIndexesDir = pathToLuceneIndexesDir;
    this.pathToWorldLuceneIndexesDir = pathToWorldLuceneIndexesDir;
  }

  @Override
  public void index(String text, Chat chat) {
    indexMessage(chat.getExternalId(), text, chat.getRebirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
    indexMessage(text, LocalDate.now().toString(), chat.getLanguage() );
  }

  @Override
  public List<String> getChatKeywords(Chat chat) {
    return getChatKeywordsFrequency(chat.getExternalId(), chat.getRebirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE), false);
  }

  @Override
  public Set<String> getChatTopics(List<String> keywords, ChatLanguage language) {
    return topicsGenerator.getTopics(keywords, language);
  }

  @Override
  public List<String> getChatHashTags(Chat chat) {
    return getChatHashTagsFrequency(chat.getExternalId(), chat.getRebirthDate().format(DateTimeFormatter.ISO_LOCAL_DATE), false);
  }

  @Override
  public List<String> getWorldKeywords(String dateIsoFormatted, ChatLanguage language) {
    return getWorldKeywordsFrequency(dateIsoFormatted, language, false);
  }

  @Override
  public Set<String> getWorldTopics(List<String> keywords, ChatLanguage language) {
    return topicsGenerator.getTopics(keywords, language);
  }

  @Override
  public List<String> getWorldHashTags(String dateIsoFormatted, ChatLanguage language) {
    return getWorldHashTagsFrequency(dateIsoFormatted, language, false);
  }

  private List<String> getChatKeywordsFrequency(String chatId, String chatBirthday, boolean extended) {
    String filename = pathToLuceneIndexesDir + "/" + chatId + "_" + chatBirthday;
    try {
      return readTermFrequency(filename, extended, LUCENE_TEXT_FIELD, KEYWORDS_COUNT);
    } catch (IOException ex) {
      logger.error("unable to get chat keywords frequency: " + ex.getMessage(), ex);
      return null;
    }
  }

  private List<String> getWorldKeywordsFrequency(String dateIsoFormatted, ChatLanguage language, boolean extended) {
    String filename = pathToWorldLuceneIndexesDir + "/" + dateIsoFormatted + "_" + language;
    try {
      return readTermFrequency(filename, extended, LUCENE_TEXT_FIELD, KEYWORDS_COUNT);
    } catch (IOException ex) {
      logger.error("unable to get world keywords frequency: " + ex.getMessage(), ex);
      return null;
    }
  }

  private List<String> getChatHashTagsFrequency(String chatId, String chatBirthday, boolean extended) {
    String filename = pathToLuceneIndexesDir + "/" + chatId + "_" + chatBirthday;
    try {
      return readTermFrequency(filename, extended, LUCENE_CHAT_HASH_TAGS, HASHTAGS_COUNT);
    } catch (IOException ex) {
      logger.error("unable to get chat hashtags frequency: " + ex.getMessage(), ex);
      return null;
    }
  }

  private List<String> getWorldHashTagsFrequency(String dateIsoFormatted, ChatLanguage language, boolean extended) {
    String filename = pathToWorldLuceneIndexesDir + "/" + dateIsoFormatted + "_" + language;
    try {
      return readTermFrequency(filename, extended, LUCENE_CHAT_HASH_TAGS, HASHTAGS_COUNT);
    } catch (IOException ex) {
      logger.error("unable to get world hashtags frequency: " + ex.getMessage(), ex);
      return null;
    }
  }

  private static List<String> readTermFrequency(String filename, boolean extended, String luceneFieldName, int amount) throws IOException {
    Directory directory = FSDirectory.open(Paths.get(filename));

    try {
      IndexReader reader = DirectoryReader.open(directory);
      Terms terms = MultiFields.getTerms(reader, luceneFieldName);

      if (terms == null)
        return null;

      TermsEnum it = terms.iterator();
      Map<String, Long> freq = new HashMap<>();
      while (it.next() != null) {
        String termName = it.term().utf8ToString();
        Term termInstance = new Term(luceneFieldName, termName);
        long termFreq = reader.totalTermFreq(termInstance);
        freq.put(termName, termFreq);
      }
      List<String> keywords = new ArrayList<>(amount);
      if (extended) {
        freq.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(amount)
            .map(e -> e.getKey() + "(" + e.getValue() + ")")
            .forEach(keywords::add);
      } else {
        freq.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(amount)
            .map(e -> e.getKey())
            .forEach(keywords::add);
      }
      return keywords;
    } catch (IndexNotFoundException ex) {
//      logger.debug("Index not found for " + filename);
      return null;
    } catch (Exception ex) {
      logger.error("Error at reading terms ", ex);
      return null;
    }
  }

  private void indexMessage(String chatId, String text, String chatBirthday) {
    if (text == null || text.isEmpty())
      return;

    String filename = pathToLuceneIndexesDir + "/" + chatId + "_" + chatBirthday;
    try {
      createLuceneIndex(filename, text);
    } catch (IOException ex) {
      logger.error("unable to add chat index: " + ex.getMessage(), ex);
    }
  }

  private void indexMessage(String text, String dateIsoFormatted, ChatLanguage language) {
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
    Directory directory = FSDirectory.open(Paths.get(filename));
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    IndexWriter writer = new IndexWriter(directory, config);
    addDoc(writer, text);
    writer.close();
  }

  private static void addDoc(IndexWriter writer, String text) throws IOException {
    org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
    doc.add(new TextField(LUCENE_TEXT_FIELD, text, Field.Store.YES));
    doc.add(new TextField(LUCENE_CHAT_HASH_TAGS, text, Field.Store.YES));
    writer.addDocument(doc);
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
        logger.error(ex.getMessage(), ex);
      }
    }

    return result;
  }
}
