package com.kiddo.parser.verifier;

/**
 * A very naive verifier to check whether a given word is valid in a given language.
 */
public class NaiveWordVerifier implements WordVerifier {
    @Override
    public boolean isWordValid(String word, LanguageType languageType) {
        switch (languageType) {
            case CHINESE -> {
                return isChineseWord(word);
            }
            case ENGLISH -> {
                return isEnglishWord(word);
            }
        }

        return false;
    }

    /*
     * Util to check whether a given word is English. We're using a very simple definition of English:
     * Only the character a is accepted for a 1 character word.
     * The string cannot consist of any digits or any non character symbol.
     *
     * A better approach is to have to a dictionary of all English words. We can store them in a set which we can easily check.
     */
    private boolean isEnglishWord(String word) {
        int size = word.length();
        if (size < 1) {
            return false;
        }
        if (size == 1) {
            return word.charAt(0) == 'a';
        }

        return !word.matches(".*[^a-zA-Z].*");
    }

    /*
     * Use UnicodeBlock class to check if a given word contains any Chinese characters.
     * Return true is any character is Chinese.
     */
    private boolean isChineseWord(String word) {
        for (char c : word.toCharArray()) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)
                    || Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)
                    || Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B.equals(block)
                    || Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION.equals(block)
                    || Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT.equals(block)
                    || Character.UnicodeBlock.KANGXI_RADICALS.equals(block)) {
                return true;
            }
        }
        return false;
    }
}
