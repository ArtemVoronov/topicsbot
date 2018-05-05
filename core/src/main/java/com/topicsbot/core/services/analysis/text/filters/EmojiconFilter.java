package com.topicsbot.core.services.analysis.text.filters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

/**
 * Author: Artem Voronov
 */
public class EmojiconFilter extends TokenFilter {
  protected CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
  protected PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);

  public EmojiconFilter(TokenStream input) {
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
      currentTokenInStream = removeEmojicons(currentTokenInStream);
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

  private static final char HIDDEN_CHAR = '\u2063';
  private static final char VARIATION_SELECTOR_16 = '\ufe0f';//(char index 65039)

  private static final String pattern = "[" +
      "\\x{1F601}-\\x{1F64F}" +   //Emoticons
      "\\x{2702}-\\x{27B0}" +     //Dingbats
      "\\x{1F680}-\\x{1F6C5}" +   //Transport and map symbols
      "\\x{1F300}-\\x{1F5FF}" +   //Other additional symbols
      "\\x{1F914}" +              //thinking emoticon
      HIDDEN_CHAR + VARIATION_SELECTOR_16 +
      "]";

  private static String removeEmojicons(String emojiconnedString) {
    return emojiconnedString.replaceAll(pattern,"");
  }
}
