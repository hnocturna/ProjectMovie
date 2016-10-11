package com.example.nocturna.projectmovie;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nocturna on 10/9/2016.
 */

public class MoviePosterAdapter extends BaseAdapter {
    String LOG_TAG = MoviePosterAdapter.class.getSimpleName();
    List<Movie> movieList;
    Context mContext;

    public MoviePosterAdapter(Context context, Movie[] movieArray) {
        if (movieArray != null) {
            this.movieList = new ArrayList<>(Arrays.asList(movieArray));
        } else {
            this.movieList = new ArrayList<>();
        }
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        Movie movie = movieList.get(position);

        Log.v(LOG_TAG, "TEST");
        if (convertView == null) {
            // LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // convertView = inflater.inflate(R.layout.movies_item, parent, false);
            imageView = new ImageView(mContext);
            // imageView.setLayoutParams(new GridView.LayoutParams(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.MATCH_PARENT));

        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageBitmap(movie.getPoster());
        Log.v(LOG_TAG, "Loading poster URL: " + movieList.get(position));
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
