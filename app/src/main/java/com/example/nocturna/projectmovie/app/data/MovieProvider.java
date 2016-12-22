package com.example.nocturna.projectmovie.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.example.nocturna.projectmovie.app.data.MovieContract.MovieEntry;
import com.example.nocturna.projectmovie.app.data.MovieContract.GenreEntry;
import com.example.nocturna.projectmovie.app.data.MovieContract.LinkEntry;

/**
 * Created by hnoct on 12/1/2016.
 *
 * ContentProvider class for accessing, inserting, modifying, and removing data from the SQLite
 * database
 */

public class MovieProvider extends ContentProvider {
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();
    // Constants
    private MovieDbHelper mOpenHelper;
    static final int MOVIE = 100;
    static final int MOVIE_WITH_ID = 101;
    static final int MOVIE_AND_GENRES = 200;
    static final int MOVIES_AND_GENRES_QUERY = 201;
//    static final int LINK_MOVIE_WITH_GENRES = 201;
//    static final int LINK_GENRES_WITH_MOVIE = 202;
    static final int GENRE = 300;
    static final int GENRE_WITH_ID = 301;


    // URI Matcher used by this content provider
    UriMatcher sUriMatcher = buildUriMatcher();

    // Query builder for querying all databases
    private static final SQLiteQueryBuilder sMoviesAndGenreQueryBuilder;

    /**
     * Matches URIs so the content provider can access the correct database and rows
     * @return UriMatcher that will match the query to the database
     */
    static UriMatcher buildUriMatcher() {
        // Constants
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // Root URI
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // URIs that need to be matched
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIE);
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIES + "/*", MOVIE_WITH_ID);
        uriMatcher.addURI(authority, MovieContract.PATH_GENRES, GENRE);
        uriMatcher.addURI(authority, MovieContract.PATH_GENRES + "/*", GENRE_WITH_ID);
        uriMatcher.addURI(authority, MovieContract.PATH_LINK, MOVIE_AND_GENRES);
        uriMatcher.addURI(authority, MovieContract.PATH_LINK + "/*", MOVIES_AND_GENRES_QUERY);
