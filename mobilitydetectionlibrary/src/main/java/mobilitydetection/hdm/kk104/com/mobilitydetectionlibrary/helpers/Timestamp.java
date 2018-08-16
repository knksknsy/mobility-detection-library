package mobilitydetection.hdm.kk104.com.mobilitydetectionlibrary.helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Timestamp {

    public static String generateTimestamp() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        return dateFormat.format(date);
    }

    public static String getTime(String timestamp) {
        return timestamp.split("T")[1];
    }

    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return dateFormat.format(date);
    }
}