package com.example.nocturna.projectmovie.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.test.ProviderTestCase2;

import com.example.nocturna.projectmovie.app.data.MovieContract;
import com.example.nocturna.projectmovie.app.data.MovieContract.MovieEntry;
import com.example.nocturna.projectmovie.app.data.MovieContract.GenreEntry;
import com.example.nocturna.projectmovie.app.data.MovieContract.LinkEntry;
import com.example.nocturna.projectmovie.app.data.MovieDbHelper;
import com.example.nocturna.projectmovie.app.data.MovieProvider;

/**
 * Created by hnoct on 12/3/2016.
 */

public class TestProvider extends ProviderTestCase2<MovieProvider> {
    public TestProvider() {
        super(MovieProvider.class, MovieContract.CONTENT_AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteTheDatabase();
    }

    /**
     * Helper method for ensuring the database is cleaned prior to each test run
     */
    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    /**
     * Basic test for raw database insertion followed by query using the ContentProvider
     */
    public void testBasicMovieAndGenreQuery() {
        ContentValues movieGenreValues = TestUtilities.createInterstellarDramaValues();
        MovieDbHelper mOpenHelper = new MovieDbHelper(mContext);
        SQLiteDatabase mDb = mOpenHelper.getWritableDatabase();
        long _id = mDb.insert(LinkEntry.TABLE_NAME, null, movieGenreValues);
        assertTrue("Error inserting movie and genre entry.", _id != -1);

        Cursor cursor = mContext.getContentResolver().query(
                LinkEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicMovieAndGenreQuery", cursor, movieGenreValues);

        cursor.close();
        mDb.close();
        mOpenHelper.close();
    }

    /**
     * Test for proper bulk insertion using the ContentProvider for each of the tables
     */
    public void testInsertReadProvider() {
        // Create the data to be inserted into the database
        ContentValues[] moviesAndGenresValues = TestUtilities.createBulkMovieAndGenres();
        ContentValues[] movieValues = TestUtilities.createBulkMovieValues();
        ContentValues[] genreValues = TestUtilities.createBulkGenreValues();

        // Insert the data into the database
        int rows = getMockContentResolver().bulkInsert(LinkEntry.CONTENT_URI, moviesAndGenresValues);

        // Check if the correct number of rows was inserted
        assertEquals("Failed to insert one or more rows into database.", moviesAndGenresValues.length, rows);

        rows = getMockContentResolver().bulkInsert(MovieEntry.CONTENT_URI, movieValues);
        assertEquals("Failed to insert one or more movies into database.", movieValues.length, rows);

        rows = getMockContentResolver().bulkInsert(GenreEntry.CONTENT_URI, genreValues);
        assertEquals("Failed to insert one or more genres into database.", genreValues.length, rows);

        // Query the link table using the provider
        Cursor cursor = getMockContentResolver().query(
                LinkEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Ensure the correct data is returned by the cursor
        cursor.moveToFirst();
        for (int i = 0; i < moviesAndGenresValues.length; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("testMovieAndGenresQuery. Error validating Link Entry " + i,
                    cursor, moviesAndGenresValues[i]);
        }

        if(Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: moviesQuery did not properly set NotificationUri",
                    cursor.getNotificationUri(), LinkEntry.CONTENT_URI);
        }
        cursor.close();

        // Query and validate the data from the movies table
        cursor = getMockContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        for (int i = 0; i < movieValues.length; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("testMovieQuery. Error validating Movie Entry " + i,
                    cursor, movieValues[i]);
        }

        if(Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: moviesQuery did not properly set NotificationUri",
                    cursor.getNotificationUri(), MovieEntry.CONTENT_URI);
        }
        cursor.close();

        // Query and validate the data from the genres table
        cursor = getMockContentResolver().query(
                GenreEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        for (int i = 0; i < genreValues.length; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("testGenreQuery. Error validating Genre Entry " + i,
                    cursor, genreValues[i]);
        }

        if(Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: moviesQuery did not properly set NotificationUri",
                    cursor.getNotificationUri(), GenreEntry.CONTENT_URI);
        }
        cursor.close();
    }

    /**
     * Test to confirm the correct type is returned by the ContentProvider
     */
    public void testGetType() {
        // Constants
        long movieId = 1;
        long genreId = 10;

        // Generate and check the URIs to make sure the ContentProvider returns the correct type
        String type = getMockContentResolver().getType(MovieEntry.CONTENT_URI);
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieEntry.CONTENT_TYPE, type);

        type = getMockContentResolver().getType(MovieEntry.buildMovieUriFromId(movieId));
        assertEquals("Error: the MovieEntry CONTENT_URI with movieId should return MovieEntry.CONTENT_ITEM_TYPE",
                MovieEntry.CONTENT_ITEM_TYPE, type);

        type = getMockContentResolver().getType(GenreEntry.CONTENT_URI);
        assertEquals("Error: the MovieEntry CONTENT_URI should return GenreEntry.CONTENT_TYPE",
                GenreEntry.CONTENT_TYPE, type);

        type = getMockContentResolver().getType(GenreEntry.buildGenreUriFromId(genreId));
        assertEquals("Error: the GenreEntry CONTENT_URI with genreID should return GenreEntry.CONTENT_TYPE",
                GenreEntry.CONTENT_ITEM_TYPE, type);

        type = getMockContentResolver().getType(LinkEntry.CONTENT_URI);
        assertEquals("Error: the LinkEntry CONTENT_URI should return LinkEntry.CONTENT_TYPE",
                LinkEntry.CONTENT_TYPE, type);

        type = getMockContentResolver().getType(LinkEntry.buildGenresUriFromMovieId(movieId));
        assertEquals("Error: the LinkEntry CONTENT_URI with movieId should return LinkEntry.CONTENT_TYPE",
                LinkEntry.CONTENT_TYPE, type);

        type = getMockContentResolver().getType(LinkEntry.buildMoviesUriFromGenreId(genreId));
        assertEquals("Error: the LinkEntry CONTENT_URI with genreId should return LinkEntry.CONTENT_TYPE",
                LinkEntry.CONTENT_TYPE, type);
    }

    /**
     * Test for the update function of the ContentProvider by first inserting data and then updating
     * it using the ContentProvider
     */
    public void testUpdateMovies() {
        // Insert initial values to be later updated
        ContentValues values = TestUtilities.createInterstellarValues();
        Uri movieUri = getMockContentResolver().insert(MovieEntry.CONTENT_URI, values);

        // Verify that a row was inserted successfully
        assertNotNull(movieUri);

        // Query the database to get the movieId inserted so that it can be used in the update
        Cursor cursor = getMockContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Check to make sure that a row was returned by the Cursor
        assertTrue(cursor.moveToFirst());

        // Get the movieId of row to be updated
        long movieId = cursor.getLong(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID));
        cursor.close();

        // Create new values to update the database with
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(MovieEntry.COLUMN_POPULARITY, 50);
        updatedValues.put(MovieEntry.COLUMN_RATING, 8.1);

        // Update using the ContentProvider
        getMockContentResolver().update(
                MovieEntry.CONTENT_URI,
                updatedValues,
                MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[] {Long.toString(movieId)}
        );

        // Re-query the database to check for that the new values were successfully inserted
        movieUri = MovieEntry.buildMovieUriFromId(movieId);
        cursor = getMockContentResolver().query(
                movieUri,
                null,
                null,
                null,
                null
        );

        // Check that the new values are correctly returned by the Cursor after the update operation
        TestUtilities.validateCursor("testUpdateMovies. Error validating updated entry", cursor, updatedValues);
        cursor.close();
    }
}
