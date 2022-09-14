package nz.ac.canterbury.seng302.portfolio.util;

import java.io.Serializable;
import java.util.Comparator;

public class DateComparator implements Comparator<String>, Serializable {
    public int compare(String date1, String date2) {
        int date1Int = convertDateToInteger(date1);
        int date2Int = convertDateToInteger(date2);

        return date1Int - date2Int;
    }

    // converts date string with format YYYY-mm-dd to integer of value YYYYMMDD
    private int convertDateToInteger(String date) {
        String[] tokens = date.split("-");
        return Integer.parseInt(tokens[0] + tokens[1] + tokens[2]);
    }
}
