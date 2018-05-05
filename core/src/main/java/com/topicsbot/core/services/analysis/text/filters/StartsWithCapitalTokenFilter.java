package com.topicsbot.core.services.analysis.text.filters;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;

import java.io.IOException;

/**
 * Author: Artem Voronov
 */
public class StartsWithCapitalTokenFilter extends FilteringTokenFilter {

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

  public StartsWithCapitalTokenFilter(TokenStream in) {
    super(in);
  }

  @Override
  protected boolean accept() throws IOException {

    return Character.isUpperCase(Character.codePointAt(termAtt.buffer(),0));
  }
}