//        uriMatcher.addURI(authority, MovieContract.PATH_LINK + "/movies/*", LINK_MOVIE_WITH_GENRES);
//        uriMatcher.addURI(authority, MovieContract.PATH_LINK + "/genres/*", LINK_GENRES_WITH_MOVIE);

        return uriMatcher;
    }

    // Sets the sMoviesAndGenreQueryBuilder as an INNER JOIN between the three databases because it
    // is final and cannot be edited using a return value from another method.
    static {
        sMoviesAndGenreQueryBuilder = new SQLiteQueryBuilder();

        sMoviesAndGenreQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " INNER JOIN " +
                        LinkEntry.TABLE_NAME +
                        " ON " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry.COLUMN_MOVIE_ID +
                        " = " + LinkEntry.TABLE_NAME +
                        "." + MovieEntry.COLUMN_MOVIE_ID +
                        " INNER JOIN " + GenreEntry.TABLE_NAME +
                        " ON " + LinkEntry.TABLE_NAME +
                        "." + GenreEntry.COLUMN_GENRE_ID +
                        " = " + GenreEntry.TABLE_NAME +
                        "." + GenreEntry.COLUMN_GENRE_ID
        );
    }

    // Static constants for querying with selection
    private static final String sMoviesOfGenreSelection = GenreEntry.TABLE_NAME + "." + GenreEntry.COLUMN_GENRE_ID + " = ?";
    private static final String sGenresOfMovieSelection = MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " = ?";
    private static final String sMovieSelection = MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_MOVIE_ID + " = ?";
    private static final String sGenreSelection = GenreEntry.TABLE_NAME + "." + GenreEntry.COLUMN_GENRE_ID + " = ?";

    /**
     * Helper method for querying database using a Cursor for all the movies of a genre or vice-versa
     * @param uri Uri containing the genreId
     * @param projection columns to return
     * @param sortOrder sort order
     * @return Cursor selecting all movies of a given genre or vice-versa from the Link Table
     */
    private Cursor getMoviesAndGenres(Uri uri, String[] projection, String sortOrder) {
        // Attempt to retrieve both IDs indiscriminately. Only one should return a real ID. The
        // other should return -1 specifying whether genres or movies is being queried for.
        long genreId = LinkEntry.getGenreFromUri(uri);
        long movieId = LinkEntry.getMovieFromUri(uri);

        // Return the appropriate cursor
        if (genreId != -1) {
            // If a genreId is returned, then movies are being queried for
            String[] selectionArgs = new String[] {Long.toString(genreId)};
            String selection = sMoviesOfGenreSelection;

            return sMoviesAndGenreQueryBuilder.query(
                    mOpenHelper.getReadableDatabase(),
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
        } else if (movieId != -1) {
            // If movieId is returned, then genres are being queried
            String[] selectionArgs = new String[] {Long.toString(movieId)};
            String selection = sGenresOfMovieSelection;

            return sMoviesAndGenreQueryBuilder.query(
                    mOpenHelper.getReadableDatabase(),
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
        } else {
            throw new UnsupportedOperationException("Uri (" + uri + ") does not query genres or movies!");
        }
    }

    /**
     * Method for querying database using a Cursor for all the genres of a movie
     * @param uri Uri containing the movieId
     * @param projection columns to return
     * @param sortOrder sort order
     * @return Cursor selecting all genres of a given movie from the Link Table
     */
    private Cursor getGenresOfMovie(Uri uri, String[] projection, String sortOrder) {
        long movieId = LinkEntry.getMovieFromUri(uri);

        String[] selectionArgs = new String[] {Long.toString(movieId)};
        String selection = sGenresOfMovieSelection;

        return sMoviesAndGenreQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /**
     * Helper method for querying the movies table with a uri containing a movieId
     * @param uri URI containing movieId after the correct query parameter
     * @param projection columns to return
     * @param sortOrder order by which the rows should be sorted (only returning a single row, so
     *                  this variable can probably be removed)
     * @return Cursor querying for a single row WHERE movieId = ? {movieId}
     */
    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        long movieId = MovieEntry.getMovieIdFromUri(uri);

        String selection = sMovieSelection;
        String[] selectionArgs = new String[] {Long.toString(movieId)};

        return mOpenHelper.getReadableDatabase().query(
                MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /**
     * Helper method for querying the genre table with a uri containing a genreId
     * @param uri URI containing genreId after the correct query parameter
     * @param projection columns to return
     * @param sortOrder order by which the rows should be sorted (only returning a single row, so
     *                  this variable can probably be removed)
     * @return Cursor querying for a single row WHERE genreId = ? {genreId}
     */
    private Cursor getGenreById(Uri uri, String[] projection, String sortOrder) {
        long genreId = GenreEntry.getGenreIdFromUri(uri);

        String selection = sGenreSelection;
        String[] selectionArgs = new String[] {Long.toString(genreId)};

        return mOpenHelper.getReadableDatabase().query(
                GenreEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        // Initialize the MovieDbHelper
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    /**
     * Method that decides whether that ContentProvider should return a single row or a group of
     * rows
     * @param uri Uri that is being queried
     * @return single or multiple rows from the database
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        String type = "";

        switch (match) {
            case MOVIE:
                return MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case GENRE:
                return GenreEntry.CONTENT_TYPE;
            case GENRE_WITH_ID:
                return GenreEntry.CONTENT_ITEM_TYPE;
//            case LINK_GENRES_WITH_MOVIE:
//                return LinkEntry.CONTENT_TYPE;
//            case LINK_MOVIE_WITH_GENRES:
//                return LinkEntry.CONTENT_TYPE;
            case MOVIE_AND_GENRES:
                return LinkEntry.CONTENT_TYPE;
            case MOVIES_AND_GENRES_QUERY:
                return LinkEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

    }

    /**
     * Method to query the database
     * @param uri URI containing the item(s) being queried
     * @param projection columns the cursor should select
     * @param selection columns to filter by
     * @param selectionArgs how to filter
     * @param sortOrder sort order
     * @return Cursor selecting all rows that match the query
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIE_WITH_ID: {
                cursor = getMovieById(uri, projection, sortOrder);
                break;
            }
            case GENRE: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        GenreEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case GENRE_WITH_ID: {
                cursor = getGenreById(uri, projection, sortOrder);
                break;
            }
            case MOVIE_AND_GENRES: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        LinkEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIES_AND_GENRES_QUERY: {
                cursor = getMoviesAndGenres(uri, projection, sortOrder);
                break;
            }
//            case LINK_GENRES_WITH_MOVIE: {
//                cursor = getGenresOfMovie(uri, projection, sortOrder);
//                break;
//            }
//            case LINK_MOVIE_WITH_GENRES: {
//                cursor = getMoviesByGenre(uri, projection, sortOrder);
//                break;
//            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        // Notifies content observers/descendants that the information in the database has changed.
        // Ensures references to the information is updated accordingly. Not necessary for a query
        // since the information is only being read.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Method for inserting values into the database
     * @param uri target for where the rows should be inserted
     * @param values data to be inserted
     * @return URI of the row that has been added
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;      // Variable that will hold the URI of the row inserted

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = MovieEntry.buildMovieUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case GENRE: {
                long _id = db.insert(GenreEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = GenreEntry.buildGenreUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case MOVIE_AND_GENRES: {
                long _id = db.insert(LinkEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = LinkEntry.buildUriLinkUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        // Notifies content observers/descendants that the information in the database has changed.
        // Ensures references to the information is updated accordingly.
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    /**
     * Method for deleting entires from the database
     * @param uri targets which database and rows to delete
     * @param selection columns to filter by
     * @param selectionArgs how to filter columns
     * @return number of rows deleted
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rows = 0;       // Variable to hold the number of rows that have been deleted

        switch (match) {
            case MOVIE: {
                rows = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case GENRE: {
                rows = db.delete(GenreEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case MOVIE_AND_GENRES: {
                rows = db.delete(LinkEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        // Notifies content observers/descendants that the information in the database has changed.
        // Ensures references to the information is updated accordingly.
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    /**
     * Method for changing the values of rows already in the database
     * @param uri targets which rows to be updated
     * @param values values with which to replace the rows
     * @param selection columns to filter by
     * @param selectionArgs how to filter
     * @return number of rows updated
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rows = 0;       // Variable to hold the number of rows updated

        switch (match) {
            case MOVIE: {
                rows = db.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case GENRE: {
                rows = db.update(GenreEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case MOVIE_AND_GENRES: {
                rows = db.update(LinkEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        // Notifies content observers/descendants that the information in the database has changed.
        // Ensures references to the information is updated accordingly.
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    /**
     * Method for inserting multiple rows of data without the costly I/O operations of opening the
     * database for each insertion
     * @param uri targets the database to insert rows
     * @param values array of data to be inserted
     * @return number of rows inserted
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rows = 0;       // Variable to hold the number of rows that have been bulk inserted

        switch (match) {
            case MOVIE: {
                // Prepare the database for bulk inserts
                db.beginTransaction();
                try {
                    //  Insert each row individually utilizing the ContentValues provided
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieEntry.TABLE_NAME, null, value);
                        if (_id > 0) {
                            // Increase the number of rows inserted if insert is successful
                            rows++;
                        }
                    }
                    // Ends the instructions for the insertion
                    db.setTransactionSuccessful();
                } finally {
                    // Instruct database to write all values at once
                    db.endTransaction();
                }
                // Notifies content observers/descendants that the information in the database has changed.
                // Ensures references to the information is updated accordingly.
                getContext().getContentResolver().notifyChange(uri, null);
                return rows;
            }
            case GENRE: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(GenreEntry.TABLE_NAME, null, value);
                        if (_id > 0) {
                            rows++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                // Notifies content observers/descendants that the information in the database has changed.
                // Ensures references to the information is updated accordingly.
                getContext().getContentResolver().notifyChange(uri, null);
                return rows;
            }
            case MOVIE_AND_GENRES: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(LinkEntry.TABLE_NAME, null, value);
                        if (_id > 0) {
                            rows++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                // Notifies content observers/descendants that the information in the database has changed.
                // Ensures references to the information is updated accordingly.
                getContext().getContentResolver().notifyChange(uri, null);
                return rows;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
