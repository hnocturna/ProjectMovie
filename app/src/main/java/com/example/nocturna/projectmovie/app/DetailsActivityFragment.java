package com.example.nocturna.projectmovie.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.nocturna.projectmovie.app.data.MovieContract;
import com.example.nocturna.projectmovie.app.disk.ImageContract;
import com.example.nocturna.projectmovie.app.disk.LoadImageResponse;
import com.example.nocturna.projectmovie.app.disk.LoadImageTask;
import com.example.nocturna.projectmovie.app.disk.SaveImageTask;

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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // Member variables
    Uri mMovieUri;                  // URI for movie
    Context mContext;               // Global interface passed from constructor
    long mMovieId;
    ReviewAdapter mReviewAdapter;
    float mPixels;                  // Set equal to 1 dip
    float LAYOUT_MARGIN;            // Retrieved from @dimen/layout_margin to set margins dynamically
    float SEPARATION_MARGIN;        // Retrieved from @dimen/separation_margin to set margins dynamically
    Bitmap mBackdropBitmap;         // Holds backdrop image to save to disk
    Bitmap mTrailerBitmap;          // Holds trailer image to save to disk

    // Constants
    String LOG_TAG = DetailsActivityFragment.class.getSimpleName();
    final String API_KEY = BuildConfig.API_KEY;
    final String API_PARAM = "api_key";
    final String BASE_URI = "http://api.themoviedb.org/3/movie";
    private static final int DETAILS_LOADER = 1;
    final static String DETAILS_URI = "detailUri";


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


    // Views and Layouts to be populated
    TextView titleText;
    TextView overviewText;
    TextView ratingText;
    TextView releaseText;
    TextView genreText;
    TextView trailerText;
    TextView reviewTitleText;

    ImageView backdropImage;
    ImageView trailerImage;
    ImageView favoriteIcon;

    LinearLayout backgroundLayout;
    LinearLayout trailerLayout;

    ListView reviewListView;

    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        // Retrieve URI
        if (getArguments() != null) {
            mMovieUri = getArguments().getParcelable(DETAILS_URI);
        }

        // Initialize member variables
        if (mMovieUri != null) {
            mMovieId = MovieContract.MovieEntry.getMovieIdFromUri(mMovieUri);
        }
        mContext = getActivity();

        // Initialize views as member variables to prevent waste of resources from constantly
        // traversing the view hierarchy in onLoadFinished
        titleText = (TextView) rootView.findViewById(R.id.detail_title_text);
        overviewText = (TextView) rootView.findViewById(R.id.detail_overview_text);
        ratingText = (TextView) rootView.findViewById(R.id.detail_rating_text);
        releaseText = (TextView) rootView.findViewById(R.id.detail_release_text);
        genreText = (TextView) rootView.findViewById(R.id.detail_genre_text);
        trailerText = (TextView) rootView.findViewById(R.id.detail_trailer_text);
        reviewTitleText = (TextView) rootView.findViewById(R.id.detail_review_title);

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

                    favoriteIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.star_on));
                     if (mBackdropBitmap != null) {
                         Object[] saveParams = new Object[] {mMovieId, mBackdropBitmap, ImageContract.BACKDROP_TYPE};
                         new SaveImageTask(mContext).execute(saveParams);
                     }

                    if (mTrailerBitmap != null) {
                        Object[] saveParams = new Object[] {mMovieId, mTrailerBitmap, ImageContract.TRAILER_TYPE};
                        new SaveImageTask(mContext).execute(saveParams);
                    }


                } else {
                    ContentValues values = new ContentValues();
                    values.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 0);

                    mContext.getContentResolver().update(
                            MovieContract.MovieEntry.CONTENT_URI,
                            values,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[] {Long.toString(mMovieId)}
                    );

                    favoriteIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.star_off));
                }

                cursor.close();
            }
        });

        backgroundLayout = (LinearLayout) rootView.findViewById(R.id.detail_subtitle_background);
        trailerLayout = (LinearLayout) rootView.findViewById(R.id.detail_trailer_layout);

        // Create pixel variable to work in dips
        mPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        // Set so that trailers appear at the bottom of the page, allowing just enough room for the
        // "Reviews" TextView to show if there are reviews
        overviewText.setMinHeight(Math.round(148 * mPixels));

        reviewListView = (ListView) rootView.findViewById(R.id.detail_review_list);
        reviewListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mReviewAdapter.isExpanded(position)) {
                    mReviewAdapter.contractContent(position);
                } else {
                    mReviewAdapter.expandContent(position);
                }
            }
        });

        // Initialize margin variables
        LAYOUT_MARGIN = getResources().getDimension(R.dimen.layout_margin);
        SEPARATION_MARGIN = getResources().getDimension(R.dimen.separation_margin);

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
        final long movieId = cursor.getLong(COL_MOVIE_ID);
        final String title = cursor.getString(COL_TITLE);
        final String overview = cursor.getString(COL_OVERVIEW);
        final String rating = Utility.formatRating(cursor.getDouble(COL_RATING));
        final String releaseDate = Utility.longToDate(cursor.getLong(COL_RELEASE_DATE));
        final String posterPath = cursor.getString(COL_POSTER);
        final String backdropPath = cursor.getString(COL_BACKDROP);
        StringBuilder genreBuilder = new StringBuilder(cursor.getString(COL_GENRE));

        // Download trailer data if it doesn't not exist in database
        if (cursor.getString(COL_TRAILER) != null) {
            final String trailerPath = cursor.getString(COL_TRAILER);


            new LoadImageTask(mContext, new LoadImageResponse() {
                @Override
                public void processFinished(Bitmap loadedBitmap) {
                    if (loadedBitmap != null) {
                        // Padding is set dynamically so that it doesn't create empty space at the bottom of
                        // the page if there is nothing to scroll to
                        trailerLayout.setPadding(
                                Math.round(LAYOUT_MARGIN),
                                Math.round(SEPARATION_MARGIN),
                                Math.round(LAYOUT_MARGIN),
                                Math.round(SEPARATION_MARGIN)
                        );
                        trailerImage.setImageBitmap(loadedBitmap);
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
                        trailerText.setText(mContext.getString(R.string.trailers));
                    } else {
                        // If trailer thumbnail does not exist on drive, download from TheMovieDB
                        FetchTrailerThumbnailTask fetchTrailerThumbnailTask = new FetchTrailerThumbnailTask();
                        fetchTrailerThumbnailTask.execute(trailerPath);
                    }
                }
            }).execute(movieId, ImageContract.TRAILER_TYPE);
        } else {
            // Trailer path doesn't exist, retrieve trailer path from TheMovieDB and download
            // trailer thumbnail from YouTube
            FetchTrailerTask fetchTrailerTask = new FetchTrailerTask();
            fetchTrailerTask.execute(movieId);
        }

        // Set the favorite icon to correct tint if favorited in database
        if (cursor.getInt(COL_FAVORITE) == 0) {
            favoriteIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.star_off));
        } else {
            favoriteIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.star_on));
        }

        // Get all genres of the movie
        while (cursor.moveToNext()) {
            genreBuilder.append(", " + cursor.getString(COL_GENRE));
        }

        final String genres = genreBuilder.toString();

        // Attempt to load image from disk if it exists
        new LoadImageTask(mContext, new LoadImageResponse() {
            @Override
            public void processFinished(Bitmap loadedBitmap) {
                if (loadedBitmap != null) {
                    // Set the ImageView
                    backdropImage.setImageBitmap(loadedBitmap);

                    // Set the background of the subtitle text to the average color of backdrop
                    int backgroundColor = Utility.getDominantColor(loadedBitmap);
                    backgroundLayout.setBackgroundColor(backgroundColor);

                    // Set the text of the TextViews after poster has been loaded to prevent awkward
                    // positioning of text while images load
                    titleText.setText(title);
                    overviewText.setText(overview);
                    ratingText.setText(rating);
                    releaseText.setText(releaseDate);
                    genreText.setText(genres);
                } else {
                    // Image does not exist on disk, download from TheMovieDb
                    String[] params = new String[] {posterPath, backdropPath, title, overview, rating, releaseDate, genres};
                    FetchImageTask fetchImageTask = new FetchImageTask();
                    fetchImageTask.execute(params);
                }

            }
        }).execute(movieId, ImageContract.BACKDROP_TYPE);

        // Download movie reviews
        Long[] reviewParams = new Long[] {movieId};
        FetchReviewTask fetchReviewTask = new FetchReviewTask();
        fetchReviewTask.execute(reviewParams);
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
                mBackdropBitmap = images[1];
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
            }
        }
    }

    private class FetchTrailerTask extends AsyncTask<Long, Void, String> {
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
                Log.v(LOG_TAG, "Trailer JSON String length :" + buffer.length());
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
            if (trailerJsonString != null && trailerJsonString.length() > 0) {
                try {
                    getTrailerFromString(movieId, trailerJsonString);
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "JSON Exception", e);
                    e.printStackTrace();
                }
            }

            // Retrieve trailer path downloaded from JSON data
            Cursor cursor = mContext.getContentResolver().query(
                    MovieContract.LinkEntry.buildGenresUriFromMovieId(movieId),
                    DETAILS_COLUMNS,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();
            String trailerPath = cursor.getString(COL_TRAILER);
            cursor.close();

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
            if (trailerPath != null && trailerPath.length() > 0) {
                FetchTrailerThumbnailTask fetchTrailerThumbnailTask = new FetchTrailerThumbnailTask();
                fetchTrailerThumbnailTask.execute(trailerPath);
            }
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
        protected void onPostExecute(Bitmap trailerBitmap) {
            // Set the thumbnail for the trailer
            if (trailerBitmap != null) {
                mTrailerBitmap = trailerBitmap;
                // Padding is set dynamically so that it doesn't create empty space at the bottom of
                // the page if there is nothing to scroll to
                trailerLayout.setPadding(
                        Math.round(LAYOUT_MARGIN),
                        Math.round(SEPARATION_MARGIN),
                        Math.round(LAYOUT_MARGIN),
                        Math.round(SEPARATION_MARGIN)
                );
                trailerImage.setImageBitmap(trailerBitmap);
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
                trailerText.setText(mContext.getString(R.string.trailers));
            }
        }
    }

    private class FetchReviewTask extends AsyncTask<Long, Void, Map<String, String>> {

        private Map<String, String> parseReviews(String jsonReviewString) throws JSONException {
            // Constants
            final String TMD_RESULTS = "results";
            final String TMD_AUTHOR = "author";
            final String TMD_CONTENT = "content";

            // Initialize LinkedHashMap to store reviews
            Map<String, String> reviewMap = new LinkedHashMap<>();

            JSONObject jsonReview = new JSONObject(jsonReviewString);
            JSONArray jsonReviewArray = jsonReview.getJSONArray(TMD_RESULTS);

            for (int i = 0; i < jsonReviewArray.length(); i++) {
                // Retrieve the author and content of the review
                String author = jsonReviewArray.getJSONObject(i).getString(TMD_AUTHOR);
                String content = jsonReviewArray.getJSONObject(i).getString(TMD_CONTENT);

                // Add author-content key-value pair to the review map to populate the ListView
                reviewMap.put(author, content);
            }

            return reviewMap;
        }

        @Override
        protected Map<String, String> doInBackground(Long... params) {
            // Constants
            long movieId = params[0];
            final String REVIEW_PATH = "reviews";
            final String LANGUAGE_PARAM = "language";
            final String LANGUAGE = "en-US";

            // Defined outside of try-block to be closed in finally-block
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Review Map to be passed to the onPostExecute
            Map<String, String> reviewMap = null;

            try {
                // Parse the URI and open a connection and download the JSON data
                Uri reviewUri = Uri.parse(BASE_URI).buildUpon()
                        .appendPath(Long.toString(movieId))
                        .appendPath(REVIEW_PATH)
                        .appendQueryParameter(API_PARAM, API_KEY)
                        .appendQueryParameter(LANGUAGE_PARAM, LANGUAGE)
                        .build();

                URL reviewUrl = new URL(reviewUri.toString());
                urlConnection = (HttpURLConnection) reviewUrl.openConnection();
                urlConnection.setRequestMethod("GET");

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing being transferred, nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    // Append new line between each line for easier reading
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Empty stream
                    return null;
                }

                String reviewJson = buffer.toString();
                reviewMap = parseReviews(reviewJson);

            } catch (MalformedURLException e) {
                Log.d(LOG_TAG, "Malformed exception trying to download reviews", e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(LOG_TAG, "IO Exception trying to download reviews", e);
                e.printStackTrace();
            } catch (JSONException e) {
                Log.d(LOG_TAG, "JSON Exception trying to download reviews", e);
                e.printStackTrace();
            } finally {
                // Close opened resources
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.d(LOG_TAG, "Error closing stream", e);
                        e.printStackTrace();
                    }
                }
            }

            return reviewMap;
        }

        @Override
        protected void onPostExecute(Map<String, String> reviewMap) {
            if (reviewMap != null && !reviewMap.keySet().isEmpty()) {
                // If there are reviews, set the adapter. The layout margins are set programmatically
                // because if there are no reviews, the layout will not contain margins that allow
                // the ScrollView to scroll
                mReviewAdapter = new ReviewAdapter(new LinkedHashMap<>(reviewMap), mContext);
                reviewListView.setAdapter(mReviewAdapter);
                reviewListView.setPadding(
                        Math.round(LAYOUT_MARGIN),
                        Math.round(LAYOUT_MARGIN),
                        Math.round(LAYOUT_MARGIN),
                        Math.round(LAYOUT_MARGIN)
                );
                mReviewAdapter.notifyDataSetChanged();
                reviewTitleText.setText(mContext.getString(R.string.reviews));

                // Move up the trailer so that the Review title text can show to indicate that there
                // are reviews to the user
                overviewText.setMinHeight(Math.round(120 * mPixels));
            }
        }
    }
}
