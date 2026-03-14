package com.projectgoth.fusion.chatnewsfeed.util;

import org.apache.axis.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtil {
   public static Node getLastNode(Document xmlDocument, String tag) {
      Node node = null;
      NodeList nodeList = xmlDocument.getElementsByTagName(tag);
      if (nodeList != null && nodeList.getLength() > 0) {
         node = nodeList.item(nodeList.getLength() - 1);
      }

      return node;
   }

   public static NodeList getNodesByTag(Document xmlDocument, String tag) {
      NodeList nodeList = xmlDocument.getElementsByTagName(tag);
      return nodeList;
   }

   public static Node getNode(NodeList nodeList, int index) {
      Node node = null;
      if (nodeList != null && nodeList.getLength() > 0) {
         node = nodeList.item(index);
      }

      return node;
   }

   public static String getStringAttribute(Node node, String attributeName) {
      String value = "";
      if (node != null && node.getNodeType() == 1) {
         NamedNodeMap attributesMap = node.getAttributes();
         Node attributeNode = attributesMap.getNamedItem(attributeName);
         if (attributeNode != null) {
            value = attributeNode.getTextContent();
         }
      }

      return value;
   }

   public static int getIntegerAttribute(Node node, String attributeName) {
      int value = 0;
      if (node != null && node.getNodeType() == 1) {
         NamedNodeMap attributesMap = node.getAttributes();
         Node attributeNode = attributesMap.getNamedItem(attributeName);
         if (attributeNode != null) {
            value = getIntValue(attributeNode);
         }
      }

      return value;
   }

   public static float getFloatAttribute(Node node, String attributeName) {
      float value = 0.0F;
      if (node != null && node.getNodeType() == 1) {
         NamedNodeMap attributesMap = node.getAttributes();
         Node attributeNode = attributesMap.getNamedItem(attributeName);
         if (attributeNode != null) {
            value = getDecimalValue(attributeNode);
         }
      }

      return value;
   }

   public static int getIntValue(Node node) {
      int value = 0;
      String text = node.getTextContent();
      if (!StringUtils.isEmpty(text)) {
         value = Integer.parseInt(text);
      }

      return value;
   }

   public static float getDecimalValue(Node node) {
      float value = 0.0F;
      String text = node.getTextContent();
      if (!StringUtils.isEmpty(text)) {
         value = Float.parseFloat(text);
      }

      return value;
   }
}
