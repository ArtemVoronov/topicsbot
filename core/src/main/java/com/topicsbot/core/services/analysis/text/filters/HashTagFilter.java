package com.topicsbot.core.services.analysis.text.filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Author: Artem Voronov
 */
public class HashTagFilter extends FilteringTokenFilter {
  private static Pattern HASHTAG_PATTERN = Pattern.compile("^#[^#]+$", Pattern.CASE_INSENSITIVE);

  public HashTagFilter(TokenStream in) {
    super(in);
  }

  @Override
  protected boolean accept() throws IOException {
    String currentTokenInStream = this.input.getAttribute(CharTermAttribute.class).toString().trim();
    boolean isHashTag = HASHTAG_PATTERN.matcher(currentTokenInStream).find();
    return isHashTag;
  }
}

