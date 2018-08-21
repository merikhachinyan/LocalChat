package com.ss.localchat.util;

import android.content.Context;
import android.util.TypedValue;

public class Util {

    private Util() {
    }

    public static int dpToPx(Context pContext, int pValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pValue, pContext.getResources().getDisplayMetrics());
    }
}
