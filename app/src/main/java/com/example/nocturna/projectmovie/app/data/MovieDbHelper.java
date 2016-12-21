package com.example.nocturna.projectmovie.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.opengl.GLException;
import android.util.Log;

import com.example.nocturna.projectmovie.app.data.MovieContract.MovieEntry;
import com.example.nocturna.projectmovie.app.data.MovieContract.GenreEntry;
import com.example.nocturna.projectmovie.app.data.MovieContract.LinkEntry;

/**
 * Created by hnoct on 12/1/2016.
 * Helper class for creating the SQLite database
 */

public class MovieDbHelper extends SQLiteOpenHelper {
    // Set up constants
    private static final int DATABASE_VERSION = 3;
    // Name of the database file in the phone's directory
    public static final String DATABASE_NAME = "movies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the table using the schema provided and the constants from the MoviesContract Class
     * @param db the database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table storing a movie and all their unique attributes as a single row
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                // Since MovieId is unique, it will be used as the primary key
                " " + MovieEntry._ID + " INTEGER AUTO INCREMENT, " +
                MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                MovieEntry.COLUMN_TITLE + " TEXT UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RATING + " REAL NOT NULL, " +
                MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_BACKDROP + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_TRAILER + " TEXT, " +
                // Since a single movie can be multiple genres, a link table will be utilized
                // to relate all the genres to a movie and vice-versa
                "FOREIGN KEY (" + MovieEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                LinkEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_MOVIE_ID + "));";

        // Table storing each genre ID and their String equivalent as a single row
        final String SQL_CREATE_GENRE_TABLE = "CREATE TABLE " + GenreEntry.TABLE_NAME + " (" +
                // GenreId is unique and therefore used as the primary key
                GenreEntry.COLUMN_GENRE_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                GenreEntry.COLUMN_GENRE + " TEXT UNIQUE NOT NULL, " +
                // Since a genre referes to multiple movies, it must reference the link table that
                // stores non-unique entries
                "FOREIGN KEY (" + GenreEntry.COLUMN_GENRE_ID + ") REFERENCES " +
                LinkEntry.TABLE_NAME + " (" + GenreEntry.COLUMN_GENRE_ID + "));";

        // Table used to relate the movie and its genres. Because the other tables have unique
        // entries in each row, they include foreign keys to the link table. Therefore, the link
        // table must be created first storing each movie and its genres
        final String SQL_CREATE_LINK_TABLE = "CREATE TABLE " + LinkEntry.TABLE_NAME + " (" +
                MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                GenreEntry.COLUMN_GENRE_ID + " INTEGER NOT NULL, " +
                // By making the combination a primary key, it will guarantee that each row is
                // unique.
                "PRIMARY KEY (" + MovieEntry.COLUMN_MOVIE_ID + ", " + GenreEntry.COLUMN_GENRE_ID + "));";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_GENRE_TABLE);
        db.execSQL(SQL_CREATE_LINK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            // All data is queried from the web so there is no need to preserve the data on upgrade.
            db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + GenreEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + LinkEntry.TABLE_NAME);
            onCreate(db);
        }
    }
}
