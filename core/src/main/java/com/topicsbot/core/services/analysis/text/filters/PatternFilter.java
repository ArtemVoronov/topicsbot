package com.topicsbot.core.services.analysis.text.filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Author: Artem Voronov
 */
public class PatternFilter extends FilteringTokenFilter {
  private static Pattern URI_PATTERN = Pattern.compile("^https?://[^/]+", Pattern.CASE_INSENSITIVE);
  private static Pattern DIGIT_JUNK = Pattern.compile("^\\d+.*\\d*", Pattern.CASE_INSENSITIVE);


  public PatternFilter(TokenStream in) {
    super(in);
  }

  @Override
  protected boolean accept() throws IOException {
    String currentTokenInStream = this.input.getAttribute(CharTermAttribute.class).toString().trim();
    boolean isUrl = URI_PATTERN.matcher(currentTokenInStream).find();
    boolean isEmailOrNickname = currentTokenInStream.contains("@");
    boolean isDigit = DIGIT_JUNK.matcher(currentTokenInStream).find();
    return !(isUrl || isEmailOrNickname || isDigit);
  }
}

