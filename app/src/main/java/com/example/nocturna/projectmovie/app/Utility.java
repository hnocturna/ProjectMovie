package com.example.nocturna.projectmovie.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by hnoct on 12/12/2016.
 */

public class Utility {
    public static Movie[] cleanMovieArray(Movie[] movieArray) {
        List<Movie> list = new ArrayList<Movie>(Arrays.asList(movieArray));
        list.removeAll(Collections.singleton(null));
        return list.toArray(new Movie[list.size()]);
    }
}
