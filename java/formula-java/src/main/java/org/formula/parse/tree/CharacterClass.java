package org.formula.parse.tree;

public class CharacterClass {

    public static final String SPACE_CHARACTERS = " ";
    public static final String BLANK_CHARACTERS = SPACE_CHARACTERS + "\t";
    public static final String DIGIT_CHARACTERS = "1234567890";

    public static final String LOWER_CASE_CHARACTERS = new String(new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' });
    public static final String UPPER_CASE_CHARACTERS = new String(new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' });
    public static final String ALPHA_CHARACTERS = LOWER_CASE_CHARACTERS + UPPER_CASE_CHARACTERS;
    public static final String ALPHANUMERIC_CHARACTERS = ALPHA_CHARACTERS + DIGIT_CHARACTERS;
    public static final String WORD_CHARACTERS = ALPHANUMERIC_CHARACTERS + "_";
}
