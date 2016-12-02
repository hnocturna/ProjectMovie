package com.example.nocturna.projectmovie.app;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nocturna on 10/9/2016.
 * Takes Movie objects and extracts the bitmap from the object and passes it to an ImageView that is
 * then displayed in the GridView of the MainActivity
 */

public class MoviePosterAdapter extends BaseAdapter {
    String LOG_TAG = MoviePosterAdapter.class.getSimpleName();
    List<Movie> movieList;
    Context mContext;

    public MoviePosterAdapter(Context context, Movie[] movieArray) {
        // Creates a new ArrayList in case we pass in null for some debugging reason
        if (movieArray != null) {
            this.movieList = new ArrayList<>(Arrays.asList(movieArray));
        } else {
            this.movieList = new ArrayList<>();
        }
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        Movie movie = movieList.get(position);

        if (convertView == null) {
            // Creates a new ImageView for the item if it is the first time loading the view
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }
        // Set the poster bitmap to the ImageView
        imageView.setImageBitmap(movie.getPoster());
        imageView.setPadding(0,8,0,8);
        // Log.v(LOG_TAG, "Loading poster URL: " + movieList.get(position));
        return imageView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return movieList.get(position);
    }

    @Override
    public int getCount() {
        return movieList.size();
    }

    public void clear() {
        movieList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void add(Movie movie) {
        movieList.add(movie);
        notifyDataSetChanged();
        // Log.v(LOG_TAG, "Poster URL added: " + url);
    }
}
