package com.kiddo.parser.verifier;

public interface WordVerifier {
    /**
     * Verify whether a word is a valid word in a given language.
     * @param word Word to verify.
     * @param languageType Language type of the word.
     * @return true or false.
     */
    boolean isWordValid(String word, LanguageType languageType);
}
