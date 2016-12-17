package com.example.nocturna.projectmovie.app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.nocturna.projectmovie.app.data.MovieContract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // Member variables
    Uri mMovieUri;

    // Constants
    String LOG_TAG = DetailsActivityFragment.class.getSimpleName();
    private static final int DETAILS_LOADER = 1;
    private static final String MOVIE_KEY = "mMovieUri";

    // Column Projection
    private static final String[] DETAILS_COLUMNS = new String[] {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_POSTER,
            MovieContract.MovieEntry.COLUMN_BACKDROP
            // MovieContract.MovieEntry.COLUMN_TRAILER,
    };

    // Column indices. Tied to DETAILS_COLUMNS
    private static final int COL_MOVIE_ID = 0;
    private static final int COL_TITLE = 1;
    private static final int COL_OVERVIEW = 2;
    private static final int COL_RATING = 3;
    private static final int COL_RELEASE_DATE = 4;
    private static final int COL_POSTER = 5;
    private static final int COL_BACKDROP = 6;
    // private static final int COL_TRAILER = 8;

    TextView titleText;
    TextView overviewText;
    TextView ratingText;
    TextView releaseText;

    ImageView posterImage;
    ImageView backdropImage;

    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        Intent intent = getActivity().getIntent();
        mMovieUri = intent.getData();

        titleText = (TextView) rootView.findViewById(R.id.detail_title_text);
        overviewText = (TextView) rootView.findViewById(R.id.detail_overview_text);
        ratingText = (TextView) rootView.findViewById(R.id.detail_rating_text);
        releaseText = (TextView) rootView.findViewById(R.id.detail_release_text);
        posterImage = (ImageView) rootView.findViewById(R.id.detail_poster_image);
        backdropImage = (ImageView) rootView.findViewById(R.id.detail_backdrop_image);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mMovieUri == null) {
            return null;
        }

        return new CursorLoader(
                getActivity(),
                mMovieUri,
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
            return;
        }

        // Movie variables to be loaded into the view
        long movieId = cursor.getLong(COL_MOVIE_ID);
        String title = cursor.getString(COL_TITLE);
        String overview = cursor.getString(COL_OVERVIEW);
        double rating = cursor.getDouble(COL_RATING);
        String releaseDate = cursor.getString(COL_RELEASE_DATE);
        String posterPath = cursor.getString(COL_POSTER);
        String backdropPath = cursor.getString(COL_BACKDROP);
        // String trailerPath = cursor.getString(COL_TRAILER);

        String[] params = new String[] {posterPath, backdropPath};
        FetchImageTask fetchImageTask = new FetchImageTask();
        fetchImageTask.execute(params);

        titleText.setText(title);
        overviewText.setText(overview);
        ratingText.setText(Double.toString(rating));
        releaseText.setText(releaseDate);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class FetchImageTask extends AsyncTask<String, Void, Bitmap[]> {
        final String LOG_TAG = FetchImageTask.class.getSimpleName();

        @Override
        protected Bitmap[] doInBackground(String... params) {
            Log.v(LOG_TAG, "FetchImageTask doInBackground");
            if (params == null) {
                // If no URLs are passed, there is nothing to download
                return null;
            }

            // Retrieve the URLs passed into the task
            String posterStr = params[0];
            String backdropStr = params[1];

            // Array to pass to onPostExecute
            Bitmap[] images = new Bitmap[2];

            // Initialize the connection that will need to be closed in the finally block
            HttpURLConnection urlConnection = null;

            try {
                // Download the poster as a bitmap and add it to the Movie object passed to the task
                URL posterUrl = new URL(posterStr);
                urlConnection = (HttpURLConnection) posterUrl.openConnection();
                // urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                Log.v(LOG_TAG, "Poster URL: " + posterUrl.toString());
                InputStream bitmapStream = urlConnection.getInputStream();
                Bitmap poster = BitmapFactory.decodeStream(bitmapStream);
                images[0] = poster;

            } catch (MalformedURLException e) {
                Log.d(LOG_TAG, "Incorrect poster URL formatting", e);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error downloading poster", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            try {
                // Download the backdrop as above
                URL backdropUrl = new URL(backdropStr);
                urlConnection = (HttpURLConnection) backdropUrl.openConnection();
                // urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                Log.v(LOG_TAG, "Backdrop URL: " + backdropUrl.toString());

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
            if (images[0] != null) {
                posterImage.setImageBitmap(images[0]);
            }

            if (images[1] != null) {
                backdropImage.setAlpha(175);
                backdropImage.setImageBitmap(images[1]);
                // ScrollView scrollView = (ScrollView) getView().findViewById(R.id.detail_scroll_view);
                // scrollView.scrollTo(0, titleText.getTop());
            }
        }
    }
}
