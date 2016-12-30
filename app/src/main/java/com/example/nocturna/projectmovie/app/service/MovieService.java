package com.example.nocturna.projectmovie.app.service;

import android.app.FragmentManager;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telecom.Call;
import android.util.Log;

import com.example.nocturna.projectmovie.app.BuildConfig;
import com.example.nocturna.projectmovie.app.MainActivityFragment;
import com.example.nocturna.projectmovie.app.Movie;
import com.example.nocturna.projectmovie.app.R;
import com.example.nocturna.projectmovie.app.Utility;
import com.example.nocturna.projectmovie.app.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hnoct on 12/29/2016.
 */

public class MovieService extends IntentService {
    // Constants
    private static final String LOG_TAG = MovieService.class.getSimpleName();
    final String BASE_URI = "http://api.themoviedb.org/3/movie";
    final String API_PARAM = "api_key";
    final String API_KEY = BuildConfig.API_KEY;

    /**
     * Default constructor
     */
    public MovieService() {
        super("MovieService");
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, MovieService.class);
            context.startService(sendIntent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String movieJsonStr;     // Holds the JSON string returned from the connection.

        // Variables that need to be defined outside of the try block so it can be closed in the
        // finally block
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;

        try {
            // Get the user-preferred sort mode from preferences
            String sortMode = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(
                            getString(R.string.pref_sort_key),
                            getString(R.string.pref_sort_popular)
                    );


            // Build the URL using a base Uri and appending on additional parameters
            Uri builtUri = Uri.parse(BASE_URI).buildUpon()
                    .appendPath(sortMode)
                    .appendQueryParameter(API_PARAM, API_KEY)
                    .build();

            // Convert the Uri to a URL and open a connection
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Get the input stream from the website and read the contents, ensuring that
            // it didn't return a blank stream.
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                // Empty stream, so nothing to do
                return;
            }

            // Read the lines from the stream
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // Append a new line to each line read to make it easier for humans to read
            // for debugging purposes utilizing the StringBuffer to append.
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            // Convert the buffer to a String that can be converted to a JSON Object
            if (buffer.length() == 0) {
                // Nothing has been read, so nothing to parse.
                return;
            }
            movieJsonStr = buffer.toString();

            // Store the retrieved movies as an array. Images will be downloaded in the CursorAdapter
            // on-the-fly. Hopefully only once as to prevent the constant loading of images as the
            // screen is scrolled.
            Movie[] movieArrayFromJson = getMovieDataFromString(movieJsonStr);

            // Add data to the Link Table
            int rows = addMoviesAndGenres(movieArrayFromJson);

            // Add trailer paths to each movie object and then insert the data
            rows = addMovies(movieArrayFromJson);

            // Get genre data and insert the data if it hasn't been added before
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean getGenre = !prefs.getBoolean(getString(R.string.genres_retrieved), false);

            if (getGenre) {
                rows = getGenreData();
                if (rows > 0) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(getString(R.string.genres_retrieved), true);
                    editor.commit();
                }
            }
        } catch (MalformedURLException e) {
            // In case the URL is incorrect
            Log.d(LOG_TAG, "Malformed URL", e);
        } catch (IOException e) {
            // If unable to connect to the website (e.g. no network connection)
            Log.d(LOG_TAG, "Unable to connect to TheMovieDB.org", e);
        } catch (Exception e) {
            // In case of any errors that were missed
            Log.d(LOG_TAG, "Exception: ", e);
        } finally {
            // Close opened resources
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Unable to close stream", e);
                }
            }
        }
        return;
    }

    /*
     * Method of extracting the data required from the JSON String returned by the
     * FetchMoviesTask and returning it as a list of string of URLs to get the movie posters
     * from
     */
    private Movie[] getMovieDataFromString(String jsonString) throws JSONException {
        // List of items that need to be extracted
        final String TMD_MOVIE_RESULTS = "results";
        final String TMD_MOVIE_ID = "id";
        final String TMD_POSTER_PATH = "poster_path";
        final String TMD_TITLE = "original_title";
        final String TMD_OVERVIEW = "overview";
        final String TMD_GENRES = "genre_ids";
        final String TMD_RATING = "vote_average";
        final String TMD_POPULARITY = "popularity";
        final String TMD_RELEASE_DATE = "release_date";
        final String TMD_BACKDROP_PATH = "backdrop_path";

        final String TMD_POSTER_BASE = "http://image.tmdb.org/t/p/w342";
        final String TMD_BACKDROP_BASE = "https://image.tmdb.org/t/p/w780";

        JSONObject movieJson = new JSONObject(jsonString);
        JSONArray movieJsonArray = movieJson.getJSONArray(TMD_MOVIE_RESULTS);

        // To hold all movie objects that will need to be added to database
        Movie[] movieArray = new Movie[movieJsonArray.length()];

        // Create a new Movie object and populate it with details from the JSON string
        // and add it to an array of ContentValues for bulk insertion into database
        for (int i = 0; i < movieJsonArray.length(); i++) {
            // Set data variables to data retrieved from TMD
            JSONObject movieJsonObject = movieJsonArray.getJSONObject(i);
            long movieId = movieJsonObject.getLong(TMD_MOVIE_ID);
            String title = movieJsonObject.getString(TMD_TITLE);
            String posterPath = movieJsonObject.getString(TMD_POSTER_PATH);
            String overview = movieJsonObject.getString(TMD_OVERVIEW);
            double rating = movieJsonObject.getDouble(TMD_RATING);
            double popularity = movieJsonObject.getDouble(TMD_POPULARITY);
            String releaseDateStr = movieJsonObject.getString(TMD_RELEASE_DATE);
            String backdropPath = movieJsonObject.getString(TMD_BACKDROP_PATH);

            // Check if movie already exists in database
            Cursor cursor = getContentResolver().query(
                    MovieContract.MovieEntry.buildMovieUriFromId(movieId),
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                // If cursor returns a row, then movie already exists in database and only the
                // rating and popularity need to be updated in the database
                double dbPopularity = cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POPULARITY));
                double dbRating = cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING));

                if (dbPopularity != popularity || dbRating != rating) {
                    // If either value does not match the value in the database, then the database
                    // needs to be updated. The cost of writing two values is practically the same
                    // as writing one value, so there is only one check.
                    ContentValues values = new ContentValues();
                    values.put(MovieContract.MovieEntry.COLUMN_POPULARITY, popularity);
                    values.put(MovieContract.MovieEntry.COLUMN_RATING, rating);

                    getContentResolver().update(
                            MovieContract.MovieEntry.CONTENT_URI,
                            values,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[] {Long.toString(movieId)}
                    );
                }

                cursor.close();
                continue;
            }
            cursor.close();

            // Convert the date format to long so that it can be stored in the database
            long releaseDate = Utility.dateToLong(releaseDateStr);

            // Prepend the base URL for the poster and the backdrop before adding it to the
            // ContentValues
            posterPath = TMD_POSTER_BASE + posterPath;
            backdropPath = TMD_BACKDROP_BASE + backdropPath;

            // Get genreIds as an array
            JSONArray movieGenreJsonArray = movieJsonObject.getJSONArray(TMD_GENRES);
            int[] genreIds = new int[movieGenreJsonArray.length()];
            for (int j = 0; j < genreIds.length; j++) {
                genreIds[j] = movieGenreJsonArray.getInt(j);
            }

            movieArray[i] = new Movie(movieId, title, overview, releaseDate, rating, popularity, posterPath, backdropPath, genreIds);
        }
        movieArray = Utility.cleanMovieArray(movieArray);
        return movieArray;
    }



    /**
     * Bulk insert all movies and their corresponding genres into the Link Table. Must be done first
     * as the other tables include Foreign Keys in the Link Table
     * @param movieArray
     */
    private int addMoviesAndGenres(Movie[] movieArray) {
        // Array to hold all content values to be bulk-inserted
        ContentValues[] contentValuesArrays = new ContentValues[movieArray.length];
        List<ContentValues> contentValuesList = new ArrayList<ContentValues>();

        // For each movie, each genre needs to be added as a separate row in the Link Table
        for (int i = 0; i < movieArray.length; i++) {
            // Set up variables
            Movie movie = movieArray[i];
            if (movie == null) {
                continue;
            }
            long movieId = movie.getId();
            int[] genreIds = movie.getGenreIds();

            // Create content values for each movie-genre pair
            for (int genreId : genreIds) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
                contentValues.put(MovieContract.GenreEntry.COLUMN_GENRE_ID, genreId);

                // Add content values to array
                contentValuesList.add(contentValues);
                contentValuesArrays[i] = contentValues;
            }
        }
        contentValuesList.toArray((contentValuesArrays = new ContentValues[contentValuesList.size()]));
        // Bulk insert array of ContentValues
        int rows = getContentResolver().bulkInsert(MovieContract.LinkEntry.CONTENT_URI, contentValuesArrays);
        return rows;
    }

    /**
     * Bulk insert all movies downloaded into Movies Table
     * @param movieArray array holding movies to be inserted
     * @return number of rows inserted into database
     */
    private int addMovies(Movie[] movieArray) {
        // Holds ContentValues to be bulk-inserted into database
        ContentValues[] contentValuesArray = new ContentValues[movieArray.length];

        for (int i = 0; i < movieArray.length; i++) {
            Movie movie = movieArray[i];

            // Retrieve values from Movie object
            long id = movie.getId();
            String title = movie.getTitle();
            String overview = movie.getOverview();
            long releaseDate = movie.getReleaseDate();
            String backdropPath = movie.getBackdropPath();
            String posterPath = movie.getPosterPath();
            // String trailerPath = movie.getTrailerPath();
            double popularity = movie.getPopularity();
            double userRating = movie.getUserRating();

            // Add values to ContentValues to be inserted
            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, id);
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
            movieValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP, backdropPath);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER, posterPath);
            // movieValues.put(MovieEntry.COLUMN_TRAILER, trailerPath);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, popularity);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, userRating);

            contentValuesArray[i] = movieValues;
        }
        // Bulk insert all values into database
        int rows = getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, contentValuesArray);
        return rows;
    }

    private int getGenreData() throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        final String GENRE_BASE_URI = "https://api.themoviedb.org/3/genre";
        final String MOVIE_PATH = "movie";
        final String LIST_PATH = "list";

        Uri genresUri = Uri.parse(GENRE_BASE_URI).buildUpon()
                .appendPath(MOVIE_PATH)
                .appendPath(LIST_PATH)
                .appendQueryParameter(API_PARAM, API_KEY)
                .build();

        URL url = new URL(genresUri.toString());

        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();

        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while ((line = reader.readLine()) != null) {
            buffer.append(line + "\n");
        }

        if (buffer.length() == 0) {
            // No data read so unable to continue
            return 0;
        }

        String genreJsonString = buffer.toString();
        int rows = addGenresFromJsonString(genreJsonString);
        return rows;
    }


    /**
     * Extracts all genre IDs and their names from the JSON data and the bulk-inserts it into the
     * database
     * @param genreJsonString JSON String containing genre data
     * @throws JSONException if the input String is not a proper JSON Object
     * @return number of rows inserted
     */
    private int addGenresFromJsonString(String genreJsonString) throws JSONException{
        // Constants for navigating and extracting JSON data
        final String TMD_GENRES = "genres";
        final String TMD_GENRE_ID = "id";
        final String TMD_GENRE_NAME = "name";

        // Navigate JSON data down to the array
        JSONObject genreJson = new JSONObject(genreJsonString);
        JSONArray genreJsonArray = genreJson.getJSONArray(TMD_GENRES);

        // Holds all data to be bulk inserted into database
        ContentValues[] contentValuesArray = new ContentValues[genreJsonArray.length()];

        // Each object in the JSON Array is a genreId-name pair. Extract the data and create a CV
        // to hold the data
        for (int i = 0; i < genreJsonArray.length(); i++) {
            JSONObject genreJsonObject = genreJsonArray.getJSONObject(i);
            int genreId = genreJsonObject.getInt(TMD_GENRE_ID);
            String genreName = genreJsonObject.getString(TMD_GENRE_NAME);

            ContentValues genreValues = new ContentValues();
            genreValues.put(MovieContract.GenreEntry.COLUMN_GENRE_ID, genreId);
            genreValues.put(MovieContract.GenreEntry.COLUMN_GENRE, genreName);

            contentValuesArray[i] = genreValues;
        }
        // Bulk-insert all genre values into Genre Table
        int rows = getContentResolver().bulkInsert(MovieContract.GenreEntry.CONTENT_URI, contentValuesArray);
        return rows;
    }
}
