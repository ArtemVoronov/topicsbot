package com.topicsbot.core.services.analysis.filters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Author: Artem Voronov
 */
public class DeleteJunkFilter extends TokenFilter {
  private static Pattern JUNK_PATTERN = Pattern.compile("^[^/]+", Pattern.CASE_INSENSITIVE);
  protected CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
  protected PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);

  protected DeleteJunkFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {

    // Loop over tokens in the token stream to find the next one
    // that is not empty
    String nextToken = null;
    while (nextToken == null) {

      // Reached the end of the token stream being processed
      if ( ! this.input.incrementToken()) {
        return false;
      }

      // Get text of the current token and remove any
      // leading/trailing whitespace.
      String currentTokenInStream = this.input.getAttribute(CharTermAttribute.class).toString().trim();

      // Save the token if it is not an empty string
      if (currentTokenInStream.length() > 0) {
        nextToken = currentTokenInStream;
      }
    }

    // Save the current token
    this.charTermAttribute.setEmpty().append(nextToken);
    this.positionIncrementAttribute.setPositionIncrement(1);
    return true;
  }
}
