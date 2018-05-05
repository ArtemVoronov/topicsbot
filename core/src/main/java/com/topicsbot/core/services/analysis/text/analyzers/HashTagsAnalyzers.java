package com.topicsbot.core.services.analysis.text.analyzers;

import com.topicsbot.core.services.analysis.text.filters.EmojiconFilter;
import com.topicsbot.core.services.analysis.text.filters.HashTagFilter;
import com.topicsbot.core.services.analysis.text.filters.PatternFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.util.CharArraySet;

import java.io.StringReader;

/**
 * Author: Artem Voronov
 */
public class HashTagsAnalyzers extends Analyzer {
  private final CharArraySet stopSet;
  private final int wordDelimiterConfig;

  public HashTagsAnalyzers(CharArraySet stopSet) {
    this.stopSet = stopSet;
    wordDelimiterConfig = WordDelimiterFilter.GENERATE_WORD_PARTS;
  }

  @Override
  protected TokenStreamComponents createComponents(String s) {
    Tokenizer whitespaceTokenizer = new WhitespaceTokenizer();
    whitespaceTokenizer.setReader(new StringReader(s));

    TokenStream tokenStream = new LowerCaseFilter(whitespaceTokenizer);
    tokenStream = new PatternFilter(tokenStream);
    tokenStream = new EmojiconFilter(tokenStream);
    //tokenStream = new WordDelimiterFilter(tokenStream, wordDelimiterConfig, null); //TODO
    tokenStream = new StopFilter(tokenStream, stopSet);
    tokenStream = new HashTagFilter(tokenStream);

    return new TokenStreamComponents(whitespaceTokenizer, tokenStream);
  }
}
