package com.ss.localchat.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {

    private static final SimpleDateFormat sFormat = new SimpleDateFormat("H:m");

    public static String getChatMessageDate(Date date) {
        return sFormat.format(date);
    }
}
