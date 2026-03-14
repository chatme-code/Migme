package com.projectgoth.fusion.common;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;

public class TemplateStringProcessor {
   private static final char VARIABLE_MARKER = '#';
   private static final int REQUIRED_MARKER_LENGTH = 2;

   private static String getValue(StringBuilder variableNameContainer, Map<String, String> parameters) {
      String rawParamName = variableNameContainer.substring(2, variableNameContainer.length() - 2);
      boolean doHTMLEscape = false;
      String paramName;
      if (rawParamName.length() > 0) {
         if (rawParamName.startsWith("^")) {
            paramName = rawParamName.substring(1);
            doHTMLEscape = true;
         } else {
            paramName = rawParamName;
         }
      } else {
         paramName = rawParamName;
      }

      if (parameters.containsKey(paramName)) {
         String paramValue = (String)parameters.get(paramName);
         return doHTMLEscape ? StringEscapeUtils.escapeHtml3(paramValue) : paramValue;
      } else {
         return variableNameContainer.toString();
      }
   }

   public static void process(Writer outputWriter, String templateString, Map<String, String> parameters) throws IOException {
      if (templateString != null) {
         int receivedMarkerCount = 0;
         boolean variablePlaceHolderHasStarted = false;
         StringBuilder variableNameContainer = new StringBuilder();
         int templateStringLen = templateString.length();

         for(int i = 0; i < templateStringLen; ++i) {
            char c = templateString.charAt(i);
            switch(c) {
            case '#':
               variableNameContainer.append(c);
               ++receivedMarkerCount;
               if (receivedMarkerCount == 2) {
                  receivedMarkerCount = 0;
                  if (variablePlaceHolderHasStarted) {
                     variablePlaceHolderHasStarted = false;
                     String paramValue = getValue(variableNameContainer, parameters);
                     variableNameContainer = new StringBuilder();
                     if (paramValue != null) {
                        outputWriter.write(paramValue.toString());
                     }
                  } else {
                     variablePlaceHolderHasStarted = true;
                  }
               }
               break;
            default:
               if (variablePlaceHolderHasStarted) {
                  receivedMarkerCount = 0;
                  variableNameContainer.append(c);
               } else {
                  if (receivedMarkerCount > 0) {
                     receivedMarkerCount = 0;
                     outputWriter.append(variableNameContainer);
                     variableNameContainer = new StringBuilder();
                  }

                  outputWriter.append(c);
               }
            }
         }

         outputWriter.append(variableNameContainer);
      }

   }

   public static String process(String templateString, Map<String, String> parameters) throws IOException {
      StringWriter sw = new StringWriter();
      process(sw, templateString, parameters);
      return sw.toString();
   }
}
