package org.mian.gitnex.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeHelper {

    public static String customDateFormatForToast(String customDate) {

        String[] parts = customDate.split("\\+");
        String part1 = parts[0] + "Z";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        Date createdTime = null;
        try {
            createdTime = formatter.parse(part1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DateFormat format = DateFormat.getDateTimeInstance();
        return format.format(createdTime);

    }

    public static String customDateFormatForToastDateFormat(Date customDate) {

        DateFormat format = DateFormat.getDateTimeInstance();
        return format.format(customDate);

    }

}
