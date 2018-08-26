package com.ss.localchat.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatUtil {

    private static final SimpleDateFormat sFormatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private static final SimpleDateFormat sFormatDateShort = new SimpleDateFormat("MMM d", Locale.getDefault());

    private static final SimpleDateFormat sFormatDateLong = new SimpleDateFormat("d MMMM, yyyy", Locale.getDefault());

    public static String formatChatDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar currentCalendar = Calendar.getInstance();

        if (calendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH)
                && calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                && calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
            return sFormatTime.format(date);
        } else {
            return sFormatDateShort.format(date);
        }
    }

    public static String formatMessageDate(Date date) {
        return sFormatTime.format(date);
    }

    public static String formatDate(Date date) {
        return sFormatDateLong.format(date);
    }
}
