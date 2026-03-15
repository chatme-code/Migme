package com.projectgoth.i18n;


import com.projectgoth.localization.Language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by felixqk on 2/12/14.
 */
public class PatternTranslation {
    private String originalPatternString;
    private String translatedString;

    private static final Pattern SPECIAL_CHARACTER_PATTERN = Pattern.compile("([\\\\\\.\\[\\]\\{\\}\\(\\)\\*\\+\\?\\^\\$\\|])");
    private static final String DEFAULT_PATTERN_SYMBOL_REG = "%\\{'[A-Za-z0-9_-]+'\\}";
    private static final Pattern DEFAULT_PATTERN_SYMBOL_REG_MODIFY_PATTERN = Pattern.compile("%\\\\\\{'[A-Za-z0-9_-]+'\\\\\\}");
    private static final Pattern DEFAULT_PATTERN_SYMBOL_REG_PATTERN = Pattern.compile(DEFAULT_PATTERN_SYMBOL_REG);
    private static final String DEFAULT_PATTERN_SYMBOL_REG_REPLACE = "(.+)";

    private String beginCheck;
    private String endCheck;
    private String containsCheck;
    private Map<String, Integer> substitutionMap;
    private Pattern pattern;
    private static List<PatternTranslation> patternTranslations = new ArrayList<PatternTranslation>();

    public PatternTranslation(String originalPatternString,String translatedString) {
        this.originalPatternString = originalPatternString;
        this.translatedString = translatedString;

        generateCheck();
        generateSubstitutionMap();
        generatePattern();

    }

    private void generateCheck() {
        String[] nonPatternStringArray = originalPatternString.split(DEFAULT_PATTERN_SYMBOL_REG, -1);
        int nonPatternStringArrayLength = nonPatternStringArray.length;

        beginCheck = nonPatternStringArray[0];
        endCheck = nonPatternStringArray[nonPatternStringArrayLength - 1];
        int maxLength = 0;
        containsCheck = "";
        for(int i = 1; i < nonPatternStringArrayLength - 1; i++) {
            if(nonPatternStringArray[i].length() > maxLength) {
                containsCheck = nonPatternStringArray[i];
                maxLength = nonPatternStringArray[i].length();
            }
        }
    }

    private void generatePattern(){

        String originalPatternStringReplacement = SPECIAL_CHARACTER_PATTERN.matcher(originalPatternString).replaceAll("\\\\$1");
        originalPatternStringReplacement = DEFAULT_PATTERN_SYMBOL_REG_MODIFY_PATTERN.matcher(originalPatternStringReplacement).replaceAll(DEFAULT_PATTERN_SYMBOL_REG_REPLACE);
        pattern = Pattern.compile("^" + originalPatternStringReplacement + "$", 0);
    }

    private void generateSubstitutionMap(){
        Matcher matcher = DEFAULT_PATTERN_SYMBOL_REG_PATTERN.matcher(this.originalPatternString);
        substitutionMap = new HashMap<String, Integer>();
        int i = 1;
        while (matcher.find()) {
            substitutionMap.put(matcher.group(), i);
            i++;
        }

    }

    public static Language initializePattern(Language language){

        resetPatternList();

        Iterator<Map.Entry<String,String>> iterator = language.translation.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String,String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();

            if(isValidPattern(key)) {
                add(key, value);
                iterator.remove();
            }
        }
        return language;
    }

    private boolean quickCheck(String inputString) {
        return inputString.startsWith(beginCheck) &&
               inputString.endsWith(endCheck) &&
               inputString.contains(containsCheck);
    }

    private static void resetPatternList() {
        PatternTranslation.patternTranslations = new ArrayList<PatternTranslation>();
    }

    private static void add(String key, String value) {
        PatternTranslation.patternTranslations.add(new PatternTranslation(key, value));
    }

    private static boolean isValidPattern(String key) {
        return DEFAULT_PATTERN_SYMBOL_REG_PATTERN.matcher(key).find();
    }

    public static String tryTranslate(String inputString) {

        for(PatternTranslation patternTranslation : patternTranslations) {
            /// may found match
            if(patternTranslation.quickCheck(inputString)) {
                String translatedString = patternTranslation.translate(inputString);
                if(translatedString != null) return translatedString;
            }
        }
        return null;
    }

    private String translate(String inputString) {
        Matcher matcher = pattern.matcher(inputString);
        String translatedStringWithSubstitution = translatedString;
        if(matcher.matches()) {
            for(Map.Entry<String, Integer> entry : substitutionMap.entrySet()) {
                translatedStringWithSubstitution = translatedStringWithSubstitution.replace(entry.getKey(), matcher.group(entry.getValue()));
            }
            return translatedStringWithSubstitution;
        }
        return null;
    }
}
