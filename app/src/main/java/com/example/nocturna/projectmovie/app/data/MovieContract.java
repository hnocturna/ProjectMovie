package com.example.nocturna.projectmovie.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by hnoct on 12/1/2016.
 *
 * Class used to store all the constants that will be used to access and query the database.
 * Accessible from any class or activity.
 */

public class MovieContract {
    // Set up static constants that will be used for navigating the database
    public static final String CONTENT_AUTHORITY = "com.example.hnocturna.projectmovie.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Paths to each database
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_GENRES = "genres";
    public static final String PATH_LINK = "link_table";

    public static final class MovieEntry implements BaseColumns {
        // URI for movie data
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        // For selecting multiple movies (filter by genre, etc.)
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        // For selecting a single movie (DetailsActivity)
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        // Setting up the naming scheme of the movies SQLiteDB
        public static final String TABLE_NAME = "movies";

        // Columns
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RATING = "vote_average";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_BACKDROP = "backdrop";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_TRAILER = "trailer";

        /**
         * Builds URI pointing to the database row given the row id
         * @param id database row id
         * @return URI for the row in the database
         */
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Builds URI pointing to a specific movie that a user has selected
         * @param movieId id of the movie selected
         * @return URI for row in the database
         */
        public static Uri buildMovieUriFromId(long movieId) {
            Uri uri = CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(movieId))
                    .build();
            return uri;
        }

        /**
         * Method for retrieving the movieId from the URI
         * @param uri URI containing movieId
         * @return movieId
         */
        public static Long getMovieIdFromUri(Uri uri) {
            // Movie Id is always the second segment after the "movie table location" segment
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    public static final class GenreEntry implements BaseColumns {
        // URI for genre data
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GENRES).build();

        // For selecting multiple genres (when inserting rows into the database)
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GENRES;

        // For selecting a single genre to filter by
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GENRES;

        // Naming scheme for the Genres SQLiteDB
        public static final String TABLE_NAME = "genres";

        // Columns
        public static final String COLUMN_GENRE_ID = "genre_id";
        public static final String COLUMN_GENRE = "genre";

        /**
         * Builds URI pointing to the database row given the row ID
         * @param row database row id
         * @return URI of the row in the database
         */
        public static Uri buildGenreUri(long row) {
            return ContentUris.withAppendedId(CONTENT_URI, row);
        }

        /**
         * I don't think this is needed. Probably overkill.
         * TODO: Complete if required.
         * @param genreId
         * @return
         */
        public static Uri buildGenreUriFromId(long genreId) {
            Uri uri = CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(genreId))
                    .build();

            return uri;
        }



        /**
         * Method for retrieving the genre from the URI
         * @param uri URI containing a query parameter for a genre
         * @return genreId being queried
         */
        public static long getGenreFromUri(Uri uri) {
            String genreString = uri.getQueryParameter(COLUMN_GENRE_ID);
            if (genreString != null && genreString.length() > 0) {
                return Long.parseLong(genreString);
            } else {
                return 0;
            }
        }
    }

    public static final class LinkEntry implements BaseColumns {
        // URI for the link table relating the movies to their genres
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LINK).build();

        // For selecting multiple rows in the case of a query
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LINK;

        // For inserting rows since they need to be inserted one at a time
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LINK;

        // Naming scheme for the link table SQLiteDB. Columns are not required because all columns
        // will be foreign keys
        public static final String TABLE_NAME = "link_table";


        /**
         * Builds Builds URI pointing to the database row given the row ID
         * @param row database row id
         * @return URI of the row in the database
         */
        public static Uri buildUriLinkUri(long row) {
            return ContentUris.withAppendedId(CONTENT_URI, row);
        }

        /**
         * Builds a unique URI that the ContentProvider can match by appending "genres" to the URI
         * to differentiate when querying a genre for all matching movies. Allows for filtering of
         * database by genre
         * @param genreId genre ID
         * @return URI querying all rows for movies for a specific genre
         */
        public static Uri buildMovieGenreUri(long genreId) {
            Uri uri = CONTENT_URI.buildUpon()
                    .appendPath("genres")
                    // Query parameter returns multiple entries
                    .appendQueryParameter(GenreEntry.COLUMN_GENRE_ID, Long.toString(genreId))
                    .build();

            return uri;
        }

        /**
         * Builds a unique URI that the ContentProvider can match by appending "movies" to the URI
         * to differentiate when querying a movie for all its genres.
         * @param movieId movieId
         * @return URI querying for all rows for a specific movie
         */
        public static Uri buildGenresUriFromMovieId(long movieId) {
            Uri uri = CONTENT_URI.buildUpon()
                    .appendPath("movies")
                    .appendQueryParameter(MovieEntry.COLUMN_MOVIE_ID, Long.toString(movieId))
                    .build();

            return uri;
        }
    }
}
