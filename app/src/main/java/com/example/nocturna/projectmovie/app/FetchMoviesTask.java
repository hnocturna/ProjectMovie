package com.example.nocturna.projectmovie.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

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

/**
 * Created by hnoct on 12/7/2016.
 */
class FetchMoviesTask extends AsyncTask<Void, Void, Movie[]> {
    Context mContext;
    MoviePosterAdapter mMovieAdapter;

    public void FetchMoviesTask(Context context, MoviePosterAdapter moviePosterAdapter) {
        mContext = context;
        mMovieAdapter = moviePosterAdapter;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

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
        final String TMD_RATING = "vote_average";
        final String TMD_RELEASE_DATE = "release_date";
        final String TMD_BACKDROP_PATH = "backdrop_path";

        final String TMD_POSTER_BASE = "http://image.tmdb.org/t/p/w342";
        final String TMD_BACKDROP_BASE = "https://image.tmdb.org/t/p/w780";

        JSONObject movieJson = new JSONObject(jsonString);
        JSONArray movieJsonArray = movieJson.getJSONArray(TMD_MOVIE_RESULTS);

        Movie[] movieArray = new Movie[movieJsonArray.length()];

        for (int i = 0; i < movieJsonArray.length(); i++) {
            // Create a new Movie object and populate it with details from the JSON string
            // and add it to an array of Movies to pass back to the doInBackground
            JSONObject movieJsonObject = movieJsonArray.getJSONObject(i);
            String movieId = movieJsonObject.getString(TMD_MOVIE_ID);
            String title = movieJsonObject.getString(TMD_TITLE);
            String posterPath = movieJsonObject.getString(TMD_POSTER_PATH);
            String overview = movieJsonObject.getString(TMD_OVERVIEW);
            String rating = movieJsonObject.getString(TMD_RATING);
            String releaseDate = movieJsonObject.getString(TMD_RELEASE_DATE);
            String backdropPath = movieJsonObject.getString(TMD_BACKDROP_PATH);

            // Prepend the base URL for the poster and the backdrop before adding it to the
            // Movie object
            posterPath = TMD_POSTER_BASE + posterPath;
            backdropPath = TMD_BACKDROP_BASE + backdropPath;

            movieArray[i] = new Movie(title, overview, releaseDate, rating, posterPath, backdropPath);
        }

        return movieArray;
    }

    @Override
    protected Movie[] doInBackground(Void... params) {
        if (params == null) {
            return null;
        }

        String movieJsonStr;     // Holds the JSON string returned from the connection.

        // Variables that need to be defined outside of the try block so it can be closed in the
        // finally block
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;

        try {
            // Build the URL using a base Uri and appending on additional parameters
            final String baseUri = "http://api.themoviedb.org/3/movie";
            final String API_PARAM = "api_key";
            final String API_KEY = BuildConfig.API_KEY;

            // Get the user-preferred sort mode from preferences
            String sortMode = PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getString(
                            mContext.getString(R.string.pref_sort_key),
                            mContext.getString(R.string.pref_sort_popular)
                    );

            Log.v(LOG_TAG, "Downloading movies. Sorting by: " + sortMode);

            Uri builtUri = Uri.parse(baseUri).buildUpon()
                    .appendPath(sortMode)
                    .appendQueryParameter(API_PARAM, API_KEY)
                    .build();

            // Convert the Uri to a URL and open a connection
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Log.v(LOG_TAG, "Built URL: " + url.toString());

            // Get the input stream from the website and read the contents, ensuring that
            // it didn't return a blank stream.
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                // Empty stream, so nothing to do
                return null;
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
                return null;
            }
            movieJsonStr = buffer.toString();

            // Store the retrieved movies as an array so the images can also be downloaded in
            // the background. This prevents issues from Picasso trying to load images slower
            // than they disappear from view as you scroll.
            Movie[] movieArrayFromJson = getMovieDataFromString(movieJsonStr);

            for (Movie movie : movieArrayFromJson) {
                // Defined outside of try block so it can be closed in the finally block
                HttpURLConnection posterConnection = null;

                try {
                    // Open a connection to the poster image.
                    URL posterUrl = new URL(movie.getPosterPath());
                    posterConnection = (HttpURLConnection) posterUrl.openConnection();
                    posterConnection.setDoInput(true);
                    posterConnection.connect();

                    // Convert to an input stream and utilize BitmapFactor to output a bitmap
                    InputStream bitmapStream = posterConnection.getInputStream();
                    Bitmap poster = BitmapFactory.decodeStream(bitmapStream);

                    movie.addPoster(poster);
                } finally {
                    if (posterConnection != null) {
                        posterConnection.disconnect();
                    }
                }
            }
            return movieArrayFromJson;

            // Log.v(LOG_TAG, movieJsonStr);
            // Log.v(LOG_TAG, "Test");

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

        return null;
    }

    @Override
    protected void onPostExecute(Movie[] moviesArray) {
        // Add the movies to the MoviePosterAdapter attached to the GridView
        if (moviesArray == null) {
            Log.d(LOG_TAG, "No movies extracted from JSON");
            return;
        }

        for (Movie movie : moviesArray) {
//            mMovieAdapter.add(movie);
        }
        return;
    }
}
