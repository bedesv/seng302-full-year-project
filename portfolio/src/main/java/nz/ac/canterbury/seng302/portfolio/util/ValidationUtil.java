package nz.ac.canterbury.seng302.portfolio.util;

import org.springframework.ui.Model;

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

    public static String stripTitle(String title){
        String regex = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]";
        Pattern pattern = Pattern.compile(
                regex,
                Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(title);
        return matcher.replaceAll("");
    }

    /**
     * Checks if the given attribute valid
     * Add attribute to model if isn't valid
     * @param model global model
     * @param attribute title of attribute being checked
     * @param value attribute value
     * @return true if valid, else false
     */
    public static boolean validAttribute(Model model, String attribute, String title, String value) {
        if (attribute.contains("Name") && !ValidationUtil.nameValid(value)){
            model.addAttribute(attribute + "Error", title +  " cannot contain special characters");
            return false;
        } else if (!ValidationUtil.titleValid(value)){
            model.addAttribute(attribute + "Error", title +  " cannot contain special characters");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Allows all Characters from and Language (L), Punctuation (P), Whitespace (Z)
     * @param name being validated
     * @return true if valid, else false.
     */
    public static boolean nameValid(String name) {
        String regex = "[^\\p{L}\\p{P}\\p{Z}]";
        Pattern pattern = Pattern.compile(
                regex,
                Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(name);
        String result = matcher.replaceAll("");
        return result.length() == name.length();
    }
}
