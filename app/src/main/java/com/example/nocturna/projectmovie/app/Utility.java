package com.example.nocturna.projectmovie.app;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by hnoct on 12/12/2016.
 */

public class Utility {
    public static Movie[] cleanMovieArray(Movie[] movieArray) {
        List<Movie> list = new ArrayList<Movie>(Arrays.asList(movieArray));
        list.removeAll(Collections.singleton(null));
        return list.toArray(new Movie[list.size()]);
    }

    public static int getDominantColor(Bitmap bitmap) {
        if (null == bitmap) return Color.TRANSPARENT;

        int redBucket = 0;
        int greenBucket = 0;
        int blueBucket = 0;
        int alphaBucket = 0;

        boolean hasAlpha = bitmap.hasAlpha();
        int pixelCount = bitmap.getWidth() * bitmap.getHeight();
        int[] pixels = new int[pixelCount];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int y = 0, h = bitmap.getHeight(); y < h; y++)
        {
            for (int x = 0, w = bitmap.getWidth(); x < w; x++)
            {
                int color = pixels[x + y * w]; // x + y * width
                redBucket += (color >> 16) & 0xFF; // Color.red
                greenBucket += (color >> 8) & 0xFF; // Color.greed
                blueBucket += (color & 0xFF); // Color.blue
                if (hasAlpha) alphaBucket += (color >>> 24); // Color.alpha
            }
        }

        return Color.argb(
                (hasAlpha) ? (alphaBucket / pixelCount) : 255,
                redBucket / pixelCount,
                greenBucket / pixelCount,
                blueBucket / pixelCount);
    }

    public static long dateToLong(String dateString) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = sdf.parse(dateString);
            Log.v("TEST", date.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long dateInMillis = date.getTime();

        return dateInMillis;
    }

    public static String longToDate(long dateInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        Date date = new Date();
        date.setTime(dateInMillis);
        String dateStr = sdf.format(date);

        return dateStr;
    }

    public static String formatRating(double rating) {
        return rating + "/10";
    }
}
