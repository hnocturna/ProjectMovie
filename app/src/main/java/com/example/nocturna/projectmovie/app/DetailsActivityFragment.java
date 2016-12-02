package com.example.nocturna.projectmovie.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.nocturna.projectmovie.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment {
    String LOG_TAG = DetailsActivityFragment.class.getSimpleName();
    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        Intent intent = getActivity().getIntent();
        Movie movie = intent.getParcelableExtra(MainActivity.EXTRA_MOVIE);

//        String movieTitle = movie.getTitle();
//        String movieOverview = movie.getOverview();
//        String movieRating = movie.getUserRating();
//        String movieReleaseDate = movie.getReleaseDate();
//        String moviePosterPath = movie.getPosterPath();
//        String movieBackdropPath = movie.getBackdropPath();
//
//        ImageView backdropImage = (ImageView) rootView.findViewById(R.id.detail_backdrop_image);
//        ImageView posterImage = (ImageView) rootView.findViewById(R.id.poster_image);
//
//        TextView titleText = (TextView) rootView.findViewById(R.id.detail_title_text);
//        TextView overviewText = (TextView) rootView.findViewById(R.id.detail_overview_text);
//        TextView ratingText = (TextView) rootView.findViewById(R.id.detail_rating_text);
//        TextView releaseText = (TextView) rootView.findViewById(R.id.detail_release_text);

        FetchImageTask fetchImageTask = new FetchImageTask();
        fetchImageTask.execute(movie);

//        titleText.setText(movieTitle);
//        overviewText.setText(movieOverview);
//        ratingText.setText(movieRating);
//        releaseText.setText(movieReleaseDate);
//
//        ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.detail_scroll_view);
//        scrollView.smoothScrollTo(0, posterImage.getTop());
        return rootView;
    }

    private class FetchImageTask extends AsyncTask<Movie, Void, Movie> {
        final String LOG_TAG = FetchImageTask.class.getSimpleName();

        @Override
        protected Movie doInBackground(Movie... params) {
            if (params == null) {
                // If no URLs are passed, there is nothing to download
                return null;
            }

            // Retrieve the URLs passed into the task via the Movie object
            Movie movie = params[0];
            String posterStr = movie.getPosterPath();
            String backdropStr = movie.getBackdropPath();

            // Initialize the connection that will need to be closed in the finally block
            HttpURLConnection urlConnection = null;

            try {
                // Download the poster as a bitmap and add it to the Movie object passed to the task
                URL posterUrl = new URL(posterStr);
                urlConnection = (HttpURLConnection) posterUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                InputStream bitmapStream = urlConnection.getInputStream();
                Bitmap poster = BitmapFactory.decodeStream(bitmapStream);
                movie.addPoster(poster);

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
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();

                InputStream bitmapStream = urlConnection.getInputStream();
                Bitmap backdrop = BitmapFactory.decodeStream(bitmapStream);
                movie.addBackdrop(backdrop);

            } catch (MalformedURLException e) {
                Log.d(LOG_TAG, "Incorrect backdrop URL formatting", e);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error downloading backdrop", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return movie;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            if (movie == null ) {
                return;
            }
            TextView titleText = (TextView) getView().findViewById(R.id.detail_title_text);
            TextView overviewText = (TextView) getView().findViewById(R.id.detail_overview_text);
            TextView ratingText = (TextView) getView().findViewById(R.id.detail_rating_text);
            TextView releaseText = (TextView) getView().findViewById(R.id.detail_release_text);

            ImageView posterImage = (ImageView) getView().findViewById(R.id.poster_image);
            ImageView backdropImage = (ImageView) getView().findViewById(R.id.detail_backdrop_image);

            if (movie.getPoster() != null) {
                posterImage.setImageBitmap(movie.getPoster());
            }

            if (movie.getBackdrop() != null) {
                backdropImage.setAlpha(175);
                backdropImage.setImageBitmap(movie.getBackdrop());
                ScrollView scrollView = (ScrollView) getView().findViewById(R.id.detail_scroll_view);
                scrollView.scrollTo(0, titleText.getTop());
            }

            String movieTitle = movie.getTitle();
            String movieOverview = movie.getOverview();
            String movieRating = movie.getUserRating();
            String movieReleaseDate = movie.getReleaseDate();

            titleText.setText(movieTitle);
            overviewText.setText(movieOverview);
            ratingText.setText(movieRating);
            releaseText.setText(movieReleaseDate);
        }
    }
}
