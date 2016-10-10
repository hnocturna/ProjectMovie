package com.example.nocturna.projectmovie;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by Nocturna on 10/9/2016.
 */

public class MoviePosterAdapter extends BaseAdapter {
    Bitmap[] bitmapsArr;
    Context mContext;

    public MoviePosterAdapter(Context context, Bitmap[] bitmaps) {
        bitmapsArr = bitmaps;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.MATCH_PARENT));

        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageBitmap(bitmapsArr[position]);
        return imageView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return bitmapsArr[position];
    }

    @Override
    public int getCount() {
        return bitmapsArr.length;
    }
}
