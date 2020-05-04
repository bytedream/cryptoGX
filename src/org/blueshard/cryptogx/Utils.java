package org.blueshard.cryptogx;

public class Utils {

    /**
     * <p>Checks if any character in {@param characters} appears in {@param string}</p>
     *
     * @param characters that should be searched in {@param string}
     * @param string that should be searched for the characters
     * @return if any character in {@param characters} appears in {@param string}
     */
    public static boolean hasAnyCharacter(CharSequence characters, String string) {
        for (char c: characters.toString().toCharArray()) {
            if (string.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

}
