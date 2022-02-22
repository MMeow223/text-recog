package com.example.textrecognitionapp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

    /**
     * Check if the text match the datetime regex pattern
     *
     * @param textForValidation String
     * @return boolean
     */
    public static boolean DatetimePattern(String textForValidation) {
        return checkIfPatternMatch(textForValidation, "(0?[1-9]|[12][0-9]|3[01])\\/(0?[1-9]|[1][0-2])\\/(0?(19|20)\\d{2})+\\s(0?[0-9]|1[0-9]|2[0-3]):(0?[0-9]|[1-5][0-9])((?:A|a|P|p)\\.?(M|m)\\.?)+");
    }

    /**
     * Check if the text match the A1C result regex pattern
     *
     * @param textForValidation String
     * @return boolean
     */
    public static boolean A1CResultPattern(String textForValidation) {
        return checkIfPatternMatch(textForValidation, "[0-9]*\\.[0-9]+/[0-9]*\\.[0-9]+");
    }

    /**
     * Check if the text match the lot view regex pattern
     *
     * @param textForValidation String
     * @return boolean
     */
    public static boolean LotViewPattern(String textForValidation) {
        return checkIfPatternMatch(textForValidation, "(^(?=.{6}$) *\\d* *$)");
    }

    /**
     * Check if the text match the inst id regex pattern
     *
     * @param textForValidation String
     * @return boolean
     */
    public static boolean InstIdPattern(String textForValidation) {
        return checkIfPatternMatch(textForValidation, "^\\d[A-Z](?=.{4}$) *\\d* *$");
    }

    /**
     * Check if the text match the test id regex pattern
     *
     * @param textForValidation String
     * @return boolean
     */
    public static boolean TestIdPattern(String textForValidation) {
        return checkIfPatternMatch(textForValidation, "(^(?=.{5}$) *\\d* *$)");
    }

    /**
     * Matching entered text with the given pattern
     *
     * @param textForValidation String
     * @param regex String
     * @return boolean
     */
    private static boolean checkIfPatternMatch(String textForValidation, String regex) {
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(textForValidation);

        return matcher.matches();
    }
}

