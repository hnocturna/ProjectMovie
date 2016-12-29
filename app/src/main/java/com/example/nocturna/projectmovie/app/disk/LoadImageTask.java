package com.example.nocturna.projectmovie.app.disk;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;

/**
 * Background task for loading images from disk. Also used to prevent CursorAdapter from attempting
 * to load an image from the background multiple times if not loaded as task
 *
 * Created by hnoct on 12/28/2016.
 */

public class LoadImageTask extends AsyncTask<Object, Void, Bitmap> {
    // Member variables
    Context mContext;

    // Callback Interface
    public LoadImageResponse delegate = null;

    public LoadImageTask(Context context, LoadImageResponse delegate) {
        this.mContext = context;
        this.delegate = delegate;   // Assign call back interface through constructor
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        // Initialize variables from passed parameters
        long movieId = (Long) params[0];
        String type = (String) params[1];       // Image type to be loaded

        ContextWrapper contextWrapper = new ContextWrapper(mContext);
        File directory = null;

        // Set directory according to image type
        switch (type) {
            case (ImageContract.POSTER_TYPE): {
                directory = contextWrapper.getDir(ImageContract.POSTER_DIRECTORY, Context.MODE_PRIVATE);
                break;
            }
            case (ImageContract.BACKDROP_TYPE): {
                directory = contextWrapper.getDir(ImageContract.BACKDROP_DIRECTORY, Context.MODE_PRIVATE);
                break;
            }
            case (ImageContract.TRAILER_TYPE): {
                directory = contextWrapper.getDir(ImageContract.TRAILER_DIRECTORY, Context.MODE_PRIVATE);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown type: " + type);
            }
        }

        File imageFile = new File(directory, movieId + ImageContract.PNG_FILE_TYPE);
        if (!imageFile.exists()) {
            return null;
        }
        // Decode the bitmap from the file directory
        Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.toString());
        return imageBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        // Pass the loaded bitmap to the callback interface
        delegate.processFinished(bitmap);
    }
}
