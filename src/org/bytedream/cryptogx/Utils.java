package org.bytedream.cryptogx;

import java.util.TreeMap;

/**
 * <p>Support class<p/>
 *
 * @since 1.3.0
 */
public class Utils {

    public static TreeMap<String, String> algorithms = allAlgorithms();

    /**
     * <p>Get all available algorithms</p>
     *
     * @return all available algorithms
     *
     * @since 1.12.0
     */
    private static TreeMap<String, String> allAlgorithms() {
        TreeMap<String, String> return_map = new TreeMap<>();

        int[] aesKeySizes = {128, 192, 256};

        for (int i: aesKeySizes) {
            return_map.put("AES-" + i, "AES");
        }

        return return_map;
    }

    /**
     * <p>Checks if any character in {@param characters} appears in {@param string}</p>
     *
     * @param characters that should be searched in {@param string}
     * @param string that should be searched for the characters
     * @return if any character in {@param characters} appears in {@param string}
     *
     * @since 1.3.0
     */
    public static boolean hasAnyCharacter(CharSequence characters, String string) {
        for (char c: characters.toString().toCharArray()) {
            if (string.indexOf(c) != -1) {
                return false;
            }
        }
        return true;
    }

}
