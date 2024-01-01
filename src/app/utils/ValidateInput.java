package app.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValidateInput {
    private ValidateInput() {

    }
    private static final int CURRENT_YEAR = 2023;
    private static final int MIN_YEAR = 1900;
    private static final int MAX_MONTH_DAY = 31;
    private static final int MAX_FEBRUARY_DAY = 28;
    private static final int MAX_MONTHS = 12;
    private static final int FEBRUARY = 2;
    private static final int DAY_INDEX = 1;
    private static final int MONTH_INDEX = 2;
    private static final int YEAR_INDEX = 3;

    /**
     * Check if a given date String is valid.
     * @param data the date as String
     * @return true if valid
     */
    public static boolean isValidDateFormat(final String data) {
        Pattern pattern = Pattern.compile("^([0-9]{2})-([0-9]{2})-([0-9]{4})$");
        Matcher matcher = pattern.matcher(data);
        if (!matcher.matches()) {
            return false;
        } else {
            String day = matcher.group(DAY_INDEX);
            String month = matcher.group(MONTH_INDEX);
            String year = matcher.group(YEAR_INDEX);
            int intYear = Integer.parseInt(year);
            int intDay = Integer.parseInt(day);
            int intMonth = Integer.parseInt(month);
            if (intYear < MIN_YEAR || intYear > CURRENT_YEAR) {
                return false;
            }
            if (intMonth > MAX_MONTHS) {
                return false;
            }
        return (intMonth != FEBRUARY || intDay <= MAX_FEBRUARY_DAY) && intDay <= MAX_MONTH_DAY;
        }
    }
}
