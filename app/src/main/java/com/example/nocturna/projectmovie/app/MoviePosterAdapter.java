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

import com.example.nocturna.projectmovie.app.disk.ImageContract;
import com.example.nocturna.projectmovie.app.disk.LoadImageResponse;
import com.example.nocturna.projectmovie.app.disk.LoadImageTask;
import com.example.nocturna.projectmovie.app.disk.SaveImageTask;

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
    // Constants
    private final String LOG_TAG = MoviePosterAdapter.class.getSimpleName();

    // Member variables
    Map<Long, Bitmap> mMoviePosterMap;              // Used to hold the posters downloaded in memory so they do not need to be continually downloaded each time the view is loaded.
    Map<Long, FetchPosterTask> mDownloadTasks;      // Holds all download tasks in progress, preventing duplicate tasks from running in background
    Map<Long, LoadImageTask> mLoadTasks;            // Holds all image loading tasks in progress, preventing duplicate tasks from running in background


    /**
     * Default constructor provided by Android
     * @param context interface for global context
     * @param cursor cursor containing all rows to be cycled
     * @param flags flags
     */
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
        // Instantiate the Map binding movieId to the correct poster Bitmap
        if (newCursor != null && mMoviePosterMap == null) {
            mMoviePosterMap = new HashMap<>();
        }
        return super.swapCursor(newCursor);
    }

    // Pre-maturely any tasks that are in working the the background to allow for smooth UI
    public void cancelTasks() {
        if (mDownloadTasks != null) {
            for (long movieId : mDownloadTasks.keySet()) {
                mDownloadTasks.get(movieId).cancel(true);
            }
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (mMoviePosterMap == null) {
            return;
        }
        // Variables to be passed to the FetchPosterTask
        final int cursorPosition = cursor.getPosition();
        final int cursorSize = cursor.getCount();
        final String posterPath = cursor.getString(MainActivityFragment.COL_POSTER);
        final long movieId = cursor.getLong(MainActivityFragment.COL_MOVIE_ID);

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        final ImageView imageView = viewHolder.posterView;

        if (mMoviePosterMap.get(movieId) == null) {
            // Load the image from disk if it exists
            Object[] loadParams = new Object[] {movieId, ImageContract.POSTER_TYPE};
            LoadImageTask loadImageTask = new LoadImageTask(mContext, new LoadImageResponse() {
                @Override
                public void processFinished(Bitmap loadedBitmap) {
                    if (loadedBitmap != null) {
                        // If bitmap is loaded, add image to memory and set the ImageView
                        mMoviePosterMap.put(movieId, loadedBitmap);
                        notifyDataSetChanged();
                    } else {
                        Log.v(LOG_TAG, "Poster not found on disk, downloading poster");
                        // Download the poster if it does not exist in the map
                        FetchPosterTask fetchPosterTask = new FetchPosterTask();
                        Object[] params = new Object[] {posterPath, movieId, cursorPosition, cursorSize, viewHolder, fetchPosterTask};

                        if (mDownloadTasks == null) {
                            // Create a new HashMap to hold all tasks that are running
                            mDownloadTasks = new HashMap<>();
                        }

                        if (!mDownloadTasks.keySet().contains(movieId)) {
                            // Only allow the AsyncTask to run if the same task isn't already running to prevent
                            // duplicate threads
                            mDownloadTasks.put(movieId, fetchPosterTask);
                            fetchPosterTask.execute(params);
                        }
                    }
                    mLoadTasks.remove(movieId);
                }
            });
            if (mLoadTasks == null) {
                mLoadTasks = new HashMap<>();
            }
            if (!mLoadTasks.keySet().contains(movieId)) {
                mLoadTasks.put(movieId, loadImageTask);
                loadImageTask.execute(loadParams);
            }
        } else {
            // If image already exists in HashMap, then set image
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

//    /**
//     * Loads poster image from disk if it exists
//     * @param movieId movie poster to be loaded
//     * @return Bitmap image of the movie's poster
//     */
//    private Bitmap loadPoster(long movieId) {
//        ContextWrapper contextWrapper = new ContextWrapper(mContext);
//        File directory = contextWrapper.getDir(ImageContract.POSTER_DIRECTORY, Context.MODE_PRIVATE);
//
//        if (!directory.exists()) {
//            // No poster to return if directory does not exist
//            return null;
//        }
//        File posterBitmapFile = new File(directory, movieId + PNG_FILE_TYPE);
//
//        if (!posterBitmapFile.exists()) {
//            Log.v(LOG_TAG, posterBitmapFile.toString());
//            return null;
//        }
//
//        Bitmap posterBitmap = null;
//        FileInputStream inStream = null;
//        try {
//            // Create inputstream of the file and decode to a bitmap
//            inStream = new FileInputStream(posterBitmapFile);
//            posterBitmap = BitmapFactory.decodeStream(inStream);
//            if (posterBitmap == null) {
//                // No poster at the file location
//                return null;
//            }
//        } catch (FileNotFoundException e) {
//            Log.v(LOG_TAG, "Poster image not found");
//            return null;
//        }
//        return posterBitmap;
//    }

    /**
     * AsyncTask for downloading poster images in background and loading them into the ImageView
     * being inflated into the GridView
     */
    private class FetchPosterTask extends AsyncTask<Object, Void, Bitmap> {
        ViewHolder viewHolder;
        int position;
        int size;
        long movieId;
        FetchPosterTask fetchPosterTask;

        @Override
        protected Bitmap doInBackground(Object... params) {
            // Retrieve the variables passed
            String posterPath = (String) params[0];         // Path of the poster
            this.movieId = (Long) params[1];                // movieId
            this.position = (Integer) params[2];            // The position of the cursor
            this.size = (Integer) params[3];                // Number of rows returned by the cursor
            this.viewHolder = (ViewHolder) params[4];       // ImageView requiring poster
            this.fetchPosterTask = (FetchPosterTask) params[5];

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

                if (isCancelled()) {
                    return null;
                }

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
            return poster;
        }

        @Override
        protected void onPostExecute(Bitmap posterBitmap) {
            // Update UI
            notifyDataSetChanged();

            // Remove the task from mDownloadTasks so that other tasks can be loaded
            mDownloadTasks.remove(movieId);

            // Save file to directory so it doesn't need to be downloaded every time the
            // activity is killed
            Object[] saveParams = new Object[] {movieId, posterBitmap, ImageContract.POSTER_TYPE};
            SaveImageTask saveImageTask = new SaveImageTask(mContext);
            saveImageTask.execute(saveParams);
        }
    }
}
