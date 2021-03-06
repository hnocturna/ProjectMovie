package com.example.nocturna.projectmovie.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
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
    /**
     * Removes all null values from the array
     * @param movieArray array of movies that may contain null values where the movie already exists
     *                   in the database
     * @return resized movieArray without null values
     */
    public static Movie[] cleanMovieArray(Movie[] movieArray) {
        List<Movie> list = new ArrayList<>(Arrays.asList(movieArray));
        list.removeAll(Collections.singleton(null));
        return list.toArray(new Movie[list.size()]);
    }

    /**
     * Calculates the average pixel color of an image to use as the divider in the DetailsFragment
     * @param bitmap Image to calculate the average pixel color of
     * @return Color as an int
     */
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

    /**
     * Converts a date String to a long in milliseconds to store in the database
     * @param dateString Date in String form from TheMovieDb API
     * @return date in milliseconds
     */
    public static long dateToLong(String dateString) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long dateInMillis = date.getTime();

        return dateInMillis;
    }

    /**
     * Converts the date to user-readable String
     * @param dateInMillis date in milliseconds from the database
     * @return Date formatted in String to populate TextViews
     */
    public static String longToDate(long dateInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        Date date = new Date();
        date.setTime(dateInMillis);
        String dateStr = sdf.format(date);

        return dateStr;
    }

    /**
     * Appends "/10" to the rating to make the rating more obvious
     * @param rating rating as a decimal from TheMovieDB API
     * @return Rating in String format "X/10"
     */
    public static String formatRating(double rating) {
        return rating + "/10";
    }


}
