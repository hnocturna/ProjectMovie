package com.example.nocturna.projectmovie;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.R.id.list;

/**
 * Created by Nocturna on 10/9/2016.
 */

public class MoviePosterAdapter extends BaseAdapter {
    String LOG_TAG = MoviePosterAdapter.class.getSimpleName();
    List<String> urlList;
    Context mContext;

    public MoviePosterAdapter(Context context, String[] urlArray) {
        if (urlArray != null) {
            this.urlList = new ArrayList<>(Arrays.asList(urlArray));
        } else {
            this.urlList = new ArrayList<>();
        }
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        Log.v(LOG_TAG, "TEST");
        if (convertView == null) {
            // LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // convertView = inflater.inflate(R.layout.movies_item, parent, false);
            imageView = new ImageView(mContext);
            // imageView.setLayoutParams(new GridView.LayoutParams(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.MATCH_PARENT));

        } else {
            imageView = (ImageView) convertView;
        }
        Picasso.with(mContext).load(urlList.get(position)).into(imageView);
        Log.v(LOG_TAG, "Loading poster URL: " + urlList.get(position));
        return imageView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return urlList.get(position);
    }

    @Override
    public int getCount() {
        return urlList.size();
    }

    public void clear() {
        urlList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void add(String url) {
        urlList.add(url);
        notifyDataSetChanged();
        // Log.v(LOG_TAG, "Poster URL added: " + url);
    }
}
