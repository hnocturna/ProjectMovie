package com.example.nocturna.projectmovie.app;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nocturna on 10/9/2016.
 * Adapter for loading poster images to display in the GridView of the main screen by getting the
 * poster URL from the database, downloading the poster, and then displaying it in the ImageView
 */

public class MoviePosterAdapter extends CursorAdapter {
    String LOG_TAG = MoviePosterAdapter.class.getSimpleName();
    // Member variables
    Map<Long, Bitmap> mMoviePosterMap;      // Used to hold the posters downloaded in memory so they do not need to be continually downloaded each time the view is loaded.

    public MoviePosterAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    /**
     * Creates a new view to hold the data from the cursor
     * @param context interface to application's global information
     * @param cursor cursor to retrieve data from
     * @param parent parent of the view to be created
     * @return newly created view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movieposter, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, cursor.getPosition());
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        // Instantiate the Bitmap array with the number of rows returned by the cursor
        if (newCursor != null && mMoviePosterMap == null) {
            mMoviePosterMap = new HashMap<>();
        }
        return super.swapCursor(newCursor);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (mMoviePosterMap == null) {
            return;
        }
        // Variables to be passed to the FetchPosterTask
        int cursorPosition = cursor.getPosition();
        int cursorSize = cursor.getCount();
        String posterPath = cursor.getString(MainActivityFragment.COL_POSTER);
        long movieId = cursor.getLong(MainActivityFragment.COL_MOVIE_ID);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        ImageView imageView = viewHolder.posterView;

        if (mMoviePosterMap.get(movieId) == null) {
            Object[] params = new Object[] {posterPath, movieId, cursorPosition, cursorSize, viewHolder};
            FetchPosterTask fetchPosterTask = new FetchPosterTask();
            fetchPosterTask.execute(params);
        } else {
            imageView.setImageBitmap(mMoviePosterMap.get(movieId));
        }
    }

    /**
     * Helper Class for setting children of the view. Reduces the time required for the system to
     * find each view
     */
    public static class ViewHolder {
        public final ImageView posterView;
        public final int position;

        public ViewHolder(View view, int position) {
            posterView = (ImageView) view.findViewById(R.id.list_poster_image);
            this.position = position;
        }
    }

    /**
     * AsyncTask for downloading poster images in background and loading them into the ImageView
     * being inflated into the GridView
     */
    private class FetchPosterTask extends AsyncTask<Object, Void, Bitmap> {
        ViewHolder viewHolder;
        int position;
        int size;
        long movieId;

        @Override
        protected Bitmap doInBackground(Object... params) {
            long startTime = System.currentTimeMillis();

            // Retrieve the variables passed
            String posterPath = (String) params[0];         // Path of the poster
            this.movieId = (Long) params[1];
            this.position = (Integer) params[2];            // The position of the cursor
            this.size = (Integer) params[3];                // Number of rows returned by the cursor
            this.viewHolder = (ViewHolder) params[4];      // ImageView requiring poster

            // Poster is defined outside of the try block so that it can be passed to the onPostExecute
            Bitmap poster = null;

            if (posterPath.isEmpty()) {
                // No URL passed. Nothing to do.
                return null;
            }
            // Defined outside of try block so it can be closed in the finally block
            HttpURLConnection posterConnection = null;

            try {
                // Open a connection to the poster image.
                URL posterUrl = new URL(posterPath);
                posterConnection = (HttpURLConnection) posterUrl.openConnection();
                posterConnection.setDoInput(true);
                posterConnection.connect();

                // Convert to an input stream and utilize BitmapFactory to output a bitmap
                InputStream bitmapStream = posterConnection.getInputStream();
                poster = BitmapFactory.decodeStream(bitmapStream);
                bitmapStream.close();
                mMoviePosterMap.put(movieId, poster);
            } catch (MalformedURLException e) {
                // Error if URL is incorrect
                Log.d(LOG_TAG, "Error ", e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error ", e);
                e.printStackTrace();
            } finally {
                // Close any active connections
                if (posterConnection != null) {
                    posterConnection.disconnect();
                }
            }
            long endTime = System.currentTimeMillis();
            long timeElapsed = endTime - startTime;
            Log.v(LOG_TAG, "Time elapsed for thread " + position + ": " + timeElapsed + "ms");
            return poster;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Load the bitmap into the ImageView
            if (position == viewHolder.position) {
                viewHolder.posterView.setImageBitmap(bitmap);
            }
            if (position == size - 1) {
                notifyDataSetChanged();
            }

        }
    }
}
