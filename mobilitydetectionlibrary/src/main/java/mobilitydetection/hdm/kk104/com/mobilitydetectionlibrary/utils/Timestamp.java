package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Util class for generating timestamps
 */
public class Timestamp {

    /**
     * Generates a timestamp in the following pattern: yyyy-MM-ddTHH:mm:ss
     *
     * @return timestamp
     */
    public static String generateTimestamp() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        return dateFormat.format(date);
    }

    /**
     * Return a timestamp in the following pattern: HH:mm:ss
     *
     * @param timestamp timestamp string to be formatted
     * @return formatted timestamp
     */
    public static String getTime(String timestamp) {
        return timestamp.split("T")[1];
    }

    /**
     * Return a timestamp in the following pattern: yyyy-MM-dd
     *
     * @return timestamp string
     */
    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return dateFormat.format(date);
    }

    /**
     * Converts a timestamp string into a Calendar object
     *
     * @param timestamp has the following pattern: yyyy-MM-ddTHH:mm:ss
     * @return Calendar object. Null when the pattern is incorrect
     */
    public static Calendar getDate(String timestamp) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            calendar.setTime(dateFormat.parse(timestamp));
            return calendar;
        } catch (ParseException e) {
            e.getMessage();
        }
        return null;
    }

    /**
     * Calculates the time difference in milliseconds of two timestamps. t2 - t1 = difference
     *
     * @param t1 has the following pattern: yyyy-MM-ddTHH:mm:ss
     * @param t2 has the following pattern: yyyy-MM-ddTHH:mm:ss
     * @return time difference in milliseconds
     */
    public static long getDifference(String t1, String t2) {
        return getDate(t2).getTimeInMillis() - getDate(t1).getTimeInMillis();
    }
}
