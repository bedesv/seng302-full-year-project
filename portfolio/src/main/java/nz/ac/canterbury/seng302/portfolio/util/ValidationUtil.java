package nz.ac.canterbury.seng302.portfolio.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil {

    private ValidationUtil() {

    }

    /**
     * Checks for a valid title,
     * can be used in all children (e.g., deadline, milestone, events, evidence, groups)
     * Allows all Characters from and Language (L), Numbers(N), Punctuation (P), Whitespace (Z)
     * (title).isBlank(); will need to be tested elsewhere
     * @param title being validated
     * @return true if valid, false if not
     */
    public static boolean titleValid(String title) {
        String regex = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]";
        Pattern pattern = Pattern.compile(
                regex,
                Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(title);
        String result = matcher.replaceAll("");
        return result.length() == title.length();
    }
}
