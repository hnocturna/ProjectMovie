package com.example.nocturna.projectmovie.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.nocturna.projectmovie.app.data.MovieContract.MovieEntry;
import com.example.nocturna.projectmovie.app.data.MovieContract.GenreEntry;
import com.example.nocturna.projectmovie.app.data.MovieContract.LinkEntry;
import com.example.nocturna.projectmovie.app.data.MovieDbHelper;
import com.example.nocturna.projectmovie.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by hnoct on 12/2/2016.
 */

public class TestUtilities extends AndroidTestCase {
    private static final String LOG_TAG = TestUtilities.class.getSimpleName();

    /**
     * Method for checking that the correct cursor was returned by the the database query
     * @param error user defined error to report if the test fails
     * @param valueCursor Cursor that was returned by the database query
     * @param expectedValues ContentValues containing the values that were inserted
     */
    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    /**
     * Method for checking that the value of the column returned by the Cursor matches the expected
     * value that was inserted as ContentValues
     * @param error user defined error to return if the test fails
     * @param valueCursor Cursor that was returned by the database query
     * @param expectedValues ContentValues containing the values that were inserted
     */
    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createInterstellarValues() {
        ContentValues interstellarValues = new ContentValues();
        interstellarValues.put(MovieEntry.COLUMN_MOVIE_ID, 1);
        interstellarValues.put(MovieEntry.COLUMN_TITLE, "Interstellar");
        interstellarValues.put(MovieEntry.COLUMN_OVERVIEW, "To go where no man has gone before.");
        interstellarValues.put(MovieEntry.COLUMN_RATING, 10);
        interstellarValues.put(MovieEntry.COLUMN_RELEASE_DATE, "10-01-2014");
        interstellarValues.put(MovieEntry.COLUMN_BACKDROP, "backdrop/path1");
        interstellarValues.put(MovieEntry.COLUMN_POPULARITY, 100);
        interstellarValues.put(MovieEntry.COLUMN_POSTER, "poster/path1");
        interstellarValues.put(MovieEntry.COLUMN_TRAILER, "trailer/path1");

        return interstellarValues;
    }

    static ContentValues createDramaValues() {
        ContentValues dramaValues = new ContentValues();
        dramaValues.put(GenreEntry.COLUMN_GENRE_ID, 10);
        dramaValues.put(GenreEntry.COLUMN_GENRE, "Action");

        return dramaValues;
    }

    static ContentValues createInterstellarDramaValues() {
        ContentValues interstellarDramaValues = new ContentValues();
        interstellarDramaValues.put(MovieEntry.COLUMN_MOVIE_ID, 1);
        interstellarDramaValues.put(GenreEntry.COLUMN_GENRE_ID, 10);

        return interstellarDramaValues;
    }

    /**
     * Creates test values for movies for bulk insertion into the database
     * @return Array of ContentValues for movies to test database
     */
    static ContentValues[] createBulkMovieValues() {
        ContentValues movieValues1 = new ContentValues();
        movieValues1.put(MovieEntry.COLUMN_MOVIE_ID, 1);
        movieValues1.put(MovieEntry.COLUMN_TITLE, "Interstellar");
        movieValues1.put(MovieEntry.COLUMN_OVERVIEW, "To go where no man has gone before.");
        movieValues1.put(MovieEntry.COLUMN_RATING, 10);
        movieValues1.put(MovieEntry.COLUMN_RELEASE_DATE, "10-01-2014");
        movieValues1.put(MovieEntry.COLUMN_BACKDROP, "backdrop/path1");
        movieValues1.put(MovieEntry.COLUMN_POPULARITY, 100);
        movieValues1.put(MovieEntry.COLUMN_POSTER, "poster/path1");
        movieValues1.put(MovieEntry.COLUMN_TRAILER, "trailer/path1");

        ContentValues movieValues2 = new ContentValues();
        movieValues2.put(MovieEntry.COLUMN_MOVIE_ID, 2);
        movieValues2.put(MovieEntry.COLUMN_TITLE, "The Martian");
        movieValues2.put(MovieEntry.COLUMN_OVERVIEW, "To go home from where no one has ever been before");
        movieValues2.put(MovieEntry.COLUMN_RATING, 9.9);
        movieValues2.put(MovieEntry.COLUMN_RELEASE_DATE, "10-02-2014");
        movieValues2.put(MovieEntry.COLUMN_BACKDROP, "backdrop/path2");
        movieValues2.put(MovieEntry.COLUMN_POPULARITY, 99);
        movieValues2.put(MovieEntry.COLUMN_POSTER, "poster/path2");
        movieValues2.put(MovieEntry.COLUMN_TRAILER, "trailer/path2");

        ContentValues[] contentValues = new ContentValues[] {movieValues1, movieValues2};
        return contentValues;
    }

