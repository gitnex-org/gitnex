package org.mian.gitnex.helpers;

import android.content.Context;
import org.mian.gitnex.R;
import org.ocpsoft.prettytime.PrettyTime;
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

    public static String formatTime(Date date, Locale locale, String timeFormat, Context context) {

        switch (timeFormat) {

            case "pretty": {
                PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
                return prettyTime.format(date);
            }

            case "normal": {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
                return formatter.format(date);
            }

            case "normal1": {
                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
                return formatter.format(date);
            }

        }

        return "";
    }

    public static String customDateFormatForToastDateFormat(Date customDate) {

        DateFormat format = DateFormat.getDateTimeInstance();
        return format.format(customDate);

    }

}
