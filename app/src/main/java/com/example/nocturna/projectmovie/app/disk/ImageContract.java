package com.example.nocturna.projectmovie.app.disk;

/**
 * Created by hnoct on 12/28/2016.
 */

public class ImageContract {
    // Constants
    public static final String POSTER_TYPE = "poster";          // For poster thumbnails on the MainActivity
    public static final String BACKDROP_TYPE = "backdrop";      // For backdrops in the DetailsActivity
    public static final String TRAILER_TYPE = "trailer";        // For trailer thumbnails in DetailsActivity

    static final String POSTER_DIRECTORY = "poster_thumbs";     // Directory to save poster thumbnails
    static final String BACKDROP_DIRECTORY = "backdrops";       // Directory to save backdrop images
    static final String TRAILER_DIRECTORY = "trailer_thumbs";   // Directory to save trailer thumbnails
    static final String PNG_FILE_TYPE = ".png";                 // PNG file type
}
