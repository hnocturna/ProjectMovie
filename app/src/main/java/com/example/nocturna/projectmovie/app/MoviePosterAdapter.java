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

import com.example.nocturna.projectmovie.app.data.MovieContract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Nocturna on 10/9/2016.
 * Adapter for loading poster images to display in the GridView of the main screen by getting the
 * poster URL from the database, downloading the poster, and then displaying it in the ImageView
 */

public class MoviePosterAdapter extends CursorAdapter {
    String LOG_TAG = MoviePosterAdapter.class.getSimpleName();
    // Member variables
    Bitmap[] moviePosters;      // Used to hold the posters downloaded in memory so they do not need to be continually downloaded each time the view is loaded.

    public MoviePosterAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        // Instantiate the member arrays with the number of rows returned by the cursor
        int cursorCount = cursor.getCount();
        moviePosters = new Bitmap[cursorCount];
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
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movieposter, parent, true);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Variables to be passed to the FetchPosterTask
        int cursorPosition = cursor.getPosition();
        String posterPath = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER));
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        ImageView imageView = viewHolder.posterView;

        if (moviePosters[cursorPosition] == null) {
            Object[] params = new Object[] {posterPath, cursorPosition};
        }
    }

    /**
     * Helper Class for setting children of the view. Reduces the time required for the system to
     * find each view
     */
    public static class ViewHolder {
        public final ImageView posterView;

        public ViewHolder(View view) {
            posterView = (ImageView) view.findViewById(R.id.detail_poster_image);
        }
    }

    private class FetchPosterTask extends AsyncTask<Object, Void, Bitmap> {
        ImageView posterView;

        @Override
        protected Bitmap doInBackground(Object... params) {
            // Retrieve the variables passed
            String posterPath = (String) params[0];         // Path of the poster
            int position = (Integer) params[1];             // The position of the cursor
            this.posterView = (ImageView) params[2];        // ImageView requiring poster
            Bitmap poster = null;                           // Bitmap to be passed to onPostExecute

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

                moviePosters[position] = poster;
            } catch (MalformedURLException e) {
                // Error if URL is incorrect
                Log.d(LOG_TAG, "Error ", e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error ", e);
                e.printStackTrace();
            } finally {
                if (posterConnection != null) {
                    posterConnection.disconnect();
                }
            }
            return poster;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            posterView.setImageBitmap(bitmap);
        }
    }
}
