package com.projectgoth.fusion.payment.mol;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MOLXMLUtils {
   private MOLXMLUtils() {
   }

   public static String getChildNodeTagValue(Element parent, String tagName) {
      NodeList nodeList = parent.getElementsByTagName(tagName);
      int nodeListCount = nodeList.getLength();

      for(int i = 0; i < nodeListCount; ++i) {
         Node node = nodeList.item(i);
         if (node.getNodeType() == 1 && node.getParentNode().isSameNode(parent)) {
            Element e = (Element)node;
            Node c = e.getFirstChild();
            if (c != null) {
               return c.getNodeValue();
            }

            return null;
         }
      }

      return null;
   }
}