    /**
     * Create link values for movies and genres for bulk insertion into database
     * @return array of ContentValues of movies and their genres to insert into the database
     */
    static ContentValues[] createBulkMovieAndGenres() {
        ContentValues movieGenreValues1 = new ContentValues();
        movieGenreValues1.put(MovieEntry.COLUMN_MOVIE_ID, 1);
        movieGenreValues1.put(GenreEntry.COLUMN_GENRE_ID, 10);

        ContentValues movieGenreValues2 = new ContentValues();
        movieGenreValues2.put(MovieEntry.COLUMN_MOVIE_ID, 1);
        movieGenreValues2.put(GenreEntry.COLUMN_GENRE_ID, 11);

        ContentValues movieGenreValues3 = new ContentValues();
        movieGenreValues3.put(MovieEntry.COLUMN_MOVIE_ID, 2);
        movieGenreValues3.put(GenreEntry.COLUMN_GENRE_ID, 10);

        ContentValues[] contentValues = new ContentValues[] {movieGenreValues1, movieGenreValues2, movieGenreValues3};

        return contentValues;
    }

    /**
     * Create test values for genres for bulk insertion into the database
     * @return array of genre ContentValues to insert into database
     */
    static ContentValues[] createBulkGenreValues() {
        ContentValues genreValues1 = new ContentValues();
        genreValues1.put(GenreEntry.COLUMN_GENRE_ID, 10);
        genreValues1.put(GenreEntry.COLUMN_GENRE, "Action");

        ContentValues genreValues2 = new ContentValues();
        genreValues2.put(GenreEntry.COLUMN_GENRE_ID, 11);
        genreValues2.put(GenreEntry.COLUMN_GENRE, "Drama");

        ContentValues[] contentValues = new ContentValues[] {genreValues1, genreValues2};
        return contentValues;
    }

    /**
     * Method for inserting values for the movies and genres into the Link Table. Must be done prior
     * testing other tables because other tables are constrained by a foreign key in the Link Table.
     * @param context used for passing context to MovieDbHelper
     * @return array of row IDs inserted
     */
    static long[] insertMoviesAndGenres(Context context) {
        MovieDbHelper movieDbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        int rowsInserted = 0;
        long[] rowIds = new long[3];

        ContentValues[] moviesAndGenreValues = createBulkMovieAndGenres();
        for (int i = 0; i < moviesAndGenreValues.length; i++) {
            long _id = db.insert(LinkEntry.TABLE_NAME, null, moviesAndGenreValues[i]);
            assertTrue("Error: Failure to insert movies and genre values into Link Table", _id > 0);

            if (_id > 0) {
                rowIds[i] = _id;
                rowsInserted++;
            }
        }
        assertTrue("Error inserting one or more rows into database!", rowsInserted == moviesAndGenreValues.length);

        db.close();
        return rowIds;
    }

    /**
     * Method for inserting values for movies into the Movies Table
     * @param context used for passing context to MovieDbHelper
     * @return array of row IDs inserted into the database
     */
    static long[] insertMovies(Context context) {
        MovieDbHelper movieDbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        long[] rowIds = new long[2];
        int rowsInserted = 0;

        ContentValues[] movieValues = createBulkMovieValues();
        for (int i = 0; i < movieValues.length; i++) {
            long _id = db.insert(MovieEntry.TABLE_NAME, null, movieValues[i]);
            assertTrue("Error: Failure to insert movie row into Movie Table", _id > 0);

            if (_id > 0) {
                rowIds[i] = _id;
                rowsInserted++;
            }
        }
        assertTrue("Error inserting one or more rows into Movie Table", rowsInserted == movieValues.length);

        db.close();
        return rowIds;
    }

    /**
     * Method for inserting values for the genres into the Genres Table
     * @param context used for passing context to MovieDbHelper
     * @return array of row IDs inserted into database
     */
    static long[] insertGenres(Context context) {
        MovieDbHelper movieDbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        long[] rowIds = new long[2];
        int rowsInserted = 0;

        ContentValues[] genreValues = createBulkGenreValues();
        for (int i = 0; i < genreValues.length; i++) {
            long _id = db.insert(GenreEntry.TABLE_NAME, null, genreValues[i]);
            assertTrue("Error: Failure to insert row into Genre Table", _id > 0);

            if (_id > 0) {
                rowIds[i] = _id;
                rowsInserted++;
            }
        }
        assertTrue("Failure inserting one or more rows into the Genre Table", rowsInserted == genreValues.length);

        db.close();
        return rowIds;
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
