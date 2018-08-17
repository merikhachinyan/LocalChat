package com.ss.localchat.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatUtil {

    private static final SimpleDateFormat sFormatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private static final SimpleDateFormat sFormatDate = new SimpleDateFormat("MMM d", Locale.getDefault());

    public static String formatChatDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (day == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
            return sFormatTime.format(date);
        } else {
            return sFormatDate.format(date);
        }
    }

    public static String formatMessageDate(Date date) {
        return sFormatTime.format(date);
    }
}
