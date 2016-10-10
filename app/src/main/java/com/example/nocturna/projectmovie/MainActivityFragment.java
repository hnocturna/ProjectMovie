package com.example.nocturna.projectmovie;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        fetchMoviesTask.execute();

        GridView gridView = (GridView) getActivity().findViewById(R.id.movie_grid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    private class FetchMoviesTask extends AsyncTask<Void, Void, String[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private String[] getMovieDataFromString(String jsonString) throws JSONException {

            JSONObject movieJson = new JSONObject(jsonString);

        }

        @Override
        protected String[] doInBackground(Void... params) {

            String movieJsonStr;     // Holds the JSON string returned from the connection.

            // Variables that need to be defined outside of the try block so it can be closed in the
            // finally block
            BufferedReader reader = null;
            HttpURLConnection urlConnection = null;

            try {
                // Build the URL using a base Uri and appending on additional parameters
                String baseUri = "http://api.themoviedb.org/3/movie";
                final String API_PARAM = "api_key";
                final String API_KEY = "b1f582365e9ca840bbf384a03c4c37cd";
                String sortMethod = "popular";

                Uri builtUri = Uri.parse(baseUri).buildUpon()
                        .appendPath(sortMethod)
                        .appendQueryParameter(API_PARAM, API_KEY)
                        .build();

                // Convert the Uri to a URL and open a connection
                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                Log.v(LOG_TAG, "Built URL: " + url.toString());

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
                if (buffer == null) {
                    // Nothing has been read, so nothing to parse.
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log.v(LOG_TAG, movieJsonStr);
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
            }
            finally {
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


    }
}
