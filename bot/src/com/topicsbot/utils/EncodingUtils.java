package com.topicsbot.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * author: Artem Voronov
 */
public class EncodingUtils {

  public static String encode(String value, String encoding) {
    try {
      return URLEncoder.encode(value, encoding);
    } catch (UnsupportedEncodingException ex) {
      throw new IllegalArgumentException("Unsupported encoding: " + encoding, ex);
    }
  }

  public static String decode(String value, String encoding) {
    try {
      return URLDecoder.decode(value, encoding);
    } catch (UnsupportedEncodingException ex) {
      throw new IllegalArgumentException("Unsupported encoding: " + encoding, ex);
    }
  }

  public static String convert(String inputEncoding, String outputEncoding, String value) {
    try {
      return new String(value.getBytes(inputEncoding), outputEncoding);
    } catch (UnsupportedEncodingException ex) {
      throw new IllegalArgumentException("Unsupported encoding: " + ex.getMessage(), ex);
    }
  }


}
