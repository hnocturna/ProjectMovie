package com.example.nocturna.projectmovie.app.disk;

import android.graphics.Bitmap;

/**
 * Call back interface for passing the loaded Bitmap to the UI thread after loading in background
 * Created by hnoct on 12/28/2016.
 */

public interface LoadImageResponse {
    void processFinished(Bitmap loadedBitmap);
}
