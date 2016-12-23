package com.example.nocturna.projectmovie.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.TimeFormatException;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

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
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // Member variables
    Uri mMovieUri;
    Context mContext;
    long mMovieId;

    // Constants
    String LOG_TAG = DetailsActivityFragment.class.getSimpleName();
    final String API_KEY = BuildConfig.API_KEY;
    final String API_PARAM = "api_key";
    final String BASE_URI = "http://api.themoviedb.org/3/movie";
    private static final int DETAILS_LOADER = 1;

    // Column Projection
    private static final String[] DETAILS_COLUMNS = new String[] {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_POSTER,
            MovieContract.MovieEntry.COLUMN_BACKDROP,
            MovieContract.GenreEntry.TABLE_NAME + "." + MovieContract.GenreEntry.COLUMN_GENRE_ID,
            MovieContract.GenreEntry.COLUMN_GENRE,
            MovieContract.MovieEntry.COLUMN_TRAILER,
            MovieContract.MovieEntry.COLUMN_FAVORITE
    };

    // Column indices. Tied to DETAILS_COLUMNS
    private static final int COL_MOVIE_ID = 0;
    private static final int COL_TITLE = 1;
    private static final int COL_OVERVIEW = 2;
    private static final int COL_RATING = 3;
    private static final int COL_RELEASE_DATE = 4;
    private static final int COL_POSTER = 5;
    private static final int COL_BACKDROP = 6;
    private static final int COL_GENRE_ID = 7;
    private static final int COL_GENRE = 8;
    private static final int COL_TRAILER = 9;
    private static final int COL_FAVORITE = 10;

    TextView titleText;
    TextView overviewText;
    TextView ratingText;
    TextView releaseText;
    TextView genreText;
    TextView trailerText;

    ImageView posterImage;
    ImageView backdropImage;
    ImageView trailerImage;
    ImageView favoriteIcon;

    LinearLayout backgroundLayout;

    ListView reviewListView;

    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        // Retrieve URI from Intent
        Intent intent = getActivity().getIntent();

        // Initialize member variables
        mMovieUri = intent.getData();
        mMovieId = MovieContract.MovieEntry.getMovieIdFromUri(mMovieUri);
        mContext = getActivity();

        // Initialize views
        titleText = (TextView) rootView.findViewById(R.id.detail_title_text);
        overviewText = (TextView) rootView.findViewById(R.id.detail_overview_text);
        ratingText = (TextView) rootView.findViewById(R.id.detail_rating_text);
        releaseText = (TextView) rootView.findViewById(R.id.detail_release_text);
        genreText = (TextView) rootView.findViewById(R.id.detail_genre_text);
        trailerText = (TextView) rootView.findViewById(R.id.detail_trailer_text);

        backdropImage = (ImageView) rootView.findViewById(R.id.detail_backdrop_image);
        trailerImage = (ImageView) rootView.findViewById(R.id.detail_trailer_thumbnail);

        favoriteIcon = (ImageView) rootView.findViewById(R.id.detail_favorite_button);
        favoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set onClickListener to update the favorite status in the database and change
                // the drawable for the icon
                Uri movieUri = MovieContract.LinkEntry.buildGenresUriFromMovieId(mMovieId);

                // Retrieve current favorite status of the movie
                Cursor cursor = mContext.getContentResolver().query(
                        movieUri,
                        DETAILS_COLUMNS,
                        null,
                        null,
                        null
                );
                cursor.moveToFirst();

                // Update the database with the new favorite value and change the drawable resource of the icon
                if (cursor.getInt(COL_FAVORITE) == 0) {
                    ContentValues values = new ContentValues();
                    values.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 1);

                    mContext.getContentResolver().update(
                            MovieContract.MovieEntry.CONTENT_URI,
                            values,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{Long.toString(mMovieId)}
                    );

                    favoriteIcon.setImageDrawable(mContext.getDrawable(R.drawable.star_on));
                } else {
                    ContentValues values = new ContentValues();
                    values.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 0);

                    mContext.getContentResolver().update(
                            MovieContract.MovieEntry.CONTENT_URI,
                            values,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[] {Long.toString(mMovieId)}
                    );

                    favoriteIcon.setImageDrawable(mContext.getDrawable(R.drawable.star_off));
                }
            }
        });

        backgroundLayout = (LinearLayout) rootView.findViewById(R.id.detail_subtitle_background);
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        overviewText.setMinHeight(Math.round(148 * pixels));

        reviewListView = (ListView) rootView.findViewById(R.id.detail_review_list);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mMovieUri == null) {
            return null;
        }

        mMovieId = MovieContract.MovieEntry.getMovieIdFromUri(mMovieUri);
        Uri movieUri = MovieContract.LinkEntry.buildGenresUriFromMovieId(mMovieId);

        return new CursorLoader(
                getActivity(),
                movieUri,
                DETAILS_COLUMNS,
                null,
                null,
                null
        );

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAILS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!cursor.moveToFirst()) {
            Log.d(LOG_TAG, "Cursor returned no rows!");
            return;
        }

        // Movie variables to be loaded into the view
        long movieId = cursor.getLong(COL_MOVIE_ID);
        String title = cursor.getString(COL_TITLE);
        String overview = cursor.getString(COL_OVERVIEW);
        String rating = Utility.formatRating(cursor.getDouble(COL_RATING));
        long releaseDate = cursor.getLong(COL_RELEASE_DATE);
        String posterPath = cursor.getString(COL_POSTER);
        String backdropPath = cursor.getString(COL_BACKDROP);
        String genres = cursor.getString(COL_GENRE);
        boolean favorite;

        if (cursor.getString(COL_TRAILER) != null) {
            String trailerPath = cursor.getString(COL_TRAILER);
            FetchTrailerThumbnailTask fetchTrailerThumbnailTask = new FetchTrailerThumbnailTask();
            fetchTrailerThumbnailTask.execute(trailerPath);
        } else {
            FetchTrailerTask fetchTrailerTask = new FetchTrailerTask();
            fetchTrailerTask.execute(movieId);
        }

        if (cursor.getInt(COL_FAVORITE) == 0) {
            favoriteIcon.setImageDrawable(mContext.getDrawable(R.drawable.star_off));
        } else {
            favoriteIcon.setImageDrawable(mContext.getDrawable(R.drawable.star_on));
        }

        while (cursor.moveToNext()) {
            genres += ", " + cursor.getString(COL_GENRE);
        }


        // Convert release date into String format
        String releaseDateStr = Utility.longToDate(releaseDate);

        String[] params = new String[] {posterPath, backdropPath, title, overview, rating, releaseDateStr, genres};
        FetchImageTask fetchImageTask = new FetchImageTask();
        fetchImageTask.execute(params);


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class FetchImageTask extends AsyncTask<String, Void, Bitmap[]> {
        final String LOG_TAG = FetchImageTask.class.getSimpleName();
        String title;
        String overview;
        String rating;
        String releaseDate;
        String genres;

        @Override
        protected Bitmap[] doInBackground(String... params) {
            if (params == null) {
                // If no URLs are passed, there is nothing to download
                return null;
            }

            // Retrieve the URLs passed into the task
            String posterStr = params[0];
            String backdropStr = params[1];
            this.title = params[2];
            this.overview = params[3];
            this.rating = params[4];
            this.releaseDate = params[5];
            this.genres = params[6];

            // Array to pass to onPostExecute
            Bitmap[] images = new Bitmap[2];

            // Initialize the connection that will need to be closed in the finally block
            HttpURLConnection urlConnection = null;

//            try {
//                // Download the poster as a bitmap and add it to the Movie object passed to the task
//                URL posterUrl = new URL(posterStr);
//                urlConnection = (HttpURLConnection) posterUrl.openConnection();
//                // urlConnection.setRequestMethod("GET");
//                urlConnection.setDoInput(true);
//                urlConnection.connect();
//
//                InputStream bitmapStream = urlConnection.getInputStream();
//                Bitmap poster = BitmapFactory.decodeStream(bitmapStream);
//                images[0] = poster;
//
//            } catch (MalformedURLException e) {
//                Log.d(LOG_TAG, "Incorrect poster URL formatting", e);
//            } catch (IOException e) {
//                Log.d(LOG_TAG, "Error downloading poster", e);
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//            }

            try {
                // Download the backdrop as above
                URL backdropUrl = new URL(backdropStr);
                urlConnection = (HttpURLConnection) backdropUrl.openConnection();
                // urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();


                InputStream bitmapStream = urlConnection.getInputStream();
                Bitmap backdrop = BitmapFactory.decodeStream(bitmapStream);
                images[1] = backdrop;
            } catch (MalformedURLException e) {
                Log.d(LOG_TAG, "Incorrect backdrop URL formatting", e);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error downloading backdrop", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return images;
        }

        @Override
        protected void onPostExecute(Bitmap[] images) {
            if (images[1] != null) {
                backdropImage.setImageBitmap(images[1]);

                // Set the background of the subtitle text to the average color of backdrop
                int backgroundColor = Utility.getDominantColor(images[1]);
                backgroundLayout.setBackgroundColor(backgroundColor);

                // Set the text of the TextViews after poster has been loaded to prevent awkward
                // positioning of text while images load
                titleText.setText(title);
                overviewText.setText(overview);
                ratingText.setText(rating);
                releaseText.setText(releaseDate);
                genreText.setText(genres);
                trailerText.setText("Trailer");
                // ScrollView scrollView = (ScrollView) get
            }
        }
    }

    private class FetchTrailerTask extends AsyncTask<Long, Void, String> {
        // Constants
//        final String API_KEY = BuildConfig.API_KEY;
//        final String API_PARAM = "api_key";
//        final String BASE_URI = "http://api.themoviedb.org/3/movie";

        @Override
        protected String doInBackground(Long... params) {
            // Constants
            long movieId = params[0];
            final String VIDEOS_PATH = "videos";

            // Defined outside of try block so it can be closed in the finally block
            HttpURLConnection urlConnection = null;
            BufferedReader reader;
            String trailerJsonString = null;

            try {
                // Builds the URI to query the trailer paths
                Uri builtUri = Uri.parse(BASE_URI)
                        .buildUpon()
                        .appendPath(Long.toString(movieId))
                        .appendPath(VIDEOS_PATH)
                        .appendQueryParameter(API_PARAM, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing returned
                    return null;
                }

                String line;

                reader = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Nothing read, so unable to continue
                    return null;
                }
                trailerJsonString = buffer.toString();

            } catch (MalformedURLException e) {
                Log.d(LOG_TAG, "Incorrect backdrop URL formatting", e);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error downloading backdrop", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            try {
                getTrailerFromString(movieId, trailerJsonString);
            } catch (JSONException e) {
                Log.d(LOG_TAG, "JSON Exception", e);
                e.printStackTrace();
            }

            Cursor cursor = mContext.getContentResolver().query(
                    MovieContract.LinkEntry.buildGenresUriFromMovieId(movieId),
                    DETAILS_COLUMNS,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();
            String trailerPath = cursor.getString(COL_TRAILER);

            return trailerPath;
        }

        /**
         * Adds the trailer from the JSONString for the movie utilizing a YouTube video ID and appending
         * it to the YouTube watch videos URL, then adds the URL to the movie object
         * @param movieId movieId being queried
         * @param trailerJsonString JSON String containing the trailer ID
         * @throws JSONException when the String is not a proper JSON object
         */
        private void getTrailerFromString(long movieId, String trailerJsonString) throws JSONException {
            // Constants
            final String TRAILER = "Trailer";
            final String TMD_RESULTS = "results";
            final String TMD_TRAILER_PATH = "key";
            final String TMD_TRAILER_TYPE = "type";
            final String YT_PATH_BASE = "https://www.youtube.com/watch?v=";

            // Holds the trailer path outside of the iteration
            String trailerPath = "";

            // Convert the String to a JSON Object and retrieve the trailer from the JSON Array
            JSONObject trailerJson = new JSONObject(trailerJsonString);
            JSONArray trailerArray = trailerJson.getJSONArray(TMD_RESULTS);

            // Make sure the Trailer ID matches a trailer type video
            for (int i = 0; i < trailerArray.length(); i++) {
                if (trailerArray.getJSONObject(i).getString(TMD_TRAILER_TYPE).equals(TRAILER)) {
                    trailerPath = trailerArray.getJSONObject(i).getString(TMD_TRAILER_PATH);
                    break;
                }
            }

            // Pre-pend the Youtube video path to the trailer ID to get the full URL of the trailer
            trailerPath = YT_PATH_BASE + trailerPath;

            // Add the trailer path to the database
            ContentValues values = new ContentValues();
            values.put(MovieContract.MovieEntry.COLUMN_TRAILER, trailerPath);
            mContext.getContentResolver().update(
                    MovieContract.MovieEntry.CONTENT_URI,
                    values,
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[] {Long.toString(movieId)}
                );
        }

        @Override
        protected void onPostExecute(String trailerPath) {
            FetchTrailerThumbnailTask fetchTrailerThumbnailTask = new FetchTrailerThumbnailTask();
            fetchTrailerThumbnailTask.execute(trailerPath);
        }
    }

    private class FetchTrailerThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        String trailerPath;

        @Override
        protected Bitmap doInBackground(String... params) {
            // Variables to get trailer thumbnails from Youtube
            final String THUMBNAIL_BASE_PATH = "https://img.youtube.com/vi/";
            final String THUMBNAIL_FILE = "hqdefault.jpg";
            this.trailerPath = params[0];

            // Retrieve the videoId from the YouTube link
            Uri trailerUri = Uri.parse(trailerPath);
            String videoId = trailerUri.getQueryParameter("v");

            Uri thumbnailUri = Uri.parse(THUMBNAIL_BASE_PATH).buildUpon()
                    .appendPath(videoId)
                    .appendPath(THUMBNAIL_FILE)
                    .build();

            // Variables that need to be defined outside of the try block so it can be closed in the
            // finally block or passed to the onPostExecute
            HttpURLConnection urlConnection = null;
            Bitmap trailerThumbnailBitmap = null;

            // Retrieve the image
            try {
                // Open a connection to the URL
                URL thumbnailUrl = new URL(thumbnailUri.toString());
                urlConnection = (HttpURLConnection) thumbnailUrl.openConnection();
                urlConnection.setDoInput(true);

                // Create InputStream from the connection
                InputStream bitmapStream = urlConnection.getInputStream();

                // Convert stream to Bitmap
                trailerThumbnailBitmap = BitmapFactory.decodeStream(bitmapStream);

                // Close InputStream
                bitmapStream.close();
            } catch (MalformedURLException e) {
                Log.d(LOG_TAG, "Malformed URL", e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error connecting to thumbnail", e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return trailerThumbnailBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Set the thumbnail for the trailer
            if (bitmap != null) {
                trailerImage.setImageBitmap(bitmap);
                // Set an Intent to fire a weblink to the YouTube trailer on click
                trailerImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Implicit Intent will either fire YouTube app if available or open
                        // the URL in a web browser
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerPath));
                        startActivity(intent);
                    }
                });
            }
        }
    }

    private class FetchReviewTask extends AsyncTask<Long, Void, Void> {
        @Override
        protected Void doInBackground(Long... params) {
            // Constants
            long movieId = params[0];

            return null;
        }
    }
}
