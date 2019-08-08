package com.lxf.googlemapsdemo.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author: lixiaofei
 * @date: 2019/8/8
 * @version: 1.0.0
 */
public class Utils {
    public static final String DATE_FORMAT_PATTERN_YMD_HMS = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_PATTERN_YMD_HM = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_PATTERN_YMD = "yyyy-MM-dd";
    public static final String DATE_FORMAT_PATTERN_YM = "yyyy-MM";
    private static final String DATE_FORMAT_PATTERN_HM = "HH:mm";
    private static final String DATE_FORMAT_PATTERN_H = "HH";
    public static final String DATE_FORMAT_PATTERN_MDY = "MMMMdd,yyyy";

    /**
     * 获取当前时间
     */
    public static String getCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN_YMD_HMS, Locale.CHINA);
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        String currentTime = simpleDateFormat.format(date);
        return currentTime;
    }

    /**
     * 将视图转为bitmap对象
     *
     * @param context
     * @param view
     * @return
     */
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }
}
