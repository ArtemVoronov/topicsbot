package com.topicsbot.core.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

/**
 * Author: Artem Voronov
 */
public class XmlUtil {

  public static void normalize(Document document) {
    try {
      XPath xp = XPathFactory.newInstance().newXPath();
      NodeList emptyTextNodes = (NodeList) xp.evaluate("//text()[normalize-space(.)='']", document, XPathConstants.NODESET);
      for (Node node : asList(emptyTextNodes)) {
        node.getParentNode().removeChild(node);
      }
      NodeList textNodes = (NodeList) xp.evaluate("//text()", document, XPathConstants.NODESET);
      for (Node node : asList(textNodes)) {
        node.setNodeValue(node.getNodeValue().trim());
      }
    } catch (XPathExpressionException ex) {
      throw new RuntimeException("Error during normalizing of xml.", ex);
    }
  }

  public static Document string2xml(String xml) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    try
    {
      builder = factory.newDocumentBuilder();
      Document doc = builder.parse( new InputSource( new StringReader( xml ) ) );
      return doc;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static List<Node> asList(NodeList n) {
    return n.getLength() == 0 ? Collections.<Node>emptyList() : new NodeListWrapper(n);
  }

  private static final class NodeListWrapper extends AbstractList<Node> implements RandomAccess {
    private final NodeList list;

    NodeListWrapper(NodeList l) {
      list = l;
    }

    public Node get(int index) {
      return list.item(index);
    }

    public int size() {
      return list.getLength();
    }
  }
}
