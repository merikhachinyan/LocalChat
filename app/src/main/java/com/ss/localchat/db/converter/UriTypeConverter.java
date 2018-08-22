package com.ss.localchat.db.converter;

import android.arch.persistence.room.TypeConverter;
import android.net.Uri;

public class UriTypeConverter {

    @TypeConverter
    public static Uri toUri(String value) {
        return value == null ? null : Uri.parse(value);
    }

    @TypeConverter
    public static String toString(Uri value) {
        return value == null ? null : value.toString();
    }
}
