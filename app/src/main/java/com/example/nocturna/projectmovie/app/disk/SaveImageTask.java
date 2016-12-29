package com.example.nocturna.projectmovie.app.disk;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by hnoct on 12/28/2016.
 *
 * Background task for saving images to disk
 */

public class SaveImageTask extends AsyncTask<Object, Void, Void> {
    // Constants
    private final String LOG_TAG = SaveImageTask.class.getSimpleName();

    // Member variables
    private Context mContext;

    /**
     * Constructor
     * @param context interface to global context
     */
    public SaveImageTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Object... params) {
        // Retrieve parameters and initialize variables
        long movieId = (Long) params[0];
        Bitmap bitmap = (Bitmap) params[1];
        String type = (String) params[2];
        ContextWrapper contextWrapper = new ContextWrapper(mContext);
        File directory = null;

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

        if (!directory.exists()) {
            directory.mkdir();
        }

        File bitmapFile = new File(directory, movieId + ImageContract.PNG_FILE_TYPE);
        Log.v(LOG_TAG, bitmapFile.toString());
        FileOutputStream outStream = null;

        try {
            outStream = new FileOutputStream(bitmapFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "Error saving image", e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error closing output stream", e);
            e.printStackTrace();
        }
        return null;
    }
}
